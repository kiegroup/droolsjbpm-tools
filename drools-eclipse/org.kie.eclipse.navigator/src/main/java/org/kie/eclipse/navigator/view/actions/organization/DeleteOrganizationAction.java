package org.kie.eclipse.navigator.view.actions.organization;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.kie.eclipse.navigator.view.actions.KieNavigatorAction;
import org.kie.eclipse.navigator.view.content.IContainerNode;
import org.kie.eclipse.server.IKieOrganizationHandler;
import org.kie.eclipse.server.IKieServiceDelegate;

public class DeleteOrganizationAction extends KieNavigatorAction {

	public DeleteOrganizationAction(ISelectionProvider provider) {
		super(provider, "Delete Organization...");
	}

	public void run() {
        IContainerNode<?> container = getContainer();
        if (container==null)
        	return;
        
        IKieOrganizationHandler organization = (IKieOrganizationHandler) container.getHandler();
        IKieServiceDelegate delegate = getDelegate();

        boolean doit = MessageDialog.openConfirm(
				getShell(), "Delete Organizational Unit",
				"Are you sure you want to delete the Organizational Unit " + container.getName() + "?");
        
		if (doit) {
            try {
            	delegate.deleteOrganization(organization);
            	refreshViewer(container.getParent());
            }
            catch (Exception e) {
            	handleException(e);
            }
        }
	}
}
