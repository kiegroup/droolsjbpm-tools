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

package org.drools.eclipse.debug;

import java.util.ArrayList;
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.launching.IJavaLaunchConfigurationConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;

/**
 * The Process instances view.
 */
public class ProcessInstancesView extends DroolsDebugEventHandlerView {

    public ProcessInstancesView() {
        setAction(DOUBLE_CLICK_ACTION, new DoubleClickAction());
    }
    
    protected IContentProvider createContentProvider() {
        ProcessInstancesViewContentProvider contentProvider = new ProcessInstancesViewContentProvider(this);
        return contentProvider;
    }
    
    private class DoubleClickAction extends Action {
        public void run() {
            IViewPart view = getSite().getPage().findView("org.drools.eclipse.debug.ProcessInstanceViewer");
            if (view == null) {
                try {
                    view = getSite().getPage().showView("org.drools.eclipse.debug.ProcessInstanceViewer");
                } catch (PartInitException e) {
                    DroolsEclipsePlugin.log(e);
                }
            }
            if (view != null) {
                ISelection selection = getViewer().getSelection();
                if (selection instanceof StructuredSelection) {
                    Object selected = ((StructuredSelection) selection).getFirstElement();
                    if (selected instanceof IJavaVariable) {
                        try {
                            openProcessInstance(((IJavaVariable) selected).getValue(), view);
                        } catch (DebugException e) {
                            DroolsEclipsePlugin.log(e);
                        }
                    }
                }
            }
        }
        
        private void openProcessInstance(IValue processInstance, IViewPart view) throws DebugException {
            String id = null;
            String processId = null;
            IVariable[] vars = processInstance.getVariables();
            for ( int j = 0; j < vars.length; j++ ) {
                IVariable var = vars[j];
                if ("id".equals(var.getName())) {
                    id = ((IJavaValue)var.getValue()).getValueString();
                } else if ("process".equals(var.getName())) {
                    IJavaValue process = (IJavaValue) var.getValue();
                    IVariable[] vars2 = process.getVariables();
                    for ( int k = 0; k < vars2.length; k++ ) {
                        IVariable var2 = vars2[k];
                        if ("id".equals(var2.getName())) {
                            processId = ((IJavaValue) var2.getValue()).getValueString();
                            break;
                        }
                    }
                }
            }
            List<String> nodeIds = new ArrayList<String>();
            IJavaArray nodeInstances = (IJavaArray) DebugUtil.getValueByExpression("return getNodeInstances().toArray();", processInstance);
            IJavaValue[] javaVals = nodeInstances.getValues();
            for ( int i = 0; i < javaVals.length; i++ ) {
                IJavaValue nodeInstance = javaVals[i];
                String nodeId = null;
                vars = nodeInstance.getVariables();
                for ( int j = 0; j < vars.length; j++ ) {
                    IVariable var = vars[j];
                    if ("nodeId".equals(var.getName())) {
                        nodeId = ((IJavaValue) var.getValue()).getValueString();
                        break;
                    }
                }
                nodeIds.add(nodeId);
            }
            String projectName = null;
            try {
                projectName = processInstance.getLaunch().getLaunchConfiguration().getAttribute(
                    IJavaLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
            } catch (CoreException e) {
                DroolsEclipsePlugin.log(e);
            }
            ((ProcessInstanceViewer) view).showProcessInstance(id, processId, nodeIds, projectName);
        }
    }
}
