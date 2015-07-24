package org.kie.eclipse.navigator.view.actions;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.navigator.CommonActionProvider;

public class KieNavigatorActionProvider extends CommonActionProvider {

	protected List<IKieNavigatorAction> actions = new ArrayList<IKieNavigatorAction>();
	
	public KieNavigatorActionProvider() {
	}

	protected void addAction(IKieNavigatorAction action) {
		actions.add(action);
	}

    public void fillContextMenu(IMenuManager menu) {
    	for (IKieNavigatorAction action : actions) {
    		action.calculateEnabled();
//    		if (action.isEnabled())
    			menu.add(action);
    	}
    }

    public void dispose() {
    	for (IKieNavigatorAction action : actions) {
        	action.dispose();
        }
    	actions.clear();
        super.dispose();
    }
    
    protected Shell getShell() {
    	return getActionSite().getViewSite().getShell();
    }
}
