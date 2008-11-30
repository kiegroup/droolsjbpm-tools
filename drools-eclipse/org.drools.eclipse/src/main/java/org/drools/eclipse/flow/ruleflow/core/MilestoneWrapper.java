package org.drools.eclipse.flow.ruleflow.core;
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

import org.drools.eclipse.flow.common.editor.core.ElementConnection;
import org.drools.eclipse.flow.common.editor.core.ElementWrapper;
import org.drools.eclipse.flow.ruleflow.view.property.constraint.MilestoneConstraintPropertyDescriptor;
import org.drools.workflow.core.WorkflowProcess;
import org.drools.workflow.core.node.MilestoneNode;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

/**
 * Wrapper for a milestone node.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class MilestoneWrapper extends EventBasedNodeWrapper {

    public static final String CONSTRAINT = "Constraint";

	private static final long serialVersionUID = 4L;

    public MilestoneWrapper() {
        setNode(new MilestoneNode());
        getMilestoneNode().setName("Event Wait");
    }
    
    protected void initDescriptors() {
    	super.initDescriptors();
    	IPropertyDescriptor[] oldDescriptors = descriptors; 
        descriptors = new IPropertyDescriptor[oldDescriptors.length + 3];
        System.arraycopy(oldDescriptors, 0, descriptors, 0, oldDescriptors.length);
        descriptors[descriptors.length - 3] = getOnEntryPropertyDescriptor();
        descriptors[descriptors.length - 2] = getOnExitPropertyDescriptor();
        descriptors[descriptors.length - 1] = 
            new MilestoneConstraintPropertyDescriptor(CONSTRAINT, "Constraint",
        		getMilestoneNode(), (WorkflowProcess) getParent().getProcessWrapper().getProcess());
    }
    
    public MilestoneNode getMilestoneNode() {
        return (MilestoneNode) getNode();
    }
    
    public boolean acceptsIncomingConnection(ElementConnection connection, ElementWrapper source) {
        return super.acceptsIncomingConnection(connection, source)
        	&& getIncomingConnections().isEmpty();
    }

    public boolean acceptsOutgoingConnection(ElementConnection connection, ElementWrapper target) {
        return super.acceptsOutgoingConnection(connection, target)
        	&& getOutgoingConnections().isEmpty();
    }
    
    public Object getPropertyValue(Object id) {
        if (CONSTRAINT.equals(id)) {
        	String constraint = getMilestoneNode().getConstraint();
            return constraint == null ? "" : constraint;
        }
        return super.getPropertyValue(id);
    }

    public void resetPropertyValue(Object id) {
        if (CONSTRAINT.equals(id)) {
        	getMilestoneNode().setConstraint("");
        } else {
            super.resetPropertyValue(id);
        }
    }

    public void setPropertyValue(Object id, Object value) {
        if (CONSTRAINT.equals(id)) {
        	getMilestoneNode().setConstraint((String) value);
        } else {
            super.setPropertyValue(id, value);
        }
    }
}
