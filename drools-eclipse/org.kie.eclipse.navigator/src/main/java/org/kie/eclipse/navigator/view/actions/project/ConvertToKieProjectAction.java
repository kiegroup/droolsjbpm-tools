package org.kie.eclipse.navigator.view.actions.project;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.ide.undo.MoveProjectOperation;
import org.eclipse.ui.ide.undo.WorkspaceUndoUtil;
import org.eclipse.ui.internal.progress.ProgressMonitorJobsDialog;
import org.kie.eclipse.navigator.Activator;
import org.kie.eclipse.navigator.KieNavigatorContentRoot;
import org.kie.eclipse.navigator.view.IKieNavigatorView;
import org.kie.eclipse.navigator.view.KieNavigatorContentProvider;
import org.kie.eclipse.navigator.view.KieNavigatorLabelProvider;
import org.kie.eclipse.navigator.view.actions.dialogs.KieRequestDialogTextField;
import org.kie.eclipse.navigator.view.content.IContentNode;
import org.kie.eclipse.navigator.view.content.RepositoryNode;
import org.kie.eclipse.server.IKieProjectHandler;
import org.kie.eclipse.server.IKieRepositoryHandler;
import org.kie.eclipse.server.KieProjectHandler;

import com.eclipsesource.json.JsonObject;

public class ConvertToKieProjectAction implements IObjectActionDelegate {
    private IJavaProject project;
	protected IStatus errorStatus;
	private Shell shell;

	public ConvertToKieProjectAction() {
	}

	@Override
	public void run(IAction action) {
        if (project != null && project.exists()) {
    		errorStatus = null;
    		shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();

    		// Get the project destination location 
    		KieDestinationSelectionDialog dlg = new KieDestinationSelectionDialog(shell, project.getProject());
    		
    		if (dlg.open() != IDialogConstants.OK_ID) {
    			return;
    		}
    		
    		RepositoryNode rn = dlg.getDestination();
    		if (rn == null) {
    			return;
    		}
    		
    		boolean completed = performMove(project.getProject(), rn, dlg.getProperties());

    		if (!completed) {
    			return; // not appropriate to show errors
    		}

    		// If errors occurred, open an Error dialog
    		if (errorStatus != null) {
    			ErrorDialog.openError(shell, "Error", null, errorStatus);
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

	boolean performMove(final IProject project, final RepositoryNode rn, final JsonObject properties) {
		
		IRunnableWithProgress op =  new IRunnableWithProgress() {
    		@Override
			public void run(final IProgressMonitor monitor) {
				Repository repository = (Repository) rn.getHandler().getResource();
				File file = new File(repository.getWorkTree().getAbsolutePath() + File.separator + project.getName());
				URI newLocation = file.toURI();
    			final MoveProjectOperation op = new MoveProjectOperation(project, newLocation, "Moving Project");
    			try {
    				// move the project
    				final IOperationHistory history = PlatformUI.getWorkbench().getOperationSupport().getOperationHistory();
    				final IAdaptable info = WorkspaceUndoUtil.getUIInfoAdapter(shell);
    				history.execute(op, monitor, info);
    				// create the project in the server
					Display.getDefault().syncExec(new Runnable() {
						
						@Override
						public void run() {
		    				try {
			    				IKieProjectHandler kieProject = new KieProjectHandler((IKieRepositoryHandler) rn.getHandler(), project.getName());
			    				kieProject.setProperties(properties);
								rn.getHandler().getDelegate().createProject(kieProject);
							}
							catch (Exception e) {
								e.printStackTrace();
								try {
									history.undoOperation(op, monitor, info);
								}
								catch (ExecutionException e1) {
									e1.printStackTrace();
								}
								displayError(e.getMessage());
							}
						}
					});
    				
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
			new ProgressMonitorJobsDialog(shell).run(true, true, op);
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
	
	void displayError(final String message) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openError(shell, "Error", message);
			}
		});
	}
		
	
	class KieDestinationSelectionDialog extends ElementTreeSelectionDialog implements IKieNavigatorView {

		IProject project;
		
		JsonObject properties = new JsonObject();
		KieRequestDialogTextField description;
		KieRequestDialogTextField groupId;
		KieRequestDialogTextField version;

		public KieDestinationSelectionDialog(Shell parent, final IProject project) {
			super(parent, new KieNavigatorLabelProvider(), new KieNavigatorContentProvider());
			this.project = project;
			setTitle("Convert to Kie Project");
			setMessage("Select a destination Repository for the Project \""+project.getName()+"\"");
			setAllowMultiple(false);
			setInput(new KieNavigatorContentRoot(this));
			setValidator(new ISelectionStatusValidator() {
				
				@Override
				public IStatus validate(Object[] selection) {
					if (selection.length==1 && selection[0] instanceof RepositoryNode) {
						RepositoryNode rn = (RepositoryNode) selection[0];
						rn.load();
						if (rn.getChildren()==null) {
							return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Repository is not available");
						}
						if (!(rn.getHandler().getResource() instanceof Repository)) {
							return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Repository is not available");
						}
						
						for (IContentNode cn : rn.getChildren()) {
							if (cn.getName().equals(project.getName())) {
								return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Repository already has a Project named \""+project.getName()+"\"");
							}
						}
						return new Status(IStatus.OK, Activator.PLUGIN_ID, "");
					}
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Please select a destination Repository");
				}
			});
		}

		@Override
		protected Control createDialogArea(Composite parent) {
			Control control = super.createDialogArea(parent);
			Composite composite = new Composite(parent, SWT.NONE);
			composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			composite.setLayout(new GridLayout(2,false));
			Label label = new Label(composite, SWT.NONE);
			label.setText("Enter Project properties:");
			label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
			properties.add("name", project.getName());
			description = new KieRequestDialogTextField(composite, "Description:", "", properties, "description");
			groupId = new KieRequestDialogTextField(composite, "Group ID:", "", properties, "groupId");
			version = new KieRequestDialogTextField(composite, "Version:", "", properties, "version");

			return control;
		}

		@Override
		public void refresh(Object element) {
			getTreeViewer().refresh(element);
		}

		@Override
		public void setProperty(String key, String value) {
			getTreeViewer().setData(key, value);
		}

		@Override
		public String getProperty(String key) {
			Object value = getTreeViewer().getData(key);
			if (value!=null)
				return value.toString();
			return null;
		}

		public JsonObject getProperties() {
			return properties;
		}

		public RepositoryNode getDestination() {
			Object[] result = getResult();
			if (result.length==1 && result[0] instanceof RepositoryNode) {
				RepositoryNode rn = (RepositoryNode) result[0];
				return rn;
			}
			return null;
		}
	}
}
