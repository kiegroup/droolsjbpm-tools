package org.drools.eclipse.flow.ruleflow.view.property.action;
/*
 * Copyright 2005 JBoss Inc
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

import java.util.List;

import org.drools.eclipse.flow.common.view.property.BeanDialogCellEditor;
import org.drools.eclipse.flow.common.view.property.EditBeanDialog;
import org.drools.workflow.core.DroolsAction;
import org.drools.workflow.core.WorkflowProcess;
import org.drools.workflow.core.impl.ExtendedNodeImpl;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * Cell editor for on exit actions.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class OnExitActionsCellEditor extends BeanDialogCellEditor<List<DroolsAction>> {

    private WorkflowProcess process;
    private ExtendedNodeImpl extendedNode;
    
    public OnExitActionsCellEditor(Composite parent, WorkflowProcess process, ExtendedNodeImpl extendedNode) {
        super(parent);
        this.process = process;
        this.extendedNode = extendedNode;
    }

    protected EditBeanDialog<List<DroolsAction>> createDialog(Shell shell) {
        ActionsDialog dialog = new ActionsDialog(shell, process);
        dialog.setValue(extendedNode.getActions(ExtendedNodeImpl.EVENT_NODE_EXIT));
        return dialog;
    }
    
    protected String getLabelText(Object value) {
    	return "";
    }
}