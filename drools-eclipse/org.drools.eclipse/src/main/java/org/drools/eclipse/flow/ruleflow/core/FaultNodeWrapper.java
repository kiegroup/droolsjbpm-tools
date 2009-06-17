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
import org.drools.eclipse.flow.common.editor.core.ElementWrapper;
import org.drools.workflow.core.node.FaultNode;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * Wrapper for a fault node.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class FaultNodeWrapper extends AbstractNodeWrapper {

    public static final String FAULT_NAME = "FaultName";
    public static final String FAULT_VARIABLE = "FaultVariable";

	private static final long serialVersionUID = 4L;
	private static IPropertyDescriptor[] descriptors;
	static {
		descriptors = new IPropertyDescriptor[DefaultElementWrapper.DESCRIPTORS.length + 2];
        System.arraycopy(DefaultElementWrapper.DESCRIPTORS, 0, descriptors, 0, DefaultElementWrapper.DESCRIPTORS.length);
        descriptors[descriptors.length - 2] = 
            new TextPropertyDescriptor(FAULT_NAME, "FaultName");
        descriptors[descriptors.length - 1] = 
            new TextPropertyDescriptor(FAULT_VARIABLE, "FaultVariable");
	}

    public FaultNodeWrapper() {
        setNode(new FaultNode());
        getFaultNode().setName("Fault");
    }
    
    public FaultNode getFaultNode() {
        return (FaultNode) getNode();
    }
    
	public IPropertyDescriptor[] getPropertyDescriptors() {
		return descriptors;
	}

    public boolean acceptsIncomingConnection(ElementConnection connection, ElementWrapper source) {
        return super.acceptsIncomingConnection(connection, source)
        	&& getIncomingConnections().isEmpty();
    }

    public boolean acceptsOutgoingConnection(ElementConnection connection, ElementWrapper target) {
        return false;
    }
    
    public Object getPropertyValue(Object id) {
        if (FAULT_NAME.equals(id)) {
        	String faultName = getFaultNode().getFaultName();
            return faultName == null ? "" : faultName;
        }
        if (FAULT_VARIABLE.equals(id)) {
        	String faultVariable = getFaultNode().getFaultVariable();
            return faultVariable == null ? "" : faultVariable;
        }
        return super.getPropertyValue(id);
    }

    public void resetPropertyValue(Object id) {
        if (FAULT_NAME.equals(id)) {
        	getFaultNode().setFaultName(null);
        } else if (FAULT_VARIABLE.equals(id)) {
        	getFaultNode().setFaultVariable(null);
        } else {
            super.resetPropertyValue(id);
        }
    }

    public void setPropertyValue(Object id, Object value) {
        if (FAULT_NAME.equals(id)) {
        	getFaultNode().setFaultName((String) value);
        } else if (FAULT_VARIABLE.equals(id)) {
        	getFaultNode().setFaultVariable((String) value);
        } else {
            super.setPropertyValue(id, value);
        }
    }
    
}
