/*
 * Copyright 2010 JBoss Inc
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

package org.drools.eclipse.flow.ruleflow.core;

import org.drools.eclipse.flow.common.editor.core.ElementConnection;
import org.drools.eclipse.flow.common.editor.core.ElementWrapper;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.workflow.core.node.DynamicNode;

public class DynamicNodeWrapper extends CompositeNodeWrapper {

    public static final String AUTO_COMPLETE = "autoComplete";

    private static final long serialVersionUID = 510l;
    
    public DynamicNodeWrapper() {
        setNode(new DynamicNode());
        getDynamicNode().setName("Dynamic");
        VariableScope variableScope = new VariableScope();
        getDynamicNode().addContext(variableScope);
        getDynamicNode().setDefaultContext(variableScope);
    }
    
    public DynamicNode getDynamicNode() {
        return (DynamicNode) getNode();
    }
    
    protected void initDescriptors() {
        super.initDescriptors();
        IPropertyDescriptor[] oldDescriptors = descriptors;
        descriptors = new IPropertyDescriptor[oldDescriptors.length + 1];
        System.arraycopy(oldDescriptors, 0, descriptors, 0, oldDescriptors.length);
        descriptors[descriptors.length - 1] = 
            new ComboBoxPropertyDescriptor(AUTO_COMPLETE, "Auto-complete", new String[] { "true", "false" });
    }

    public boolean acceptsIncomingConnection(ElementConnection connection, ElementWrapper source) {
        return super.acceptsIncomingConnection(connection, source)
            && getIncomingConnections().isEmpty();
    }

    public boolean acceptsOutgoingConnection(ElementConnection connection, ElementWrapper target) {
        return target == null
            || (target.getParent() == getParent() && getOutgoingConnections().isEmpty());
//            || (target.getParent() == this && getForEachNode().getLinkedIncomingNode(Node.CONNECTION_DEFAULT_TYPE) == null);
    }
    
    public Object getPropertyValue(Object id) {
        if (AUTO_COMPLETE.equals(id)) {
            return getDynamicNode().isAutoComplete() ? new Integer(0) : new Integer(1);
        }
        return super.getPropertyValue(id);
    }

    public void resetPropertyValue(Object id) {
        if (AUTO_COMPLETE.equals(id)) {
            getDynamicNode().setAutoComplete(false);
        } else {
            super.resetPropertyValue(id);
        }
    }

    public void setPropertyValue(Object id, Object value) {
        if (AUTO_COMPLETE.equals(id)) {
            getDynamicNode().setAutoComplete(((Integer) value).intValue() == 0);
        } else {
            super.setPropertyValue(id, value);
        }
    }
    
}
