package org.kie.eclipse.navigator.view.actions;

import org.eclipse.jface.action.IAction;

public interface IKieNavigatorAction extends IAction {
	void calculateEnabled();
	void dispose();
}
