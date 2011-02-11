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

package org.drools.eclipse.flow.ruleflow.view.property.timers;

import java.util.Map;

import org.drools.eclipse.flow.common.view.property.BeanDialogCellEditor;
import org.drools.eclipse.flow.common.view.property.EditBeanDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.jbpm.process.core.timer.Timer;
import org.jbpm.workflow.core.DroolsAction;
import org.jbpm.workflow.core.WorkflowProcess;
import org.jbpm.workflow.core.node.StateBasedNode;

/**
 * Cell editor for timers.
 */
public class TimersCellEditor extends BeanDialogCellEditor<Map<Timer, DroolsAction>> {

    private WorkflowProcess process;
    private StateBasedNode stateBasedNode;
    
    public TimersCellEditor(Composite parent, WorkflowProcess process, StateBasedNode stateBasedNode) {
        super(parent);
        this.process = process;
        this.stateBasedNode = stateBasedNode;
    }

    protected EditBeanDialog<Map<Timer, DroolsAction>> createDialog(Shell shell) {
        TimersDialog dialog = new TimersDialog(shell, process);
        dialog.setValue(stateBasedNode.getTimers());
        return dialog;
    }
    
    protected String getLabelText(Object value) {
        return "";
    }

}
