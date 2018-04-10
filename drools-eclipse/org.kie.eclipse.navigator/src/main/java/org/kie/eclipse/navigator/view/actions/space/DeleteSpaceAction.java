package org.kie.eclipse.navigator.view.actions.space;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.kie.eclipse.navigator.view.actions.KieNavigatorAction;
import org.kie.eclipse.navigator.view.content.IContainerNode;
import org.kie.eclipse.server.IKieSpaceHandler;

public class DeleteSpaceAction extends KieNavigatorAction {

    public DeleteSpaceAction(final ISelectionProvider provider) {
        super(provider, "Delete Space...");
    }

    public void run() {
        final IContainerNode<?> container = getContainer();
        if (container == null) {
            return;
        }

        final boolean deleteConfirmed = MessageDialog.openConfirm(
                getShell(),
                "Delete Space",
                "Are you sure you want to delete the Space '" + container.getName() + "'?");

        if (deleteConfirmed) {
            try {
                getDelegate().deleteSpace((IKieSpaceHandler) container.getHandler());
                refreshViewer(container.getParent());
            } catch (final Exception e) {
                handleException(e);
            }
        }
    }
}
