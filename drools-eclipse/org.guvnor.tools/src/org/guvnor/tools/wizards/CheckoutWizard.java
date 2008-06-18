package org.guvnor.tools.wizards;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.guvnor.tools.Activator;

public class CheckoutWizard extends Wizard implements INewWizard, IGuvnorWizard {
	
	private GuvnorMainConfigPage 			mainConfigPage;
	private SelectGuvnorRepPage 			selectRepPage;
	private SelectGuvnorResourcesPage 		selectResPage;
	private SelectTargetLocationPage 		targetLocationPage;
	private WizardNewProjectCreationPage 	createProjectPage;
	private SelectResourceVersionPage		selectVerPage;
	
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
		
		selectResPage = new SelectGuvnorResourcesPage("select_res_page", "Select Guvnor repository location",
											Activator.getImageDescriptor(Activator.IMG_GUVREPWIZBAN));
		selectResPage.setDescription("Select resources from the Guvnor repository to check out");
		super.addPage(selectResPage);
		
		targetLocationPage = new SelectTargetLocationPage("select_targetloc_page", "Select target location",
				Activator.getImageDescriptor(Activator.IMG_GUVREPWIZBAN));
		targetLocationPage.setDescription("Select the location to check out the resources");
		super.addPage(targetLocationPage);
		
		createProjectPage = new WizardNewProjectCreationPage("new_project_page");
		createProjectPage.setImageDescriptor(Activator.getImageDescriptor(Activator.IMG_GUVREPWIZBAN));
		createProjectPage.setTitle("Create a new project");
		createProjectPage.setDescription("Specify the name of the new project to create");
		super.addPage(createProjectPage);
		
		selectVerPage = new SelectResourceVersionPage("select_version_page", "Select resource versions",
											Activator.getImageDescriptor(Activator.IMG_GUVREPWIZBAN));
		selectVerPage.setDescription("Select the version of the resources to check out");
		super.addPage(selectVerPage);
		
		super.addPages();
	}
	
	@Override
	public boolean performFinish() {
		return true;
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		
	}

	@Override
	public boolean canFinish() {
		return model.isModelComplete();
	}
}
