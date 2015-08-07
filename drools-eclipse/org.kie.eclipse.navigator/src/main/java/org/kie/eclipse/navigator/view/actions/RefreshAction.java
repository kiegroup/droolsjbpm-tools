package org.kie.eclipse.navigator.view.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.actions.RetargetAction;
import org.kie.eclipse.navigator.view.content.IContainerNode;

public class RefreshAction extends RetargetAction {

	ISelectionProvider provider;
	
	public RefreshAction(ISelectionProvider provider) {
		super(ActionFactory.REFRESH.getId(), "Refresh");
		this.provider = provider;
		setActionHandler(new Action() {

			@Override
			public void run() {
				IContainerNode<?> container = getContainer();
				if (container == null)
					return;

				container.clearChildren();
				container.refresh();
			}

			@Override
			public void runWithEvent(Event event) {
				run();
			}

			@Override
			public boolean isEnabled() {
				return true;
			}

			@Override
			public String getToolTipText() {
				return "Reload the Navigator View for this server";
			}
		});
	}
	
	protected IContainerNode<?> getContainer() {
		IStructuredSelection selection = (IStructuredSelection) provider.getSelection();
		if (selection == null || selection.isEmpty()) {
			return null;
		}
		Object element = selection.getFirstElement();
		if (element instanceof IContainerNode)
			return (IContainerNode<?>) element;
		return null;
	}

}