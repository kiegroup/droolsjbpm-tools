package org.kie.eclipse.navigator.view.actions;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

public class ShowPropertiesAction extends PropertyDialogAction implements IKieNavigatorAction {
	
	public ShowPropertiesAction(ICommonActionExtensionSite aSite) {
		super(aSite.getViewSite().getShell(), aSite.getStructuredViewer());
	}
	
	public void calculateEnabled() {
		setEnabled(isEnabled());
	}

	@Override
	public void handleException(Throwable t) {
		t.printStackTrace();
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openError(Display.getDefault().getActiveShell(), "Error", t.getMessage());
			}
		});
	}
}