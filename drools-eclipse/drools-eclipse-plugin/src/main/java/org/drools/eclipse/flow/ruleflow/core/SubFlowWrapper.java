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
import org.drools.ruleflow.core.SubFlowNode;
import org.drools.ruleflow.core.impl.SubFlowNodeImpl;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * Wrapper for a SubFlow node.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class SubFlowWrapper extends NodeWrapper {

	private static final long serialVersionUID = 3668348577732020324L;
    private static IPropertyDescriptor[] descriptors;
    
    public static final String PROCESS_ID = "ProcessId";

    static {
        descriptors = new IPropertyDescriptor[DefaultElementWrapper.descriptors.length + 1];
        System.arraycopy(DefaultElementWrapper.descriptors, 0, descriptors, 0, DefaultElementWrapper.descriptors.length);
        descriptors[descriptors.length - 1] = 
        	new TextPropertyDescriptor(PROCESS_ID, "ProcessId");
    }
    
    public SubFlowWrapper() {
        setNode(new SubFlowNodeImpl());
        getSubFlowNode().setName("SubFlow");
    }
    
    public SubFlowNode getSubFlowNode() {
        return (SubFlowNode) getNode();
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
        if (PROCESS_ID.equals(id)) {
        	String processId = getSubFlowNode().getProcessId();
            return processId == null ? "" : processId;
        }
        return super.getPropertyValue(id);
    }

    public void resetPropertyValue(Object id) {
        if (PROCESS_ID.equals(id)) {
        	getSubFlowNode().setProcessId("");
        } else {
            super.resetPropertyValue(id);
        }
    }

    public void setPropertyValue(Object id, Object value) {
        if (PROCESS_ID.equals(id)) {
        	getSubFlowNode().setProcessId((String) value);
        } else {
            super.setPropertyValue(id, value);
        }
    }
}
