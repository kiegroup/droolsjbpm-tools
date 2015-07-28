package org.kie.eclipse.navigator.view.actions.repository;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.kie.eclipse.navigator.view.actions.KieNavigatorAction;
import org.kie.eclipse.navigator.view.content.IContainerNode;
import org.kie.eclipse.navigator.view.content.RepositoryNode;
import org.kie.eclipse.navigator.view.utils.ActionUtils;
import org.kie.eclipse.server.KieRepositoryHandler;

public class ImportRepositoryAction extends KieNavigatorAction {

	protected ImportRepositoryAction(ISelectionProvider provider, String text) {
		super(provider, text);
	}

	public ImportRepositoryAction(ISelectionProvider selectionProvider) {
		this(selectionProvider, "Import Repository");
	}

	@Override
	public boolean isEnabled() {
		IContainerNode<?> container = getContainer();
		if (container instanceof RepositoryNode) {
			KieRepositoryHandler handler = (KieRepositoryHandler) ((RepositoryNode) container).getHandler();
			if (handler == null || !handler.isLoaded())
				return true;
		}
		return false;
	}

	@Override
	public String getToolTipText() {
		return "Clone this Git Repository from the remote server";
	}

	public void run() {
		final RepositoryNode container = (RepositoryNode) getContainer();
		if (container==null)
			return;
		
		try {
			ActionUtils.importRepository(this, container);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
