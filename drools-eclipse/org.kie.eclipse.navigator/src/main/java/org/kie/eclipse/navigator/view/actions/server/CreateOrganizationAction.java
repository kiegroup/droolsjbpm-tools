package org.kie.eclipse.navigator.view.actions.server;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.Window;
import org.kie.eclipse.navigator.view.actions.KieNavigatorAction;
import org.kie.eclipse.navigator.view.actions.dialogs.CreateOrganizationRequestDialog;
import org.kie.eclipse.navigator.view.content.IContainerNode;
import org.kie.eclipse.server.IKieServerHandler;
import org.kie.eclipse.server.IKieServiceDelegate;
import org.kie.eclipse.server.KieOrganizationHandler;

import com.eclipsesource.json.JsonObject;

public class CreateOrganizationAction extends KieNavigatorAction {

	protected CreateOrganizationAction(ISelectionProvider provider, String text) {
		super(provider, text);
	}
	
	public CreateOrganizationAction(ISelectionProvider selectionProvider) {
		this(selectionProvider, "Create Organization...");
	}

	public void run() {
        IContainerNode<?> container = getContainer();
        if (container==null)
        	return;
        
        IKieServerHandler server = (IKieServerHandler) container.getHandler();
        IKieServiceDelegate delegate = getDelegate();
        
        CreateOrganizationRequestDialog dlg = new CreateOrganizationRequestDialog(getShell(), server);
        
        if (dlg.open()==Window.OK) {
        	JsonObject properties = dlg.getResult();
        	String name = properties.get("name").asString().trim();
            KieOrganizationHandler organization = new KieOrganizationHandler(server, name);
            organization.setProperties(properties);
            
            try {
            	delegate.createOrganization(organization);
            	refreshViewer(container);
            }
            catch (Exception e) {
            	handleException(e);
            }
        }
    }
}