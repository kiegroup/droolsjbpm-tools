package org.guvnor.tools.actions;

import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.guvnor.tools.Activator;
import org.guvnor.tools.views.model.TreeObject;

/**
 * Opens the Guvnor web console in a browser instance.
 * Defaults to an internal Eclipse web browser if available.
 * @author jgraham
 */
public class OpenGuvnorWebConsoleAction implements IObjectActionDelegate {
	
	private TreeObject selectedNode;
	
	public OpenGuvnorWebConsoleAction() {
		super();
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) { }

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		if (selectedNode == null) {
			return;
		} 
		IWorkbenchBrowserSupport browserSupport = 
			Activator.getDefault().getWorkbench().getBrowserSupport();
		try {
			URL consoleURL = new URL(
					extractGuvnorConsoleUrl(selectedNode.getGuvnorRepository().getLocation()));
			if (browserSupport.isInternalWebBrowserAvailable()) {
				browserSupport.createBrowser(null).openURL(consoleURL);
			} else {
				browserSupport.getExternalBrowser().openURL(consoleURL);
			}
		} catch (Exception e) {
			Activator.getDefault().displayError(IStatus.ERROR, e.getMessage(), e, true);
		}
	}
	
	private String extractGuvnorConsoleUrl(String guvnorLoc) {
		String id = "/webdav"; //$NON-NLS-1$
		int pos = guvnorLoc.indexOf(id);
		if (pos == -1) {
			 return guvnorLoc + "/Guvnor.html"; //$NON-NLS-1$;
		}
		return guvnorLoc.substring(0, pos) + "/Guvnor.html"; //$NON-NLS-1$
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		action.setEnabled(false);

		if (!(selection instanceof IStructuredSelection)) {
			return;
		}
		
		IStructuredSelection sel = (IStructuredSelection)selection;
		if (sel.size() != 1) {
			return;
		}
		
		if (sel.getFirstElement() instanceof TreeObject) {
			if (((TreeObject)sel.getFirstElement()).getNodeType() == TreeObject.Type.REPOSITORY) {
				selectedNode = (TreeObject)sel.getFirstElement();
				action.setEnabled(true);
			}
		}
	}
}
