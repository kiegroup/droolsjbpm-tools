package org.kie.eclipse.navigator.view.utils;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class ViewUtils {
	public static final String GIT_REPO_VIEW_ID = "org.eclipse.egit.ui.RepositoriesView";
	public static final String SERVERS_VIEW_ID = "org.eclipse.wst.server.ui.ServersView";

	private ViewUtils() {
	}

	public static boolean showServersView() {
		IWorkbench wb = PlatformUI.getWorkbench();
		IWorkbenchWindow win = wb.getActiveWorkbenchWindow();
		if (win == null)
			return false;
		IWorkbenchPage page = win.getActivePage();
		if (page == null)
			return false;
		try {
			page.showView(SERVERS_VIEW_ID, null, IWorkbenchPage.VIEW_CREATE);
			page.showView(SERVERS_VIEW_ID, null, IWorkbenchPage.VIEW_ACTIVATE);
			return true;
		} catch (Exception e) {
			MessageDialog.openError(win.getShell(), "Unable to Open View", "An error occurred whilst trying to open the Servers View\nIs it possible the WST UI is not installed?");
		}
		return false;
	}

	public static void showGitRepositoriesView(Repository repository) {
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
			org.eclipse.egit.ui.internal.repository.tree.RepositoryNode rn =
					new org.eclipse.egit.ui.internal.repository.tree.RepositoryNode(null, repository);
			TreePath tp = new TreePath(new Object[] {rn});
			TreeSelection ts = new TreeSelection(tp);
			site.getSelectionProvider().setSelection(ts);
			return;
		}
		catch (Exception e) {}
	}
}
