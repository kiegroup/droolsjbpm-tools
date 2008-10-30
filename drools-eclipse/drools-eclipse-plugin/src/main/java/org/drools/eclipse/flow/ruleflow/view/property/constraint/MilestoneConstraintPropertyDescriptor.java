package org.drools.eclipse.flow.ruleflow.view.property.constraint;
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


import org.drools.knowledge.definitions.process.WorkflowProcess;
import org.drools.workflow.core.node.MilestoneNode;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * Property descriptor for a milestone constraint.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class MilestoneConstraintPropertyDescriptor extends PropertyDescriptor {
    
    private WorkflowProcess process;
    private MilestoneNode milestone;
    
    public MilestoneConstraintPropertyDescriptor(Object id, String displayName, MilestoneNode milestone, WorkflowProcess process) {
        super(id, displayName);
        this.milestone = milestone;
        this.process = process;
    }
    
    public WorkflowProcess getProcess() {
        return process;
    }
    
    public CellEditor createPropertyEditor(Composite parent) {
    	MilestoneConstraintCellEditor editor = new MilestoneConstraintCellEditor(parent, process, milestone);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }
}
