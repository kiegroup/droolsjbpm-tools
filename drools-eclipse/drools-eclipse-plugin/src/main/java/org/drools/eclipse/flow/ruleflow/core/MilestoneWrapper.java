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

import org.drools.eclipse.flow.common.editor.core.DefaultElementWrapper;
import org.drools.eclipse.flow.common.editor.core.ElementConnection;
import org.drools.ruleflow.core.MilestoneNode;
import org.drools.ruleflow.core.impl.MilestoneNodeImpl;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * Wrapper for a milestone node.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class MilestoneWrapper extends NodeWrapper {

	private static final long serialVersionUID = -5976489437109982927L;
	private static IPropertyDescriptor[] descriptors;

    public static final String CONSTRAINT = "Constraint";
    static {
        descriptors = new IPropertyDescriptor[DefaultElementWrapper.descriptors.length + 1];
        System.arraycopy(DefaultElementWrapper.descriptors, 0, descriptors, 0, DefaultElementWrapper.descriptors.length);
        descriptors[descriptors.length - 1] = 
            new TextPropertyDescriptor(CONSTRAINT, "Constraint");
    }

    public MilestoneWrapper() {
        setNode(new MilestoneNodeImpl());
        getMilestoneNode().setName("Milestone");
    }
    
    public MilestoneNode getMilestoneNode() {
        return (MilestoneNode) getNode();
    }
    
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return descriptors;
    }

    public boolean acceptsIncomingConnection(ElementConnection connection) {
        return getIncomingConnections().isEmpty();
    }

    public boolean acceptsOutgoingConnection(ElementConnection connection) {
        return getOutgoingConnections().isEmpty();
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
