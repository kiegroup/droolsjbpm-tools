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

import org.drools.eclipse.flow.common.editor.core.ElementConnection;
import org.drools.eclipse.flow.common.editor.core.ElementWrapper;
import org.drools.eclipse.flow.ruleflow.view.property.subprocess.SubProcessParameterInMappingPropertyDescriptor;
import org.drools.eclipse.flow.ruleflow.view.property.subprocess.SubProcessParameterOutMappingPropertyDescriptor;
import org.drools.workflow.core.node.SubProcessNode;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * Wrapper for a SubFlow node.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class SubProcessWrapper extends EventBasedNodeWrapper {

	private static final long serialVersionUID = 3668348577732020324L;
    
    public static final String PROCESS_ID = "ProcessId";
    public static final String WAIT_FOR_COMPLETION = "WaitForCompletion";
    public static final String INDEPENDENT = "Independent";
    public static final String PARAMETER_IN_MAPPING = "ParameterInMapping";
    public static final String PARAMETER_OUT_MAPPING = "ParameterOutMapping";

    public SubProcessWrapper() {
        setNode(new SubProcessNode());
        getSubProcessNode().setName("SubProcess");
    }
    
	protected void initDescriptors() {
    	super.initDescriptors();
    	IPropertyDescriptor[] oldDescriptors = descriptors; 
        descriptors = new IPropertyDescriptor[oldDescriptors.length + 7];
        System.arraycopy(oldDescriptors, 0, descriptors, 0, oldDescriptors.length);
        descriptors[descriptors.length - 7] = getOnEntryPropertyDescriptor();
        descriptors[descriptors.length - 6] = getOnExitPropertyDescriptor();
        descriptors[descriptors.length - 5] = 
            new SubProcessParameterInMappingPropertyDescriptor(PARAMETER_IN_MAPPING, "Parameter In Mapping", getSubProcessNode());
        descriptors[descriptors.length - 4] = 
            new SubProcessParameterOutMappingPropertyDescriptor(PARAMETER_OUT_MAPPING, "Parameter Out Mapping", getSubProcessNode());
        descriptors[descriptors.length - 3] = 
            new ComboBoxPropertyDescriptor(INDEPENDENT, "Independent", new String[] {"true", "false"});
        descriptors[descriptors.length - 2] = 
        	new TextPropertyDescriptor(PROCESS_ID, "ProcessId");
        descriptors[descriptors.length - 1] = 
            new ComboBoxPropertyDescriptor(WAIT_FOR_COMPLETION, "Wait for completion", new String[] {"true", "false"});
    }
    
    public SubProcessNode getSubProcessNode() {
        return (SubProcessNode) getNode();
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
        if (PROCESS_ID.equals(id)) {
        	String processId = getSubProcessNode().getProcessId();
            return processId == null ? "" : processId;
        }
        if (WAIT_FOR_COMPLETION.equals(id)) {
            return getSubProcessNode().isWaitForCompletion() ? new Integer(0) : new Integer(1);
        }
        if (INDEPENDENT.equals(id)) {
            return getSubProcessNode().isIndependent() ? new Integer(0) : new Integer(1);
        }
        if (PARAMETER_IN_MAPPING.equals(id)) {
            return getSubProcessNode().getInMappings();
        } 
        if (PARAMETER_OUT_MAPPING.equals(id)) {
            return getSubProcessNode().getOutMappings();
        } 
        return super.getPropertyValue(id);
    }

    public void resetPropertyValue(Object id) {
        if (PROCESS_ID.equals(id)) {
        	getSubProcessNode().setProcessId("");
        } else if (WAIT_FOR_COMPLETION.equals(id)) {
            getSubProcessNode().setWaitForCompletion(true);
        } else if (INDEPENDENT.equals(id)) {
            getSubProcessNode().setIndependent(true);
        } else if (PARAMETER_IN_MAPPING.equals(id)) {
            getSubProcessNode().setInMappings(new HashMap<String, String>());
        } else if (PARAMETER_OUT_MAPPING.equals(id)) {
            getSubProcessNode().setOutMappings(new HashMap<String, String>());
        } else {
            super.resetPropertyValue(id);
        }
    }

    @SuppressWarnings("unchecked")
	public void setPropertyValue(Object id, Object value) {
        if (PROCESS_ID.equals(id)) {
        	getSubProcessNode().setProcessId((String) value);
        } else if (WAIT_FOR_COMPLETION.equals(id)) {
            getSubProcessNode().setWaitForCompletion(((Integer) value).intValue() == 0);
        } else if (INDEPENDENT.equals(id)) {
            getSubProcessNode().setIndependent(((Integer) value).intValue() == 0);
        } else if (PARAMETER_IN_MAPPING.equals(id)) {
            getSubProcessNode().setInMappings((Map<String, String>) value);
        } else if (PARAMETER_OUT_MAPPING.equals(id)) {
            getSubProcessNode().setOutMappings((Map<String, String>) value);
        } else {
            super.setPropertyValue(id, value);
        }
    }
}
