package org.kie.eclipse.navigator.view.actions.repository;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.kie.eclipse.navigator.view.actions.KieNavigatorAction;
import org.kie.eclipse.navigator.view.content.ContentNode;
import org.kie.eclipse.navigator.view.content.IContainerNode;
import org.kie.eclipse.navigator.view.content.RepositoryNode;
import org.kie.eclipse.server.IKieResourceHandler;

public class SyncRepositoryAction extends KieNavigatorAction {

	protected SyncRepositoryAction(ISelectionProvider provider, String text) {
		super(provider, text);
	}

	public SyncRepositoryAction(ISelectionProvider selectionProvider) {
		this(selectionProvider, "Synchronize Repository");
	}

	@Override
	public boolean isEnabled() {
		IContainerNode<?> container = getContainer();
		if (container instanceof ContentNode) {
			IKieResourceHandler handler = container.getHandler();
			if (handler == null || !handler.isLoaded())
				return false;
		}
		return true;
	}

	@Override
	public String getToolTipText() {
		return "Synchronize this local Git Repository with the remote server";
	}

	@Override
	public void run() {
		final IContainerNode<?> container = getContainer();
		if (container==null)
			return;

		new PullOperationUI((RepositoryNode) container).start();
	}
}
