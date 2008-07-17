package org.guvnor.tools.wizards;

import java.net.URL;
import java.util.Iterator;

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
	
	private SelectGuvnorRepPage		selectRepPage;
	private GuvnorMainConfigPage 	mainConfigPage;
	private SelectGuvnorFolderPage 	selectFolderPage;
	
	private IStructuredSelection 	selectedItems;
	
	private GuvWizardModel model;
	
	public AddResourceWizard() {
		model = new GuvWizardModel();
	}
	
	public GuvWizardModel getModel() {
		return model;
	}
	
	public void init(IWorkbench workbench, IStructuredSelection selection) { 
		selectedItems = selection;
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
	
	private void setDuplicateFileError(IFile selectedFile) {
		selectFolderPage.setErrorMessage(selectedFile.getName() +
				                        " already exists in folder " +
				                        model.getTargetLocation());
		model.setTargetLocation(null);
		super.getContainer().updateButtons();
	}
	
	private boolean processSelectedFile(IFile selectedFile) {
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
				if (!res) {
					setDuplicateFileError(selectedFile);
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
					if (!res) {
						setDuplicateFileError(selectedFile);
					}
				}
			}
			if (res) {
				GuvnorMetadataUtils.markCurrentGuvnorResource(selectedFile);
				ResourceProperties resProps = client.queryProperties(fullPath);
				GuvnorMetadataProps mdProps = 
						new GuvnorMetadataProps(selectedFile.getName(),
					                           model.getRepLocation(),
					                           fullPath, resProps.getLastModifiedDate(),
					                           resProps.getRevision());
				GuvnorMetadataUtils.setGuvnorMetadataProps(selectedFile.getFullPath(), mdProps);
			}
		} catch (Exception e) {
			Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
		}
		return res;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean performFinish() {
		boolean res = true;
		assert(selectedItems != null);
		for (Iterator it = selectedItems.iterator(); it.hasNext();) {
			Object oneItem = it.next();
			if (oneItem instanceof IFile) {
				res = processSelectedFile((IFile)oneItem);
				if (!res) {
					break;
				}
			}
		}
		PlatformUtils.updateDecoration();
		return res;
	}
	
	@Override
	public boolean canFinish() {	
		return model.getRepLocation() != null 
		       && model.getTargetLocation() != null;
	}
}
