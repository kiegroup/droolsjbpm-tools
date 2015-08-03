package org.kie.eclipse.navigator.view.actions;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.kie.eclipse.navigator.view.content.IContainerNode;

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
        
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		if (win==null)
			return;
		IWorkbenchPage page = win.getActivePage();
		if (page==null)
			return;
		try {
			page.showView(GIT_REPO_VIEW_ID, null, IWorkbenchPage.VIEW_CREATE);
			IViewPart part = page.showView(GIT_REPO_VIEW_ID, null,  IWorkbenchPage.VIEW_ACTIVATE);
			IWorkbenchPartSite site = part.getSite();
			Repository repository = (Repository) container.getHandler().getResource();
			org.eclipse.egit.ui.internal.repository.tree.RepositoryNode rn =
					new org.eclipse.egit.ui.internal.repository.tree.RepositoryNode(null, repository);
			TreePath tp = new TreePath(new Object[] {rn});
			TreeSelection ts = new TreeSelection(tp);
			site.getSelectionProvider().setSelection(ts);
			return;
		}
		catch (Exception e) {}
		return;
    }
}