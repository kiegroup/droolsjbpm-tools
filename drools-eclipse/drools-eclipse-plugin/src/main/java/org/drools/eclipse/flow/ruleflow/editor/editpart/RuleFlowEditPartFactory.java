package org.drools.eclipse.flow.ruleflow.editor.editpart;
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

import org.drools.eclipse.flow.common.editor.editpart.ElementContainerEditPart;
import org.drools.eclipse.flow.common.editor.editpart.ProcessEditPart;
import org.drools.eclipse.flow.common.editor.editpart.ProcessEditPartFactory;
import org.drools.eclipse.flow.ruleflow.core.ActionWrapper;
import org.drools.eclipse.flow.ruleflow.core.CompositeNodeWrapper;
import org.drools.eclipse.flow.ruleflow.core.ConnectionWrapper;
import org.drools.eclipse.flow.ruleflow.core.EndNodeWrapper;
import org.drools.eclipse.flow.ruleflow.core.EventNodeWrapper;
import org.drools.eclipse.flow.ruleflow.core.ForEachNodeWrapper;
import org.drools.eclipse.flow.ruleflow.core.JoinWrapper;
import org.drools.eclipse.flow.ruleflow.core.MilestoneWrapper;
import org.drools.eclipse.flow.ruleflow.core.RuleFlowProcessWrapper;
import org.drools.eclipse.flow.ruleflow.core.RuleSetNodeWrapper;
import org.drools.eclipse.flow.ruleflow.core.SplitWrapper;
import org.drools.eclipse.flow.ruleflow.core.StartNodeWrapper;
import org.drools.eclipse.flow.ruleflow.core.SubProcessWrapper;
import org.drools.eclipse.flow.ruleflow.core.TimerWrapper;
import org.drools.eclipse.flow.ruleflow.core.WorkItemWrapper;
import org.eclipse.gef.EditPart;
import org.eclipse.jdt.core.IJavaProject;

/**
 * Factory for RuleFlow EditParts.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class RuleFlowEditPartFactory implements ProcessEditPartFactory {
    
    private IJavaProject project;
    
    public void setProject(IJavaProject project) {
        this.project = project;
    }
    
    public EditPart createEditPart(EditPart context, Object model) {
        EditPart result = null;
        if (model instanceof RuleFlowProcessWrapper) {
            result = new ProcessEditPart();
        } else if (model instanceof RuleSetNodeWrapper) {
            result = new RuleSetNodeEditPart();
        } else if (model instanceof ConnectionWrapper) {
            result = new ConnectionWrapperEditPart();
        } else if (model instanceof StartNodeWrapper) {
            result = new StartNodeEditPart();
        } else if (model instanceof EndNodeWrapper) {
            result = new EndNodeEditPart();
        } else if (model instanceof SplitWrapper) {
            result = new SplitEditPart();
        } else if (model instanceof JoinWrapper) {
            result = new JoinEditPart();
        } else if (model instanceof MilestoneWrapper) {
            result = new MilestoneEditPart();
        } else if (model instanceof SubProcessWrapper) {
            result = new SubFlowEditPart();
        } else if (model instanceof ActionWrapper) {
            result = new ActionEditPart();
        } else if (model instanceof WorkItemWrapper) {
            result = new WorkItemEditPart();
            ((WorkItemEditPart) result).setProject(project);
        } else if (model instanceof TimerWrapper) {
            result = new TimerEditPart();
        } else if (model instanceof ForEachNodeWrapper) {
            result = new ForEachNodeEditPart();
        } else if (model instanceof CompositeNodeWrapper) {
            result = new ElementContainerEditPart();
        } else if (model instanceof EventNodeWrapper) {
            result = new EventNodeEditPart();
        } else {
            throw new IllegalArgumentException(
                "Unknown model object " + model);
        }
        result.setModel(model);
        return result;
    }

}
