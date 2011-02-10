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

package org.drools.eclipse.flow.ruleflow.core;

import java.util.List;

import org.drools.eclipse.flow.ruleflow.view.property.action.OnEntryActionsPropertyDescriptor;
import org.drools.eclipse.flow.ruleflow.view.property.action.OnExitActionsPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.jbpm.workflow.core.DroolsAction;
import org.jbpm.workflow.core.WorkflowProcess;
import org.jbpm.workflow.core.impl.ExtendedNodeImpl;

public class ExtendedNodeWrapper extends AbstractNodeWrapper {

    public static final String ON_ENTRY_ACTIONS = "OnEntryActions";
    public static final String ON_EXIT_ACTIONS = "OnExitActions";

    private static final long serialVersionUID = 510l;

    protected IPropertyDescriptor getOnEntryPropertyDescriptor() {
        return new OnEntryActionsPropertyDescriptor(
            ON_ENTRY_ACTIONS, "On Entry Actions", getExtendedNode(),
            (WorkflowProcess) getParent().getProcessWrapper().getProcess());
    }
    
    protected IPropertyDescriptor getOnExitPropertyDescriptor() {
        return new OnExitActionsPropertyDescriptor(
            ON_EXIT_ACTIONS, "On Exit Actions", getExtendedNode(),
            (WorkflowProcess) getParent().getProcessWrapper().getProcess());
    }
    
    public ExtendedNodeImpl getExtendedNode() {
        return (ExtendedNodeImpl) getNode();
    }
    
    public Object getPropertyValue(Object id) {
        if (ON_ENTRY_ACTIONS.equals(id)) {
            return getExtendedNode().getActions(ExtendedNodeImpl.EVENT_NODE_ENTER);
        }
        if (ON_EXIT_ACTIONS.equals(id)) {
            return getExtendedNode().getActions(ExtendedNodeImpl.EVENT_NODE_EXIT);
        }
        return super.getPropertyValue(id);
    }

    public void resetPropertyValue(Object id) {
        if (ON_ENTRY_ACTIONS.equals(id)) {
            getExtendedNode().setActions(ExtendedNodeImpl.EVENT_NODE_ENTER, null);
        } else if (ON_EXIT_ACTIONS.equals(id)) {
            getExtendedNode().setActions(ExtendedNodeImpl.EVENT_NODE_EXIT, null);
        } else {
            super.resetPropertyValue(id);
        }
    }

    @SuppressWarnings("unchecked")
    public void setPropertyValue(Object id, Object value) {
        if (ON_ENTRY_ACTIONS.equals(id)) {
            getExtendedNode().setActions(ExtendedNodeImpl.EVENT_NODE_ENTER, (List<DroolsAction>) value);
        } else if (ON_EXIT_ACTIONS.equals(id)) {
            getExtendedNode().setActions(ExtendedNodeImpl.EVENT_NODE_EXIT, (List<DroolsAction>) value);
        } else {
            super.setPropertyValue(id, value);
        }
    }
    
}
