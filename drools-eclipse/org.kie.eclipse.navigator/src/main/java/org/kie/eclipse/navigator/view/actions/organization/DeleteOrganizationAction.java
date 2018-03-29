package org.kie.eclipse.navigator.view.actions.organization;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.kie.eclipse.navigator.view.actions.KieNavigatorAction;
import org.kie.eclipse.navigator.view.content.IContainerNode;
import org.kie.eclipse.server.IKieOrganizationHandler;

public class DeleteOrganizationAction extends KieNavigatorAction {

    public DeleteOrganizationAction(final ISelectionProvider provider) {
        super(provider, "Delete Organization...");
    }

    public void run() {
        final IContainerNode<?> container = getContainer();
        if (container == null) {
            return;
        }

        final boolean deleteConfirmed = MessageDialog.openConfirm(
                getShell(),
                "Delete Organizational Unit",
                "Are you sure you want to delete the Organizational Unit '" + container.getName() + "'?");

        if (deleteConfirmed) {
            try {
                getDelegate().deleteOrganization((IKieOrganizationHandler) container.getHandler());
                refreshViewer(container.getParent());
            } catch (final Exception e) {
                handleException(e);
            }
        }
    }
}
