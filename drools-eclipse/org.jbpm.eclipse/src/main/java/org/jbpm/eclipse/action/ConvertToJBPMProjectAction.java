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

package org.jbpm.eclipse.action;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.jbpm.eclipse.JBPMEclipsePlugin;
import org.jbpm.eclipse.util.JBPMRuntimeManager;

public class ConvertToJBPMProjectAction implements IObjectActionDelegate {

    private IJavaProject project;
    
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	public void run(IAction action) {
		if (project != null && project.exists()) {
			try {
		    	JBPMRuntimeManager.getDefault().addBuilder(project, null);
			} catch (Throwable t) {
				JBPMEclipsePlugin.log(t);
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
}
