package org.kie.eclipse.navigator.view.actions.repository;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.kie.eclipse.navigator.view.actions.KieNavigatorAction;
import org.kie.eclipse.navigator.view.content.IContainerNode;
import org.kie.eclipse.server.IKieRepositoryHandler;

public class DeleteRepositoryAction extends KieNavigatorAction {

    public DeleteRepositoryAction(final ISelectionProvider selectionProvider) {
        super(selectionProvider, "Delete Repository...");
    }

    public void run() {
        final IContainerNode<?> container = getContainer();
        if (container == null) {
            return;
        }

        final boolean deleteConfirmed = MessageDialog.openConfirm(
                getShell(),
                "Delete Project",
                "Are you sure you want to delete the Repository '" + container.getName() + "'?");

        if (deleteConfirmed) {
            try {
                getDelegate().deleteRepository((IKieRepositoryHandler) container.getHandler(), false);
                refreshViewer(container.getParent());
            } catch (final Exception e) {
                handleException(e);
            }
        }
    }
}