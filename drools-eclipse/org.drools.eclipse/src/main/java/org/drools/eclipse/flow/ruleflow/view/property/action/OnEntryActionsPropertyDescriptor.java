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

package org.drools.eclipse.flow.ruleflow.view.property.action;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jbpm.workflow.core.WorkflowProcess;
import org.jbpm.workflow.core.impl.ExtendedNodeImpl;

public class OnEntryActionsPropertyDescriptor extends PropertyDescriptor {

    private WorkflowProcess process;
    private ExtendedNodeImpl extendedNode;

    public OnEntryActionsPropertyDescriptor(Object id, String displayName, ExtendedNodeImpl extendedNode, WorkflowProcess process) {
        super(id, displayName);
        this.extendedNode = extendedNode;
        this.process = process;
    }

    public CellEditor createPropertyEditor(Composite parent) {
        OnEntryActionsCellEditor editor = new OnEntryActionsCellEditor(parent, process, extendedNode);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }
}
