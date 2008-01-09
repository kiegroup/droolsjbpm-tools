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

import java.util.Iterator;
import java.util.Set;

import org.drools.eclipse.WorkItemDefinitions;
import org.drools.eclipse.flow.common.editor.core.DefaultElementWrapper;
import org.drools.eclipse.flow.common.editor.core.ElementConnection;
import org.drools.process.core.ParameterDefinition;
import org.drools.process.core.Work;
import org.drools.process.core.WorkDefinition;
import org.drools.workflow.core.node.WorkItemNode;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * Wrapper for a task node.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class WorkItemWrapper extends NodeWrapper {

    public static final String TASK = "Task";
    
	private static final long serialVersionUID = -3618183280436588589L;

	private IPropertyDescriptor[] descriptors;

    public WorkItemWrapper() {
        setNode(new WorkItemNode());
    }
    
    public WorkItemNode getWorkItemNode() {
        return (WorkItemNode) getNode();
    }
    
    private WorkDefinition getWorkDefinition() {
        Work work = getWorkItemNode().getWork();
        if (work != null && work.getName() != null) {
            return WorkItemDefinitions.getWorkDefinition(work.getName());
        }
        return null;
    }
    
    private boolean workParameterExists(String parameterName) {
        WorkDefinition workDefinition = getWorkDefinition();
        if (workDefinition != null) {
            return workDefinition.getParameter(parameterName) != null;
        }
        return false;
    }
    
    private void setDescriptors() {
        WorkDefinition workDefinition = getWorkDefinition();
        if (workDefinition != null) {
            Set parameters = workDefinition.getParameters();
            descriptors = new IPropertyDescriptor[DefaultElementWrapper.descriptors.length + parameters.size()];
            System.arraycopy(DefaultElementWrapper.descriptors, 0, descriptors, 0, DefaultElementWrapper.descriptors.length);
            int i = 0;
            for (Iterator iterator = parameters.iterator(); iterator.hasNext(); ) {
                ParameterDefinition def = (ParameterDefinition) iterator.next();
                descriptors[descriptors.length - parameters.size() + (i++)] = 
                    new TextPropertyDescriptor(def.getName(), def.getName());
            }
        }
        if (descriptors == null) {
            descriptors = DefaultElementWrapper.descriptors;
        }
    }
    
    public IPropertyDescriptor[] getPropertyDescriptors() {
        if (descriptors == null) {
            setDescriptors();
        }
    	return descriptors;
    }

    public boolean acceptsIncomingConnection(ElementConnection connection) {
        return getIncomingConnections().isEmpty();
    }

    public boolean acceptsOutgoingConnection(ElementConnection connection) {
        return getOutgoingConnections().isEmpty();
    }
    
    public Object getPropertyValue(Object id) {
        if (id instanceof String) {
            String name = (String) id;
            if (workParameterExists(name)) {
            	Object value = getWorkItemNode().getWork().getParameter(name);
            	if (value instanceof String) {
            	    return value;
            	}
            	return "";
            }
        }
        return super.getPropertyValue(id);
    }

    public void resetPropertyValue(Object id) {
        if (id instanceof String && workParameterExists((String) id)) {
            getWorkItemNode().getWork().setParameter((String) id, null);
        } else {
            super.resetPropertyValue(id);
        }
    }

    public void setPropertyValue(Object id, Object value) {
        if (id instanceof String && workParameterExists((String) id)) {
            getWorkItemNode().getWork().setParameter((String) id, value);
        } else {
            super.setPropertyValue(id, value);
        }
    }
}
