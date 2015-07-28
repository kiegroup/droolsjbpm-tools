package org.kie.eclipse.navigator.view.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.window.Window.IExceptionHandler;

public interface IKieNavigatorAction extends IAction, IExceptionHandler {
	void calculateEnabled();
	void dispose();
}
