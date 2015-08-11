package org.kie.eclipse.navigator.view.actions.project;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.ide.undo.MoveProjectOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.internal.progress.ProgressMonitorJobsDialog;
import org.kie.eclipse.navigator.KieNavigatorContentRoot;
import org.kie.eclipse.navigator.view.KieNavigatorContentProvider;
import org.kie.eclipse.navigator.view.KieNavigatorLabelProvider;

public class ConvertToKieProjectAction implements IObjectActionDelegate {
    private IJavaProject project;
	protected IStatus errorStatus;

	public ConvertToKieProjectAction() {
	}

	@Override
	public void run(IAction action) {
        if (project != null && project.exists()) {
    		errorStatus = null;

    		//Get the project name and location 
    		File destination = getDestination(project.getProject());
    		if (destination == null) {
    			return;
    		}
    		
    		boolean completed = performMove(project.getProject(), destination.toURI());

    		if (!completed) {
    			return; // not appropriate to show errors
    		}

    		// If errors occurred, open an Error dialog
    		if (errorStatus != null) {
    			ErrorDialog.openError(getShell(), "Error", null, errorStatus);
    			errorStatus = null;
    		}
        }
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection structured = (IStructuredSelection) selection;
            if (structured.size() == 1) {
                Object element = structured.getFirstElement();
                if (element instanceof IJavaProject) {
                    project = (IJavaProject) element;
                } else if (element instanceof IProject) {
                    IJavaProject javaProject = JavaCore.create((IProject) element);
                    if (javaProject != null && javaProject.exists()) {
                        project = javaProject;
                    }
                }
            }
        }
	}

	@Override
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	File getDestination(IProject project) {
		KieNavigatorContentProvider contentProvider = new KieNavigatorContentProvider();
		KieNavigatorLabelProvider labelProvider = new KieNavigatorLabelProvider();
		KieNavigatorContentRoot root = new KieNavigatorContentRoot(null);
		KieDestinationSelectionDialog dlg = new KieDestinationSelectionDialog(getShell(), labelProvider, contentProvider);
		dlg.setInput(root);
		
		dlg.open();
		return null;
	}
	
	boolean performMove(final IProject project, final URI newLocation) {
		
		IRunnableWithProgress op =  new IRunnableWithProgress() {
    		@Override
			public void run(IProgressMonitor monitor) {
    			MoveProjectOperation op = new MoveProjectOperation(project, newLocation, "Moving Project");
//    			op.setModelProviderIds(getModelProviderIds());
    			try {
    				PlatformUI.getWorkbench().getOperationSupport()
    						.getOperationHistory().execute(op, monitor, 
    								WorkspaceUndoUtil.getUIInfoAdapter(getShell()));
    			} catch (ExecutionException e) {
					if (e.getCause() instanceof CoreException) {
						errorStatus = ((CoreException)e.getCause()).getStatus();
					} else {
						e.printStackTrace();
						displayError(e.getMessage());
					}
    			}
    		}
    	};
		
		try {
			new ProgressMonitorJobsDialog(getShell()).run(true, true, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			// CoreExceptions are collected by the operation, but unexpected runtime
			// exceptions and errors may still occur.
			e.printStackTrace();
			displayError(NLS.bind("Internal error: {0}", e.getTargetException().getMessage()));
			return false;
		}

		return true;
	}
	
	static void displayError(String message) {
		MessageDialog.openError(getShell(), "Error", message);
	}

	static Shell getShell() {
		return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
	}
	
	class KieDestinationSelectionDialog extends ElementTreeSelectionDialog {

		public KieDestinationSelectionDialog(Shell parent, ILabelProvider labelProvider, ITreeContentProvider contentProvider) {
			super(parent, labelProvider, contentProvider);
			this.setAllowMultiple(false);
		}
		
	}
}
