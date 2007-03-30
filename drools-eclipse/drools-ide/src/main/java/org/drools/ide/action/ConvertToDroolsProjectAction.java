package org.drools.ide.action;

import org.drools.ide.DroolsIDEPlugin;
import org.drools.ide.builder.DroolsBuilder;
import org.drools.ide.wizard.project.NewDroolsProjectWizard;
import org.drools.util.ArrayUtils;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class ConvertToDroolsProjectAction implements IObjectActionDelegate {

    private IFile file;
    
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	public void run(IAction action) {
		IJavaProject javaProject = JavaCore.create(file.getProject());
		if (javaProject != null && javaProject.exists()) {
			try {
				addDroolsBuilder(javaProject, null);
				NewDroolsProjectWizard.addDroolsLibraries(javaProject, null);
			} catch (Throwable t) {
				DroolsIDEPlugin.log(t);
			}
		}

	}

	public void selectionChanged(IAction action, ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			IStructuredSelection structured = (IStructuredSelection) selection;
			if (structured.size() == 1) {
				Object element = structured.getFirstElement();
				if (element instanceof IFile) {
					file = (IFile) element;
				}
			}
		}
	}
	
    public static void addDroolsBuilder(IJavaProject project, IProgressMonitor monitor) throws CoreException {
        IProjectDescription description = project.getProject().getDescription();
        ICommand[] commands = description.getBuildSpec();
        if (!ArrayUtils.contains(commands, DroolsBuilder.BUILDER_ID)) {
	        ICommand[] newCommands = new ICommand[commands.length + 1];
	        System.arraycopy(commands, 0, newCommands, 0, commands.length);
	
	        ICommand droolsCommand = description.newCommand();
	        droolsCommand.setBuilderName(DroolsBuilder.BUILDER_ID);
	        newCommands[commands.length] = droolsCommand;
	        
	        description.setBuildSpec(newCommands);
	        project.getProject().setDescription(description, monitor);
        }
    }
            
}
