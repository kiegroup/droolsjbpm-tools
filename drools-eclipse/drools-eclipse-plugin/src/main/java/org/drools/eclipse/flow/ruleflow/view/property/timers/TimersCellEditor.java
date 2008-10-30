package org.drools.eclipse.flow.ruleflow.view.property.timers;
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

import java.util.Map;

import org.drools.eclipse.flow.common.view.property.BeanDialogCellEditor;
import org.drools.eclipse.flow.common.view.property.EditBeanDialog;
import org.drools.knowledge.definitions.process.WorkflowProcess;
import org.drools.process.core.timer.Timer;
import org.drools.workflow.core.DroolsAction;
import org.drools.workflow.core.node.EventBasedNode;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * Cell editor for timers.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class TimersCellEditor extends BeanDialogCellEditor<Map<Timer, DroolsAction>> {

    private WorkflowProcess process;
    private EventBasedNode eventBasedNode;
    
    public TimersCellEditor(Composite parent, WorkflowProcess process, EventBasedNode eventBasedNode) {
        super(parent);
        this.process = process;
        this.eventBasedNode = eventBasedNode;
    }

    protected EditBeanDialog<Map<Timer, DroolsAction>> createDialog(Shell shell) {
        TimersDialog dialog = new TimersDialog(shell, process);
        dialog.setValue(eventBasedNode.getTimers());
        return dialog;
    }
    
    protected String getLabelText(Object value) {
    	return "";
    }

}