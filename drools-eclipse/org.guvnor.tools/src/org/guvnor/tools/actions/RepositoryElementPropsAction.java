package org.guvnor.tools.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.guvnor.tools.properties.RepositoryElementPropsDialog;
import org.guvnor.tools.views.model.TreeObject;

/**
 * Action for showing properties for a Guvnor repository element.
 * @author jgraham
 */
public class RepositoryElementPropsAction implements IViewActionDelegate {

	private TreeObject node;
	
	public void init(IViewPart view) { }

	public void run(IAction action) {
		if (node == null) {
			return;
		}
		
		Shell shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().
									getActivePage().getActivePart().getSite().getShell();
		RepositoryElementPropsDialog diag = new RepositoryElementPropsDialog(shell, node);
		diag.open();
	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection sel = (IStructuredSelection)selection;
			if (sel.getFirstElement() instanceof TreeObject) {
				node = (TreeObject)sel.getFirstElement();
			}
		}
	}
}
