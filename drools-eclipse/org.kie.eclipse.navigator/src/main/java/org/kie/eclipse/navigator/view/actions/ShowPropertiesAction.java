package org.kie.eclipse.navigator.view.actions;

import org.eclipse.ui.dialogs.PropertyDialogAction;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;

public class ShowPropertiesAction extends PropertyDialogAction implements IKieNavigatorAction {
	
	public ShowPropertiesAction(ICommonActionExtensionSite aSite) {
		super(aSite.getViewSite().getShell(), aSite.getStructuredViewer());
	}
	
	public void calculateEnabled() {
		setEnabled(isEnabled());
	}
}