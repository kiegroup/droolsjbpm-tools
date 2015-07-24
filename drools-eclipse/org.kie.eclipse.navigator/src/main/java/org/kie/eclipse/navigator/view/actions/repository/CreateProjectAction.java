package org.kie.eclipse.navigator.view.actions.repository;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.wizards.IWizardDescriptor;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.kie.eclipse.navigator.view.actions.KieNavigatorAction;
import org.kie.eclipse.navigator.view.actions.dialogs.CreateProjectRequestDialog;
import org.kie.eclipse.navigator.view.content.ContentNode;
import org.kie.eclipse.navigator.view.content.IContainerNode;
import org.kie.eclipse.navigator.view.server.IKieProjectHandler;
import org.kie.eclipse.navigator.view.server.IKieRepositoryHandler;
import org.kie.eclipse.navigator.view.server.IKieServiceDelegate;
import org.kie.eclipse.navigator.view.server.KieProjectHandler;
import org.kie.eclipse.navigator.view.server.KieRepositoryHandler;

import com.eclipsesource.json.JsonObject;

public class CreateProjectAction extends KieNavigatorAction {

	protected CreateProjectAction(ISelectionProvider provider, String text) {
		super(provider, text);
	}

	public CreateProjectAction(ISelectionProvider selectionProvider) {
		this(selectionProvider, "Create Project...");
	}

	@Override
	public String getToolTipText() {
		return "Create a new Project with the New Drools Project Wizard";
	}

	@Override
	public boolean isEnabled() {
		IContainerNode<?> container = getContainer();
		if (container instanceof ContentNode) {
			KieRepositoryHandler handler = (KieRepositoryHandler) ((ContentNode) container).getHandler();
			if (handler == null || !handler.isLoaded())
				return false;
		}
		return true;
	}

	public void run() {
		IContainerNode<?> container = getContainer();
		if (container==null)
			return;
		
		IKieRepositoryHandler repository = (IKieRepositoryHandler) container.getHandler();
        IKieServiceDelegate delegate = getDelegate();
        
        CreateProjectRequestDialog dlg = new CreateProjectRequestDialog(getShell(), repository);
        
        if (dlg.open()==Window.OK) {
        	JsonObject properties = dlg.getResult();
        	String name = properties.get("name").asString();
            IKieProjectHandler project = new KieProjectHandler(repository, name);
            project.setProperties(properties);
	        try {
	        	delegate.createProject(project);

	        	refreshViewer(container.getParent());

	            if (dlg.shouldStartProjectWizard()) {
		    		BasicNewResourceWizard wizard = (BasicNewResourceWizard) createWizard("org.drools.eclipse.wizards.new.project");
		    		wizard.init(PlatformUI.getWorkbench(), getStructuredSelection());
		    		WizardDialog wd = new WizardDialog(Display.getDefault().getActiveShell(), wizard);
		    		wd.setTitle(wizard.getWindowTitle());
		    		int rtn = wd.open();
		    		System.out.println(rtn);
	            }
	        }
	        catch (Exception e) {
	        	handleException(e);
	        }
        }
	}

	public IWizard createWizard(String id) {
		// First see if this is a "new wizard".
		IWizardDescriptor descriptor = PlatformUI.getWorkbench().getNewWizardRegistry().findWizard(id);
		// If not check if it is an "import wizard".
		if (descriptor == null) {
			descriptor = PlatformUI.getWorkbench().getImportWizardRegistry().findWizard(id);
		}
		// Or maybe an export wizard
		if (descriptor == null) {
			descriptor = PlatformUI.getWorkbench().getExportWizardRegistry().findWizard(id);
		}
		try {
			// Then if we have a wizard, open it.
			if (descriptor != null) {
				IWizard wizard = descriptor.createWizard();
				return wizard;
			}
		}
		catch (CoreException e) {
			e.printStackTrace();
		}
		return null;
	}
}