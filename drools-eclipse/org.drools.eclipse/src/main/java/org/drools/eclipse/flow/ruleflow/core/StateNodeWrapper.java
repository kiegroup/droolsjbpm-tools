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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.drools.definition.process.Connection;
import org.drools.eclipse.flow.common.editor.core.DefaultElementWrapper;
import org.drools.eclipse.flow.common.editor.core.ElementConnection;
import org.drools.eclipse.flow.common.editor.core.ElementWrapper;
import org.drools.eclipse.flow.ruleflow.view.property.constraint.StateConstraintsPropertyDescriptor;
import org.drools.workflow.core.Constraint;
import org.drools.workflow.core.Node;
import org.drools.workflow.core.WorkflowProcess;
import org.drools.workflow.core.impl.ConnectionRef;
import org.drools.workflow.core.impl.NodeImpl;
import org.drools.workflow.core.node.StateNode;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

/**
 * Wrapper for a milestone node.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class StateNodeWrapper extends StateBasedNodeWrapper {

    public static final String CONSTRAINTS = "constraints";

	private static final long serialVersionUID = 4L;

    public StateNodeWrapper() {
        setNode(new StateNode());
        getStateNode().setName("State");
    }
    
    protected void initDescriptors() {
    	super.initDescriptors();
    	IPropertyDescriptor[] oldDescriptors = descriptors; 
        descriptors = new IPropertyDescriptor[oldDescriptors.length + 3];
    	System.arraycopy(oldDescriptors, 0, descriptors, 0, oldDescriptors.length);
    	descriptors[descriptors.length - 3] = getOnEntryPropertyDescriptor();
    	descriptors[descriptors.length - 2] = getOnExitPropertyDescriptor();
        descriptors[descriptors.length - 1] = 
            new StateConstraintsPropertyDescriptor(CONSTRAINTS, "Constraints",
        		getStateNode(), (WorkflowProcess) getParent().getProcessWrapper().getProcess());
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
    	if (descriptors == null) {
    		initDescriptors();
    	}
        return descriptors;
    }

    public void setNode(Node node) {
    	super.setNode(node);
    	for (Connection connection: getStateNode().getOutgoingConnections(NodeImpl.CONNECTION_DEFAULT_TYPE)) {
    		String label = null;
    		Constraint constraint = getStateNode().getConstraint(connection);
			if (constraint != null) {
				label = constraint.getName();
			}
			((org.drools.workflow.core.Connection) connection).setMetaData("label", label);
    	}
    }
    
    public StateNode getStateNode() {
        return (StateNode) getNode();
    }
    
    private void updateConnectionLabels() {
    	for (ElementConnection connection: getOutgoingConnections()) {
    		updateConnectionLabel(connection);
    	}
    }
    
    private void updateConnectionLabel(ElementConnection connection) {
    	ConnectionWrapper connectionWrapper = (ConnectionWrapper) connection;
		String label = null;
		Constraint constraint = getStateNode().getConstraint(
			connectionWrapper.getConnection());
		if (constraint != null) {
			label = constraint.getName();
		}
		connectionWrapper.getConnection().setMetaData("label", label);
		connectionWrapper.notifyListeners(ElementConnection.CHANGE_LABEL);
    }
     
    public boolean acceptsIncomingConnection(ElementConnection connection, ElementWrapper source) {
        return super.acceptsIncomingConnection(connection, source)
        	&& getIncomingConnections().isEmpty();
    }

    public Object getPropertyValue(Object id) {
        if (CONSTRAINTS.equals(id)) {
    		return new MyHashMap<ConnectionRef, Constraint>(
				getStateNode().getConstraints());
        }
        return super.getPropertyValue(id);
    }

    public void resetPropertyValue(Object id) {
        if (CONSTRAINTS.equals(id)) {
        	for (Connection connection: getStateNode().getOutgoingConnections(NodeImpl.CONNECTION_DEFAULT_TYPE)) {
        		getStateNode().setConstraint(connection, null);
        	}
            updateConnectionLabels();
        } else {
            super.resetPropertyValue(id);
        }
    }

    @SuppressWarnings("unchecked")
	public void setPropertyValue(Object id, Object value) {
    	if (CONSTRAINTS.equals(id)) {
        	Iterator<Map.Entry<ConnectionRef, Constraint>> iterator = ((Map<ConnectionRef, Constraint>) value).entrySet().iterator();
        	while (iterator.hasNext()) {
				Map.Entry<ConnectionRef, Constraint> element = iterator.next();
				ConnectionRef connectionRef = element.getKey();
				Connection outgoingConnection = null; 
				for (Connection out: getStateNode().getOutgoingConnections(NodeImpl.CONNECTION_DEFAULT_TYPE)) {
				    if (out.getToType().equals(connectionRef.getToType())
			            && out.getTo().getId() == connectionRef.getNodeId()) {
				        outgoingConnection = out;
				    }
				}
				if (outgoingConnection == null) {
				    throw new IllegalArgumentException("Could not find outgoing connection");
				}
				getStateNode().setConstraint(outgoingConnection, (Constraint) element.getValue()); 
			}
        	updateConnectionLabels();
        } else {
            super.setPropertyValue(id, value);
        }
    }
    
    public class MyHashMap<K, V> extends HashMap<K, V> {
		private static final long serialVersionUID = -1748055291307174539L;
		public MyHashMap() {
    	}
    	public MyHashMap(Map<K, V> map) {
    		super(map);
    	}
		public String toString() {
    		return "";
    	}
    }

}
