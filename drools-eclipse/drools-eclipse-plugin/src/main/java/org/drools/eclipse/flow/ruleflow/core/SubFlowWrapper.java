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
import org.drools.workflow.core.node.SubProcessNode;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
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
    public static final String WAIT_FOR_COMPLETION = "WaitForCompletion";

    static {
        descriptors = new IPropertyDescriptor[DefaultElementWrapper.descriptors.length + 2];
        System.arraycopy(DefaultElementWrapper.descriptors, 0, descriptors, 0, DefaultElementWrapper.descriptors.length);
        descriptors[descriptors.length - 2] = 
        	new TextPropertyDescriptor(PROCESS_ID, "ProcessId");
        descriptors[descriptors.length - 1] = 
            new ComboBoxPropertyDescriptor(WAIT_FOR_COMPLETION, "Wait for completion", new String[] {"true", "false"});
    }
    
    public SubFlowWrapper() {
        setNode(new SubProcessNode());
        getSubProcessNode().setName("SubProcess");
    }
    
    public SubProcessNode getSubProcessNode() {
        return (SubProcessNode) getNode();
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
        	String processId = getSubProcessNode().getProcessId();
            return processId == null ? "" : processId;
        }
        if (WAIT_FOR_COMPLETION.equals(id)) {
            return getSubProcessNode().isWaitForCompletion() ? new Integer(0) : new Integer(1);
        }
        return super.getPropertyValue(id);
    }

    public void resetPropertyValue(Object id) {
        if (PROCESS_ID.equals(id)) {
        	getSubProcessNode().setProcessId("");
        } else if (WAIT_FOR_COMPLETION.equals(id)) {
            getSubProcessNode().setWaitForCompletion(true);
        } else {
            super.resetPropertyValue(id);
        }
    }

    public void setPropertyValue(Object id, Object value) {
        if (PROCESS_ID.equals(id)) {
        	getSubProcessNode().setProcessId((String) value);
        } else if (WAIT_FOR_COMPLETION.equals(id)) {
            getSubProcessNode().setWaitForCompletion(((Integer) value).intValue() == 0);
        } else {
            super.setPropertyValue(id, value);
        }
    }
}
