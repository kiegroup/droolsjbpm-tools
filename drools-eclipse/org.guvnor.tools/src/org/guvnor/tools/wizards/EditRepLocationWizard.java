package org.guvnor.tools.wizards;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.guvnor.tools.Activator;
import org.guvnor.tools.GuvnorRepository;
import org.guvnor.tools.Messages;
import org.guvnor.tools.utils.PlatformUtils;

/**
 * Wizard page for creating a new Guvnor connection.
 * @author jgraham
 *
 */
public class EditRepLocationWizard extends Wizard implements INewWizard, IGuvnorWizard {
	
	private GuvnorConnectionEditPage mainPage;
	
	private GuvnorRepository rep;
	
	private GuvWizardModel model = new GuvWizardModel();
	
	
	public EditRepLocationWizard(GuvnorRepository rep){
		this.rep = rep;
	}
	
	public GuvWizardModel getModel() {
		return model;
	}
	
	@Override
	public void addPages() {
		mainPage = new GuvnorConnectionEditPage(rep, "config_page", Messages.getString("edit.guvnor.loc"),  //$NON-NLS-1$ //$NON-NLS-2$
										Activator.getImageDescriptor(Activator.IMG_GUVLOCADD));
		mainPage.setDescription(Messages.getString("edit.guvnor.loc.desc")); //$NON-NLS-1$
		super.addPage(mainPage);
		super.addPages();
	}
	
	public boolean canFinish() {
		return mainPage.isPageComplete();
	}
	
	@Override
	public boolean performFinish() {
		if (!(model.getRepLocation().equals(rep.getLocation()))) {

			try {
				WizardUtils.createGuvnorRepository(model);
			} catch (Exception e) {
				Activator.getDefault().displayError(IStatus.ERROR,
						e.getMessage(), e, true);
				return false;
			}

			Activator.getLocationManager().removeRepository(rep.getLocation());
		}
		else{
			
			Activator.getLocationManager().removeRepository(rep.getLocation());
			try {
				WizardUtils.createGuvnorRepository(model);
			} catch (Exception e) {
				Activator.getDefault().displayError(IStatus.ERROR,
						e.getMessage(), e, true);
				return false;
			}

		}
		return true;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) { }
}
