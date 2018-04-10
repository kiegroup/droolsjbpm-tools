package org.kie.eclipse.navigator.view.actions.space;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.kie.eclipse.navigator.view.actions.KieNavigatorAction;
import org.kie.eclipse.navigator.view.actions.dialogs.CreateRepositoryRequestDialog;
import org.kie.eclipse.navigator.view.content.IContainerNode;
import org.kie.eclipse.server.IKieSpaceHandler;
import org.kie.eclipse.server.IKieServiceDelegate;
import org.kie.eclipse.server.KieRepositoryHandler;

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
        
        IKieSpaceHandler space = (IKieSpaceHandler) container.getHandler();
        IKieServiceDelegate delegate = getDelegate();

        CreateRepositoryRequestDialog dlg = new CreateRepositoryRequestDialog(Display.getDefault().getActiveShell(), space);
        
        if (dlg.open()== Window.OK){
        	JsonObject properties = dlg.getResult();
        	String name = properties.get("name").asString().trim();
        	KieRepositoryHandler repository = new KieRepositoryHandler(space, name);
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