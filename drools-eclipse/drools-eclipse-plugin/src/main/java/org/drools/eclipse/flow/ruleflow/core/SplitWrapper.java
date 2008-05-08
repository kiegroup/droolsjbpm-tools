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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.drools.eclipse.flow.common.editor.core.DefaultElementWrapper;
import org.drools.eclipse.flow.common.editor.core.ElementConnection;
import org.drools.eclipse.flow.common.editor.core.ElementWrapper;
import org.drools.eclipse.flow.ruleflow.view.property.constraint.ConstraintsPropertyDescriptor;
import org.drools.workflow.core.Connection;
import org.drools.workflow.core.Constraint;
import org.drools.workflow.core.WorkflowProcess;
import org.drools.workflow.core.node.Split;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

/**
 * Wrapper for a split node.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class SplitWrapper extends AbstractNodeWrapper {

    public static final String TYPE = "type";
    public static final String CONSTRAINTS = "constraints";
    
    private static final long serialVersionUID = 400L;
    private transient IPropertyDescriptor[] descriptors;

    public SplitWrapper() {
        setNode(new Split());
        getSplit().setName("Split");
        setDescriptors();
    }
     
    private void setDescriptors() {
        descriptors = new IPropertyDescriptor[DefaultElementWrapper.descriptors.length + 1];
        System.arraycopy(DefaultElementWrapper.descriptors, 0, descriptors, 0, DefaultElementWrapper.descriptors.length);
        descriptors[descriptors.length - 1] = 
            new ComboBoxPropertyDescriptor(TYPE, "Type", 
                new String[] { "", "AND", "XOR", "OR" });
    }
    
    public Split getSplit() {
        return (Split) getNode();
    }
    
    public boolean acceptsIncomingConnection(ElementConnection connection, ElementWrapper source) {
        return super.acceptsIncomingConnection(connection, source)
        	&& getIncomingConnections().isEmpty();
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
        if (getParent() != null && (getSplit().getType() == Split.TYPE_XOR
                || getSplit().getType() == Split.TYPE_OR)) {
            IPropertyDescriptor[] result = new IPropertyDescriptor[descriptors.length + 1];
            System.arraycopy(descriptors, 0, result, 0, descriptors.length);
            result[descriptors.length] = 
                new ConstraintsPropertyDescriptor(CONSTRAINTS, "Constraints", getSplit(), (WorkflowProcess) getParent().getProcessWrapper().getProcess());
            return result;
        }
        return descriptors;
    }

    public Object getPropertyValue(Object id) {
        if (TYPE.equals(id)) {
            return new Integer(getSplit().getType());
        }
        if (CONSTRAINTS.equals(id)) {
        	return getSplit().getType() == Split.TYPE_XOR
        		|| getSplit().getType() == Split.TYPE_OR
        		? new MyHashMap<Split.ConnectionRef, Constraint>(getSplit().getConstraints())
	            : new MyHashMap<Split.ConnectionRef, Constraint>();
        }
        return super.getPropertyValue(id);
    }

    public void resetPropertyValue(Object id) {
        if (TYPE.equals(id)) {
            getSplit().setType(Split.TYPE_UNDEFINED);
        } else if (CONSTRAINTS.equals(id)) {
        	for (Connection connection: getSplit().getDefaultOutgoingConnections()) {
        		getSplit().setConstraint(connection, null);
        	}
        } else {
            super.resetPropertyValue(id);
        }
    }

    public void setPropertyValue(Object id, Object value) {
        if (TYPE.equals(id)) {
            getSplit().setType(((Integer) value).intValue());
        } else if (CONSTRAINTS.equals(id)) {
        	Iterator<Map.Entry<Split.ConnectionRef, Constraint>> iterator = ((Map<Split.ConnectionRef, Constraint>) value).entrySet().iterator();
        	while (iterator.hasNext()) {
				Map.Entry<Split.ConnectionRef, Constraint> element = iterator.next();
				Split.ConnectionRef connectionRef = element.getKey();
				Connection outgoingConnection = null; 
				for (Connection out: getSplit().getDefaultOutgoingConnections()) {
				    if (out.getToType().equals(connectionRef.getToType())
			            && out.getTo().getId() == connectionRef.getNodeId()) {
				        outgoingConnection = out;
				    }
				}
				if (outgoingConnection == null) {
				    throw new IllegalArgumentException("Could not find outgoing connection");
				}
				getSplit().setConstraint(outgoingConnection, (Constraint) element.getValue()); 
			}
        } else {
            super.setPropertyValue(id, value);
        }
    }
    
    private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
        aInputStream.defaultReadObject();
        setDescriptors();
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
