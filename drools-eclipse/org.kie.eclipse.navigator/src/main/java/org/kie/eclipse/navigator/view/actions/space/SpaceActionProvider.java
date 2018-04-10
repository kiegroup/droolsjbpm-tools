package org.kie.eclipse.navigator.view.actions.space;

import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.kie.eclipse.navigator.view.actions.KieNavigatorActionProvider;

public class SpaceActionProvider extends KieNavigatorActionProvider {

	public SpaceActionProvider() {
	}

	public void init(final ICommonActionExtensionSite aSite) {
		super.init(aSite);
		addAction(new CreateRepositoryAction(aSite.getStructuredViewer()));
		addAction(new DeleteSpaceAction(aSite.getStructuredViewer()));
	}

}
