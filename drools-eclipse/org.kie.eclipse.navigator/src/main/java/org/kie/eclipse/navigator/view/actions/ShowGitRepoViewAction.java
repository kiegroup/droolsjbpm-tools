package org.kie.eclipse.navigator.view.actions;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jgit.lib.Repository;
import org.kie.eclipse.navigator.view.content.IContainerNode;
import org.kie.eclipse.navigator.view.utils.ViewUtils;

public class ShowGitRepoViewAction extends KieNavigatorAction {
	
	static String GIT_REPO_VIEW_ID = "org.eclipse.egit.ui.RepositoriesView";

	protected ShowGitRepoViewAction(ISelectionProvider provider, String text) {
		super(provider, text);
	}
	
	public ShowGitRepoViewAction(ISelectionProvider selectionProvider) {
		this(selectionProvider, "Show in Git Repository View");
	}

	@Override
	public void run() {
        IContainerNode<?> container = getContainer();
        if (container==null)
        	return;

        Repository repository = (Repository) container.getHandler().getResource();
		ViewUtils.showGitRepositoriesView(repository);
		return;
    }
}