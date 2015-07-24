package org.kie.eclipse.navigator.view.actions.repository;

import java.io.IOException;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.kie.eclipse.navigator.view.actions.KieNavigatorAction;
import org.kie.eclipse.navigator.view.content.IContainerNode;
import org.kie.eclipse.navigator.view.server.IKieRepositoryHandler;
import org.kie.eclipse.navigator.view.server.IKieServiceDelegate;

public class DeleteRepositoryAction extends KieNavigatorAction {

	protected DeleteRepositoryAction(ISelectionProvider provider, String text) {
		super(provider, text);
	}

	public DeleteRepositoryAction(ISelectionProvider selectionProvider) {
		this(selectionProvider, "Remove Repository...");
	}

	@Override
	public String getToolTipText() {
		return "Remove this Git Repository from the Organizational Unit and optionally delete the Repository";
	}

	public void run() {
		IContainerNode<?> container = getContainer();
		if (container==null)
			return;
		
		MessageDialogWithToggle dlg = MessageDialogWithToggle.openYesNoQuestion(getShell(), "Remove Repository",
				"Are you sure you want to remove the Repository " + container.getName() + " from the Organizational Unit "
						+ container.getParent().getName() + "?", "Also delete the Repository completely from the Server.",
				false, null, null);
		if (dlg.getReturnCode() == IDialogConstants.YES_ID) {
			IKieServiceDelegate delegate = getDelegate();
			
			try {
				delegate.deleteRepository((IKieRepositoryHandler) container.getHandler(), !dlg.getToggleState());
            	refreshViewer(container.getRoot());
			}
			catch (IOException e) {
				handleException(e);
			}
		}
	}
}