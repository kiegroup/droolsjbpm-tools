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

import org.drools.eclipse.flow.common.view.property.BeanDialogCellEditor;
import org.drools.eclipse.flow.common.view.property.EditBeanDialog;
import org.drools.ruleflow.core.MilestoneNode;
import org.drools.ruleflow.core.RuleFlowProcess;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * Cell editor for milestone constraints.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class MilestoneConstraintCellEditor extends BeanDialogCellEditor {

    private RuleFlowProcess process;
    private MilestoneNode milestone;
    
    public MilestoneConstraintCellEditor(Composite parent, RuleFlowProcess process, MilestoneNode milestone) {
        super(parent);
        this.process = process;
        this.milestone = milestone;
    }

    protected EditBeanDialog createDialog(Shell shell) {
        return new MilestoneConstraintDialog(shell, process, milestone);
    }
    
    protected String getLabelText(Object value) {
    	if (milestone == null || milestone.getConstraint() == null) {
    		return "";
    	}
        return milestone.getConstraint();
    }
}