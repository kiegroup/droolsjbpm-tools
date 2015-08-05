package org.kie.eclipse.navigator.view.actions;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.internal.WorkbenchMessages;
import org.kie.eclipse.navigator.view.content.IContainerNode;

public class RefreshAction extends KieNavigatorAction {

	final static String REFRESH_ACTION_ID = "org.kie.eclipse.navigator.actions.refresh";

	public RefreshAction(ISelectionProvider selectionProvider) {
		super(selectionProvider, WorkbenchMessages.Workbench_refresh);
		setId(REFRESH_ACTION_ID);
	}

	@Override
	public void run() {
		IContainerNode<?> container = getContainer();
		if (container == null)
			return;

		container.clearChildren();
		refreshViewer(container);
	}

}