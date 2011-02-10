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

package org.drools.eclipse.flow.ruleflow.view.property.timers;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jbpm.workflow.core.WorkflowProcess;
import org.jbpm.workflow.core.node.StateBasedNode;

public class TimersPropertyDescriptor extends PropertyDescriptor {

    private WorkflowProcess process;
    private StateBasedNode stateBasedNode;

    public TimersPropertyDescriptor(Object id, String displayName, StateBasedNode stateBasedNode, WorkflowProcess process) {
        super(id, displayName);
        this.stateBasedNode = stateBasedNode;
        this.process = process;
    }

    public CellEditor createPropertyEditor(Composite parent) {
        TimersCellEditor editor = new TimersCellEditor(parent, process, stateBasedNode);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }
}
