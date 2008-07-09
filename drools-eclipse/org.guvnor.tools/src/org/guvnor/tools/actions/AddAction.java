package org.guvnor.tools.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.guvnor.tools.Activator;
import org.guvnor.tools.utils.GuvnorMetadataUtils;
import org.guvnor.tools.utils.PlatformUtils;
import org.guvnor.tools.wizards.AddResourceWizard;

public class AddAction implements IObjectActionDelegate {
	
	private IStructuredSelection sel;
	private IWorkbenchPart targetPart;
	
	/**
	 * Constructor for Action1.
	 */
	public AddAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		assert(targetPart != null && sel != null);
		AddResourceWizard wiz = new AddResourceWizard();
		wiz.init(Activator.getDefault().getWorkbench(), sel);
		WizardDialog dialog = new WizardDialog(targetPart.getSite().getShell(), wiz);
	    dialog.create();
	    if (dialog.open() == WizardDialog.OK) {
	    	PlatformUtils.refreshRepositoryView();
	    }
	}
	
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		// Reset state to default
		sel = null;
		action.setEnabled(false);
		// See if we should enable for the selection
		try {
			if (selection instanceof IStructuredSelection) {
				sel = (IStructuredSelection)selection;
				if (sel.getFirstElement() instanceof IFile) {
					IFile mdFile = GuvnorMetadataUtils.findGuvnorMetadata((IFile)sel.getFirstElement());
					if (mdFile == null) {
						action.setEnabled(true);
					}
				}
			} 
		} catch (Exception e) {
			Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
		}
	}
}
