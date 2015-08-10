package org.kie.eclipse.navigator.view.actions.organization;

import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.kie.eclipse.navigator.view.actions.KieNavigatorActionProvider;
import org.kie.eclipse.navigator.view.actions.ShowPropertiesAction;

public class OrganizationActionProvider extends KieNavigatorActionProvider {

	public OrganizationActionProvider() {
	}

	public void init(ICommonActionExtensionSite aSite) {
		super.init(aSite);
		addAction(new AddRepositoryAction(aSite.getStructuredViewer()));
		addAction(new CreateRepositoryAction(aSite.getStructuredViewer()));
		addAction(new DeleteOrganizationAction(aSite.getStructuredViewer()));
//        addAction(new ShowPropertiesAction(aSite));
	}

}
