package org.kie.eclipse.navigator.view.actions;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.kie.eclipse.navigator.view.content.IContainerNode;

public class ShowGitRepoViewAction extends KieNavigatorAction {
	
	static String GIT_REPO_VIEW_ID = "org.eclipse.egit.ui.RepositoriesView";

	protected ShowGitRepoViewAction(ISelectionProvider provider, String text) {
		super(provider, text);
	}
	
	public ShowGitRepoViewAction(ISelectionProvider selectionProvider) {
		this(selectionProvider, "Show Git Repository View");
	}

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
			page.showView(GIT_REPO_VIEW_ID, null,  IWorkbenchPage.VIEW_ACTIVATE);
			return;
		}
		catch (Exception e) {}
		return;
    }
}