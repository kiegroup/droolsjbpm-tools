package org.kie.eclipse.navigator.view.actions.organization;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.kie.eclipse.navigator.view.actions.KieNavigatorAction;
import org.kie.eclipse.navigator.view.actions.dialogs.CreateRepositoryRequestDialog;
import org.kie.eclipse.navigator.view.content.IContainerNode;
import org.kie.eclipse.navigator.view.server.IKieOrganizationHandler;
import org.kie.eclipse.navigator.view.server.IKieServiceDelegate;
import org.kie.eclipse.navigator.view.server.KieRepositoryHandler;

import com.eclipsesource.json.JsonObject;

public class CreateRepositoryAction extends KieNavigatorAction {

	protected CreateRepositoryAction(ISelectionProvider provider, String text) {
		super(provider, text);
	}
	
	public CreateRepositoryAction(ISelectionProvider selectionProvider) {
		this(selectionProvider, "Create Repository...");
	}

	public void run() {
        IContainerNode<?> container = getContainer();
        if (container==null)
        	return;
        
        IKieOrganizationHandler organization = (IKieOrganizationHandler) container.getHandler();
        IKieServiceDelegate delegate = getDelegate();

        CreateRepositoryRequestDialog dlg = new CreateRepositoryRequestDialog(Display.getDefault().getActiveShell(), organization);
        
        if (dlg.open()== Window.OK){
        	JsonObject properties = dlg.getResult();
        	String name = properties.get("name").asString();
        	KieRepositoryHandler repository = new KieRepositoryHandler(organization, name);
        	repository.setProperties(properties);
            
            try {
            	delegate.createRepository(repository);
            	refreshViewer(container);
            }
            catch (Exception e) {
            	handleException(e);
            }
        }
    }
}