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
import org.drools.workflow.core.node.EndNode;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

/**
 * Wrapper for an end node.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class EndNodeWrapper extends AbstractNodeWrapper {

    public static final String TERMINATE = "terminate";

    private static final long serialVersionUID = 400L;
    private static IPropertyDescriptor[] descriptors;

    static {
        descriptors = new IPropertyDescriptor[DefaultElementWrapper.DESCRIPTORS.length + 1];
        System.arraycopy(DefaultElementWrapper.DESCRIPTORS, 0, descriptors, 0, DefaultElementWrapper.DESCRIPTORS.length);
        descriptors[descriptors.length - 1] = 
            new ComboBoxPropertyDescriptor(TERMINATE, "Terminate", new String[] { "true", "false" });
    }
    
    public EndNodeWrapper() {
        setNode(new EndNode());
        getEndNode().setName("End");
    }
    
    public EndNode getEndNode() {
        return (EndNode) getNode();
    }
    
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return descriptors;
    }

    public Object getPropertyValue(Object id) {
        if (TERMINATE.equals(id)) {
            return getEndNode().isTerminate() ? new Integer(0) : new Integer(1);
        }
        return super.getPropertyValue(id);
    }

    public void resetPropertyValue(Object id) {
        if (TERMINATE.equals(id)) {
            getEndNode().setTerminate(true);
        } else {
            super.resetPropertyValue(id);
        }
    }

    public void setPropertyValue(Object id, Object value) {
        if (TERMINATE.equals(id)) {
            getEndNode().setTerminate(((Integer) value).intValue() == 0);
        } else {
            super.setPropertyValue(id, value);
        }
    }

    public boolean acceptsIncomingConnection(ElementConnection connection, ElementWrapper source) {
        return super.acceptsIncomingConnection(connection, source)
        	&& getIncomingConnections().isEmpty();
    }

    public boolean acceptsOutgoingConnection(ElementConnection connection, ElementWrapper target) {
        return false;
    }
}
