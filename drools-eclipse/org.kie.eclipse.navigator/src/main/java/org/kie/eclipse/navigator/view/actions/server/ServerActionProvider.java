package org.kie.eclipse.navigator.view.actions.server;

import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.kie.eclipse.navigator.IKieNavigatorConstants;
import org.kie.eclipse.navigator.view.actions.KieNavigatorActionProvider;
import org.kie.eclipse.navigator.view.actions.ShowPropertiesAction;

public class ServerActionProvider extends KieNavigatorActionProvider implements IKieNavigatorConstants {

	public ServerActionProvider() {
	}

    public void init(ICommonActionExtensionSite aSite) {
        super.init(aSite);
        addAction(new CreateOrganizationAction(aSite.getStructuredViewer()));
        addAction(new ShowPropertiesAction(aSite));
    }
}
