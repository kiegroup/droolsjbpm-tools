package org.guvnor.tools.wizards;

import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.guvnor.tools.Activator;
import org.guvnor.tools.utils.GuvnorMetadataUtils;
import org.guvnor.tools.utils.webdav.IWebDavClient;
import org.guvnor.tools.utils.webdav.ResourceProperties;
import org.guvnor.tools.utils.webdav.WebDavServerCache;

public class CheckoutWizard extends Wizard implements INewWizard, IGuvnorWizard {
	
	private GuvnorMainConfigPage 			mainConfigPage;
	private SelectGuvnorRepPage 			selectRepPage;
	private SelectGuvnorResourcesPage 		selectResPage;
	private SelectLocalTargetPage			selectLocalTargetPage;
//	private SelectResourceVersionPage		selectVerPage;
	
	private GuvWizardModel model;
	
	public CheckoutWizard() {
		model = new GuvWizardModel();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.jboss.tools.guvnor.wizards.IGuvnorWizard#getModel()
	 */
	public GuvWizardModel getModel() {
		return model;
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
		
		selectResPage = new SelectGuvnorResourcesPage("select_res_page", "Select resources",
											Activator.getImageDescriptor(Activator.IMG_GUVREPWIZBAN));
		selectResPage.setDescription("Select resources to copy from the Guvnor repository");
		super.addPage(selectResPage);
		
		selectLocalTargetPage = new SelectLocalTargetPage("local_target_page", "Select copy location",
											Activator.getImageDescriptor(Activator.IMG_GUVREPWIZBAN));
		selectLocalTargetPage.setDescription("Select the destination location");
		super.addPage(selectLocalTargetPage);
		
//		selectVerPage = new SelectResourceVersionPage("select_version_page", "Select resource versions",
//											Activator.getImageDescriptor(Activator.IMG_GUVREPWIZBAN));
//		selectVerPage.setDescription("Select the version of the resources to check out");
//		super.addPage(selectVerPage);
		
		super.addPages();
	}
	
	@Override
	public boolean performFinish() {
		try {
			IWebDavClient webdav = WebDavServerCache.getWebDavClient(model.getRepLocation());
			// During the course of the wizard, the user had to drill into a Guvnor repository
			// to choose resources. Therefore, we should have a cached repository connection
			// that is authenticated already. If not, something is really strange.
			assert(webdav != null);
			IPath metaPath = GuvnorMetadataUtils.createGuvnorMetadataLocation(model.getTargetLocation());
			IWorkspaceRoot wsRoot = ResourcesPlugin.getWorkspace().getRoot();
			for (String oneResource:model.getResources()) {
				// Get the metadata properties
				ResourceProperties resprops = webdav.queryProperties(oneResource);
				if (resprops == null) {
					throw new Exception("Null resource properties for " + oneResource);
				}
			}
		} catch (Exception e) {
			Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
		}
		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		
	}

	@Override
	public boolean canFinish() {
		return model.isModelComplete();
	}
}
