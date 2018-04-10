package org.kie.eclipse.navigator.view.actions.server;

import com.eclipsesource.json.JsonObject;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.Window;
import org.kie.eclipse.navigator.view.actions.KieNavigatorAction;
import org.kie.eclipse.navigator.view.actions.dialogs.CreateSpaceRequestDialog;
import org.kie.eclipse.navigator.view.content.IContainerNode;
import org.kie.eclipse.server.IKieServerHandler;
import org.kie.eclipse.server.IKieServiceDelegate;
import org.kie.eclipse.server.KieSpaceHandler;

public class CreateSpaceAction extends KieNavigatorAction {

	protected CreateSpaceAction(ISelectionProvider provider, String text) {
		super(provider, text);
	}

	public CreateSpaceAction(ISelectionProvider selectionProvider) {
		this(selectionProvider, "Create Space...");
	}

	public void run() {
        IContainerNode<?> container = getContainer();
        if (container==null)
        	return;

        IKieServerHandler server = (IKieServerHandler) container.getHandler();
        IKieServiceDelegate delegate = getDelegate();

        CreateSpaceRequestDialog dlg = new CreateSpaceRequestDialog(getShell(), server);

        if (dlg.open()==Window.OK) {
        	JsonObject properties = dlg.getResult();
        	String name = properties.get("name").asString().trim();
            KieSpaceHandler space = new KieSpaceHandler(server, name);
            space.setProperties(properties);

            try {
            	delegate.createSpace(space);
            	refreshViewer(container);
            }
            catch (Exception e) {
            	handleException(e);
            }
        }
    }
}