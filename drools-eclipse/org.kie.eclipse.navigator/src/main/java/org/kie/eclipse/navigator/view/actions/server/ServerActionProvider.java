package org.kie.eclipse.navigator.view.actions.server;

import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.kie.eclipse.IKieConstants;
import org.kie.eclipse.navigator.view.actions.KieNavigatorActionProvider;

public class ServerActionProvider extends KieNavigatorActionProvider implements IKieConstants {

	public ServerActionProvider() {
	}

    @Override
	public void init(ICommonActionExtensionSite aSite) {
        super.init(aSite);
        addAction(new CreateSpaceAction(aSite.getStructuredViewer()));
//        addAction(new ShowPropertiesAction(aSite));
    }
}
