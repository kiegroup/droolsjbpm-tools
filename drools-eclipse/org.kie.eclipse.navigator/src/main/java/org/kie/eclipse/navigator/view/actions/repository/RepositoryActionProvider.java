package org.kie.eclipse.navigator.view.actions.repository;

import org.eclipse.ui.IActionBars;
import org.eclipse.ui.navigator.ICommonActionConstants;
import org.eclipse.ui.navigator.ICommonActionExtensionSite;
import org.kie.eclipse.navigator.view.actions.KieNavigatorActionProvider;
import org.kie.eclipse.navigator.view.actions.ShowGitRepoViewAction;

public class RepositoryActionProvider extends KieNavigatorActionProvider {

	private ImportRepositoryAction importAction;
	
	public RepositoryActionProvider() {
	}

    @Override
	public void fillActionBars(IActionBars actionBars) {
		super.fillActionBars(actionBars);
		if (importAction.isEnabled())
			actionBars.setGlobalActionHandler(ICommonActionConstants.OPEN, importAction);
	}

	@Override
	public void init(ICommonActionExtensionSite aSite) {
		super.init(aSite);
		importAction = new ImportRepositoryAction(aSite.getStructuredViewer());
		addAction(importAction);
//		addAction(new SyncRepositoryAction(aSite.getStructuredViewer()));
		addAction(new CreateProjectAction(aSite.getStructuredViewer()));
		addAction(new DeleteRepositoryAction(aSite.getStructuredViewer()));
        addAction(new ShowGitRepoViewAction(aSite.getStructuredViewer()));
	}
}