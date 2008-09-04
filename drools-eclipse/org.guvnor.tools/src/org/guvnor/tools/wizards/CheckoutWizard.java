package org.guvnor.tools.wizards;

import java.io.ByteArrayInputStream;
import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.guvnor.tools.Activator;
import org.guvnor.tools.Messages;
import org.guvnor.tools.utils.GuvnorMetadataProps;
import org.guvnor.tools.utils.GuvnorMetadataUtils;
import org.guvnor.tools.utils.webdav.IWebDavClient;
import org.guvnor.tools.utils.webdav.ResourceProperties;
import org.guvnor.tools.utils.webdav.WebDavServerCache;

/**
 * Wizard for copying Guvnor resources to the local workspace.
 * @author jgraham
 */
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
		selectRepPage = new SelectGuvnorRepPage("select_rep_page", Messages.getString("select.guvnor.rep"), //$NON-NLS-1$ //$NON-NLS-2$
                							Activator.getImageDescriptor(Activator.IMG_GUVREPWIZBAN));
		selectRepPage.setDescription(Messages.getString("select.guvnor.rep.desc")); //$NON-NLS-1$
		super.addPage(selectRepPage);
		
		mainConfigPage = new GuvnorMainConfigPage("config_page", Messages.getString("new.guvnor.rep"),  //$NON-NLS-1$ //$NON-NLS-2$
											Activator.getImageDescriptor(Activator.IMG_GUVREPWIZBAN));
		mainConfigPage.setDescription(Messages.getString("new.guvnor.rep.desc")); //$NON-NLS-1$
		super.addPage(mainConfigPage);
		
		selectResPage = new SelectGuvnorResourcesPage("select_res_page", Messages.getString("select.resources"), //$NON-NLS-1$ //$NON-NLS-2$
											Activator.getImageDescriptor(Activator.IMG_GUVREPWIZBAN));
		selectResPage.setDescription(Messages.getString("select.resources.desc")); //$NON-NLS-1$
		super.addPage(selectResPage);
		
		selectLocalTargetPage = new SelectLocalTargetPage("local_target_page", Messages.getString("select.target.loc"), //$NON-NLS-1$ //$NON-NLS-2$
											Activator.getImageDescriptor(Activator.IMG_GUVREPWIZBAN));
		selectLocalTargetPage.setDescription(Messages.getString("select.target.loc.desc")); //$NON-NLS-1$
		super.addPage(selectLocalTargetPage);
		
//		selectVerPage = new SelectResourceVersionPage("select_version_page", "Select resource versions",
//											Activator.getImageDescriptor(Activator.IMG_GUVREPWIZBAN));
//		selectVerPage.setDescription("Select the version of the resources to check out");
//		super.addPage(selectVerPage);
		
		super.addPages();
	}
	
	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		if (page.getName().equals("select_rep_page")) { //$NON-NLS-1$
			if (model.shouldCreateNewRep()) {
				return mainConfigPage;
			} else {
				return selectResPage;
			}
		}
		if (page.getName().equals("config_page")) { //$NON-NLS-1$
			return selectResPage;
		}
		if (page.getName().equals("select_res_page")) { //$NON-NLS-1$
			return selectLocalTargetPage;
		}
		return null;
	}
	
	@Override
	public boolean performFinish() {
		try {
			IWebDavClient webdav = WebDavServerCache.getWebDavClient(model.getRepLocation());
			// During the course of the wizard, the user had to drill into a Guvnor repository
			// to choose resources. Therefore, we should have a cached repository connection
			// that is authenticated already. If not, something is really strange.
			assert(webdav != null);
			for (String oneResource:model.getResources()) {
				// Get the metadata properties
				ResourceProperties resprops = webdav.queryProperties(oneResource);
				if (resprops == null) {
					throw new Exception("Null resource properties for " + oneResource); //$NON-NLS-1$
				}
				String contents = webdav.getResourceContents(oneResource);
				IPath targetLocation = new Path(model.getTargetLocation());
				IWorkspaceRoot root = Activator.getDefault().getWorkspace().getRoot();
				IFile targetFile = root.getFile(
										targetLocation.append(
											oneResource.substring(oneResource.lastIndexOf('/'))));
				if (targetFile.exists()) {
					targetFile = resolveNameConflict(targetFile);
				}
				if (targetFile == null) {
					continue;
				}
				ByteArrayInputStream bis = 
							new ByteArrayInputStream(contents.getBytes(targetFile.getCharset()));
				if (targetFile.exists()) {
					targetFile.setContents(bis, true, true, null);
				} else {
					targetFile.create(bis, true, null);
				}
				GuvnorMetadataProps mdProps = new GuvnorMetadataProps(targetFile.getName(), 
						                                             model.getRepLocation(), 
						                                             oneResource, 
						                                             resprops.getLastModifiedDate(),
						                                             resprops.getRevision());
				GuvnorMetadataUtils.setGuvnorMetadataProps(targetFile.getFullPath(), mdProps);
				GuvnorMetadataUtils.markCurrentGuvnorResource(targetFile);
			}
		} catch (Exception e) {
			Activator.getDefault().displayError(IStatus.ERROR, e.getMessage(), e);
		}
		return true;
	}
	
	public IFile resolveNameConflict(IFile conflictingFile) {
		final IWorkspaceRoot root = Activator.getDefault().getWorkspace().getRoot();
		final IPath basePath = conflictingFile.getFullPath().removeLastSegments(1);
		InputDialog dialog = new InputDialog(super.getShell(),
                                            Messages.getString("name.conflict"), //$NON-NLS-1$
                                            MessageFormat.format(Messages.getString("name.conflict.request"), //$NON-NLS-1$
                                            		            new Object[] { conflictingFile.getName() }),
                                            Messages.getString("copy.of") + conflictingFile.getName(), //$NON-NLS-1$
        new IInputValidator() {
			public String isValid(String newText) {
				IFile temp = root.getFile(basePath.append(newText));
				if (temp == null
				   || !temp.exists()) {
					return null;
				} else {
					return MessageFormat.format(Messages.getString("already.exists"), //$NON-NLS-1$
							                   new Object[] { newText });
				}
			}
		});
		if (dialog.open() == InputDialog.OK) {
			return root.getFile(basePath.append(dialog.getValue()));
		} else {
			return null;
		}
	}
	
	public void init(IWorkbench workbench, IStructuredSelection selection) { }

	@Override
	public boolean canFinish() {	
		return model.getRepLocation() != null 
		       && model.getTargetLocation() != null 
		       && model.getResources() != null;
	}
}
