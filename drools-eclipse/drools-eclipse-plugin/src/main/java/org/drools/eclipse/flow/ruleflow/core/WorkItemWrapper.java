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
import java.util.Map;
import java.util.Set;

import org.drools.eclipse.flow.common.editor.core.DefaultElementWrapper;
import org.drools.eclipse.flow.common.editor.core.ElementConnection;
import org.drools.eclipse.flow.common.editor.core.ElementWrapper;
import org.drools.eclipse.flow.ruleflow.view.property.workitem.WorkItemParameterMappingPropertyDescriptor;
import org.drools.eclipse.flow.ruleflow.view.property.workitem.WorkItemResultMappingPropertyDescriptor;
import org.drools.process.core.ParameterDefinition;
import org.drools.process.core.Work;
import org.drools.process.core.WorkDefinition;
import org.drools.process.core.impl.WorkImpl;
import org.drools.workflow.core.Node;
import org.drools.workflow.core.node.WorkItemNode;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * Wrapper for a work item node.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class WorkItemWrapper extends ExtendedNodeWrapper {

    public static final String WAIT_FOR_COMPLETION = "WaitForCompletion";
    public static final String RESULT_MAPPING = "ResultMapping";
    public static final String PARAMETER_MAPPING = "ParameterMapping";
    
	private static final long serialVersionUID = 400L;

	private WorkDefinition workDefinition;
	private IPropertyDescriptor[] descriptors;

    public WorkItemWrapper() {
        setNode(new WorkItemNode());
    }
    
    public WorkItemNode getWorkItemNode() {
        return (WorkItemNode) getNode();
    }
    
    public void setNode(Node node) {
        super.setNode(node);
        if (this.workDefinition != null) {
            Work work = getWorkItemNode().getWork();
            if (work == null) {
                work = new WorkImpl();
                work.setName(workDefinition.getName());
                getWorkItemNode().setWork(work);
            }
            work.setParameterDefinitions(workDefinition.getParameters());
        }
    }
    
    public void setWorkDefinition(WorkDefinition workDefinition) {
        this.workDefinition = workDefinition;
        Work work = getWorkItemNode().getWork();
        if (work == null) {
            work = new WorkImpl();
            work.setName(workDefinition.getName());
            getWorkItemNode().setWork(work);
        }
        work.setParameterDefinitions(workDefinition.getParameters());
    }
    
    public WorkDefinition getWorkDefinition() {
        return this.workDefinition;
    }

    private boolean workParameterExists(String parameterName) {
        if (workDefinition != null) {
            return workDefinition.getParameter(parameterName) != null;
        }
        return false;
    }
    
    private void setDescriptors() {
        if (workDefinition != null) {
            descriptors = createPropertyDescriptors();
        }
        if (descriptors == null) {
            descriptors = DefaultElementWrapper.descriptors;
        }
    }
    
    protected IPropertyDescriptor[] createPropertyDescriptors() {
        Set<ParameterDefinition> parameters = workDefinition.getParameters();
            descriptors = new IPropertyDescriptor[DefaultElementWrapper.descriptors.length + parameters.size() + 5];
        System.arraycopy(DefaultElementWrapper.descriptors, 0, descriptors, 0, DefaultElementWrapper.descriptors.length);
        int i = 0;
        for (ParameterDefinition def: parameters) {
            descriptors[DefaultElementWrapper.descriptors.length + (i++)] = 
                new TextPropertyDescriptor(def.getName(), def.getName());
        }
        descriptors[descriptors.length - 5] = getOnEntryPropertyDescriptor();
        descriptors[descriptors.length - 4] = getOnExitPropertyDescriptor();
        descriptors[descriptors.length - 3] = 
            new ComboBoxPropertyDescriptor(WAIT_FOR_COMPLETION, "Wait for completion", new String[] {"true", "false"});
        descriptors[descriptors.length - 2] = 
            new WorkItemParameterMappingPropertyDescriptor(PARAMETER_MAPPING, "Parameter Mapping", getWorkItemNode());
        descriptors[descriptors.length - 1] = 
            new WorkItemResultMappingPropertyDescriptor(RESULT_MAPPING, "Result Mapping", getWorkItemNode());
        return descriptors;
    }
    
    public IPropertyDescriptor[] getPropertyDescriptors() {
        if (descriptors == null) {
            setDescriptors();
        }
    	return descriptors;
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
        if (WAIT_FOR_COMPLETION.equals(id)) {
            return getWorkItemNode().isWaitForCompletion() ? new Integer(0) : new Integer(1);
        } else if (PARAMETER_MAPPING.equals(id)) {
            return getWorkItemNode().getInMappings();
        } else if (RESULT_MAPPING.equals(id)) {
            return getWorkItemNode().getOutMappings();
        } else if (id instanceof String) {
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
        if (WAIT_FOR_COMPLETION.equals(id)) {
            getWorkItemNode().setWaitForCompletion(true);
        } else if (PARAMETER_MAPPING.equals(id)) {
            getWorkItemNode().setInMappings(new HashMap<String, String>());
        } else if (RESULT_MAPPING.equals(id)) {
            getWorkItemNode().setOutMappings(new HashMap<String, String>());
        } else if (id instanceof String && workParameterExists((String) id)) {
            getWorkItemNode().getWork().setParameter((String) id, null);
        } else {
            super.resetPropertyValue(id);
        }
    }

    public void setPropertyValue(Object id, Object value) {
        if (WAIT_FOR_COMPLETION.equals(id)) {
            getWorkItemNode().setWaitForCompletion(((Integer) value).intValue() == 0);
        } else if (PARAMETER_MAPPING.equals(id)) {
            getWorkItemNode().setInMappings((Map<String, String>) value);
        } else if (RESULT_MAPPING.equals(id)) {
            getWorkItemNode().setOutMappings((Map<String, String>) value);
        } else if (id instanceof String && workParameterExists((String) id)) {
            getWorkItemNode().getWork().setParameter((String) id, value);
        } else {
            super.setPropertyValue(id, value);
        }
    }
}
