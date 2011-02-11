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

package org.drools.eclipse.flow.ruleflow.view.property.action;

import java.util.List;

import org.drools.eclipse.flow.common.view.property.BeanDialogCellEditor;
import org.drools.eclipse.flow.common.view.property.EditBeanDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.jbpm.workflow.core.DroolsAction;
import org.jbpm.workflow.core.WorkflowProcess;
import org.jbpm.workflow.core.impl.ExtendedNodeImpl;

/**
 * Cell editor for on entry actions.
 */
public class OnEntryActionsCellEditor extends BeanDialogCellEditor<List<DroolsAction>> {

    private WorkflowProcess process;
    private ExtendedNodeImpl extendedNode;
    
    public OnEntryActionsCellEditor(Composite parent, WorkflowProcess process, ExtendedNodeImpl extendedNode) {
        super(parent);
        this.process = process;
        this.extendedNode = extendedNode;
    }

    protected EditBeanDialog<List<DroolsAction>> createDialog(Shell shell) {
        ActionsDialog dialog = new ActionsDialog(shell, process);
        dialog.setValue(extendedNode.getActions(ExtendedNodeImpl.EVENT_NODE_ENTER));
        return dialog;
    }
    
    protected String getLabelText(Object value) {
        return "";
    }
}
