package org.guvnor.tools.wizards;

import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.webdav.IResponse;
import org.guvnor.tools.Activator;
import org.guvnor.tools.utils.GuvnorMetadataProps;
import org.guvnor.tools.utils.GuvnorMetadataUtils;
import org.guvnor.tools.utils.PlatformUtils;
import org.guvnor.tools.utils.webdav.IWebDavClient;
import org.guvnor.tools.utils.webdav.ResourceProperties;
import org.guvnor.tools.utils.webdav.WebDavClientFactory;
import org.guvnor.tools.utils.webdav.WebDavException;
import org.guvnor.tools.utils.webdav.WebDavServerCache;

public class AddResourceWizard extends Wizard implements INewWizard, IGuvnorWizard {
	
	private IFile selectedFile;
	
	private SelectGuvnorRepPage		selectRepPage;
	private GuvnorMainConfigPage 	mainConfigPage;
	private SelectGuvnorFolderPage 	selectFolderPage;
	
	private GuvWizardModel model;
	
	public AddResourceWizard() {
		model = new GuvWizardModel();
	}
	
	public GuvWizardModel getModel() {
		return model;
	}
	
	public void init(IWorkbench workbench, IStructuredSelection selection) { 
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection)selection;
			if (sel.getFirstElement() instanceof IFile) {
				selectedFile = (IFile)sel.getFirstElement();
			}
		}
	}
	
	@Override
	public void addPages() {
		selectRepPage = new SelectGuvnorRepPage("select_rep_page", "Select Guvnor repository location",
								Activator.getImageDescriptor(Activator.IMG_GUVREPWIZBAN));
		selectRepPage.setDescription("Select an existing Guvnor repository location or create a new one");
		super.addPage(selectRepPage);
		
		mainConfigPage = new GuvnorMainConfigPage("config_page", "New Guvnor repository location", 
								Activator.getImageDescriptor(Activator.IMG_GUVREPWIZBAN));
		mainConfigPage.setDescription("Creates a connection to a Guvnor repository");
		super.addPage(mainConfigPage);

		selectFolderPage = new SelectGuvnorFolderPage("select_folder_page", "Select folder",
								Activator.getImageDescriptor(Activator.IMG_GUVREPWIZBAN));
		selectFolderPage.setDescription("Select the target folder in the Guvnor repository");
		super.addPage(selectFolderPage);

		super.addPages();
	}
	
	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page.getName().equals("select_rep_page")) {
			if (model.shouldCreateNewRep()) {
				return mainConfigPage;
			} else {
				return selectFolderPage;
			}
		}
		if (page.getName().equals("config_page")) {
			return selectFolderPage;
		}
		return null;
	}
	
	private void setDuplicateFileError() {
		selectFolderPage.setErrorMessage(selectedFile.getName() +
				                        " already exists in folder " +
				                        model.getTargetLocation());
		model.setTargetLocation(null);
		super.getContainer().updateButtons();
	}
	
	@Override
	public boolean performFinish() {
		assert(selectedFile != null);
		boolean res = false;
		try {
			String fullPath = model.getTargetLocation() + selectedFile.getName();
			IWebDavClient client = WebDavServerCache.getWebDavClient(model.getRepLocation());
			if (client == null) {
				client = WebDavClientFactory.createClient(new URL(model.getRepLocation()));
				WebDavServerCache.cacheWebDavClient(model.getRepLocation(), client);
			}
			try {
				res = client.createResource(fullPath, selectedFile.getContents(), false);
				client.closeResponse();
				if (!res) {
					setDuplicateFileError();
				}
			} catch (WebDavException wde) {
				if (wde.getErrorCode() != IResponse.SC_UNAUTHORIZED) {
					// If not an authentication failure, we don't know what to do with it
					throw wde;
				}
				boolean retry = PlatformUtils.getInstance().
									authenticateForServer(model.getRepLocation(), client); 
				if (retry) {
					res = client.createResource(fullPath, selectedFile.getContents());
					client.closeResponse();
					if (!res) {
						setDuplicateFileError();
					}
				}
			}
			if (res) {
				GuvnorMetadataUtils.markCurrentGuvnorResource(selectedFile);
				ResourceProperties resProps = client.queryProperties(fullPath);
				client.closeResponse();
				GuvnorMetadataProps mdProps = 
						new GuvnorMetadataProps(selectedFile.getName(),
					                           model.getRepLocation(),
					                           fullPath, resProps.getLastModifiedDate());
				GuvnorMetadataUtils.setGuvnorMetadataProps(selectedFile.getFullPath(), mdProps);
				PlatformUtils.updateDecoration();
			}
		} catch (Exception e) {
			Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
		}
		return res;
	}
	
	@Override
	public boolean canFinish() {	
		return model.getRepLocation() != null 
		       && model.getTargetLocation() != null;
	}
}
