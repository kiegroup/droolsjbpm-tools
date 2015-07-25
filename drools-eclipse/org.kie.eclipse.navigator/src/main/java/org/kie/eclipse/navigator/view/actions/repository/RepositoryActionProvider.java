package org.kie.eclipse.navigator.view.actions.repository;

import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.kie.eclipse.navigator.view.actions.KieNavigatorActionProvider;
import org.kie.eclipse.navigator.view.actions.ShowGitRepoViewAction;
import org.kie.eclipse.navigator.view.actions.ShowPropertiesAction;

public class RepositoryActionProvider extends KieNavigatorActionProvider {

	public RepositoryActionProvider() {
	}

	public void init(ICommonActionExtensionSite aSite) {
		super.init(aSite);
		addAction(new ImportRepositoryAction(aSite.getStructuredViewer()));
		addAction(new SyncRepositoryAction(aSite.getStructuredViewer()));
		addAction(new CreateProjectAction(aSite.getStructuredViewer()));
		addAction(new DeleteRepositoryAction(aSite.getStructuredViewer()));
        addAction(new ShowGitRepoViewAction(aSite.getStructuredViewer()));
        addAction(new ShowPropertiesAction(aSite));
	}
}