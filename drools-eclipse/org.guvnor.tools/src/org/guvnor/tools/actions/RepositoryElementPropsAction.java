package org.guvnor.tools.actions;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.guvnor.tools.Activator;
import org.guvnor.tools.properties.RepositoryElementPropsDialog;
import org.guvnor.tools.utils.PlatformUtils;
import org.guvnor.tools.views.model.TreeObject;

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
		if (diag.open() == Dialog.OK
		   && diag.wereSecuritySettingModified()) {
			// Have to update security settings for this repository
			try {
				PlatformUtils.getInstance().
					updateAuthentication(node.getGuvnorRepository().getLocation(), 
							            diag.getUsername(), diag.getPassword(), diag.saveAuthenInfo());
			} catch (Exception e) {
				Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
			}
		}
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
