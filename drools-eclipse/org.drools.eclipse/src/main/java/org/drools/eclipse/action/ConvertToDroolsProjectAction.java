/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.eclipse.action;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.builder.DroolsBuilder;
import org.drools.eclipse.wizard.project.NewDroolsProjectWizard;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

public class ConvertToDroolsProjectAction implements IObjectActionDelegate {

    private IJavaProject project;
    
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    }

    public void run(IAction action) {
        if (project != null && project.exists()) {
            try {
                addDroolsBuilder(project, null);
                addDroolsLibraries(project, null);
            } catch (Throwable t) {
                DroolsEclipsePlugin.log(t);
            }
        }

    }

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

    public static void addDroolsBuilder(IJavaProject project, IProgressMonitor monitor) throws CoreException {
        IProjectDescription description = project.getProject().getDescription();
        // check whether Drools builder is already part of the project
        ICommand[] commands = description.getBuildSpec();
        for (int i = 0; i < commands.length; i++) {
            if (DroolsBuilder.BUILDER_ID.equals(commands[i].getBuilderName())) {
                return;
            }
        }
        // add Drools builder
        ICommand[] newCommands = new ICommand[commands.length + 1];
        System.arraycopy(commands, 0, newCommands, 0, commands.length);

        ICommand droolsCommand = description.newCommand();
        droolsCommand.setBuilderName(DroolsBuilder.BUILDER_ID);
        newCommands[commands.length] = droolsCommand;
        
        description.setBuildSpec(newCommands);
        project.getProject().setDescription(description, monitor);
    }
    
    public static void addDroolsLibraries(IJavaProject project, IProgressMonitor monitor) throws JavaModelException {
        IClasspathEntry[] classpathEntries = project.getRawClasspath();
        for (int i = 0; i < classpathEntries.length; i++) {
            if (NewDroolsProjectWizard.DROOLS_CLASSPATH_CONTAINER_PATH.equals(classpathEntries[i].getPath().toString())) {
                return;
            }
        }
        NewDroolsProjectWizard.addDroolsLibraries(project, null);
    }
            
}
