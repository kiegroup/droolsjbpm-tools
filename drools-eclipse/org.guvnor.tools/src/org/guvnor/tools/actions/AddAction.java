package org.guvnor.tools.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.guvnor.tools.Activator;
import org.guvnor.tools.utils.ActionUtils;
import org.guvnor.tools.utils.PlatformUtils;
import org.guvnor.tools.wizards.AddResourceWizard;

/**
 * Adds a new resource to Guvnor.
 * @author jgraham
 */
public class AddAction implements IObjectActionDelegate {
	
	private IStructuredSelection selectedItems;
	private IWorkbenchPart targetPart;
	
	/**
	 * Constructor for Action1.
	 */
	public AddAction() {
		super();
	}

	/*
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	/*
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		assert(targetPart != null && selectedItems != null);
		AddResourceWizard wiz = new AddResourceWizard();
		wiz.init(Activator.getDefault().getWorkbench(), selectedItems);
		WizardDialog dialog = new WizardDialog(targetPart.getSite().getShell(), wiz);
	    dialog.create();
	    if (dialog.open() == WizardDialog.OK) {
	    	PlatformUtils.refreshRepositoryView();
	    }
	}
	
	/*
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		boolean validResourceSet = ActionUtils.checkResourceSet(selection, false);
		if (validResourceSet) {
			action.setEnabled(true);
			selectedItems = (IStructuredSelection)selection;
		} else {
			action.setEnabled(false);
			selectedItems = null;
		}
	}
}
