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
import org.drools.workflow.core.node.Join;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

/**
 * Wrapper for a join node.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class JoinWrapper extends AbstractNodeWrapper {

    private static final long serialVersionUID = 400L;
    private static IPropertyDescriptor[] descriptors;

    public static final String TYPE = "type";
    static {
        descriptors = new IPropertyDescriptor[DefaultElementWrapper.descriptors.length + 1];
        System.arraycopy(DefaultElementWrapper.descriptors, 0, descriptors, 0, DefaultElementWrapper.descriptors.length);
        descriptors[descriptors.length - 1] = 
            new ComboBoxPropertyDescriptor(TYPE, "Type", new String[] { "", "AND", "XOR", "Discriminator" });
    }
    
    public JoinWrapper() {
        setNode(new Join());
        getJoin().setName("Join");
    }
    
    public Join getJoin() {
        return (Join) getNode();
    }
    
    public boolean acceptsIncomingConnection(ElementConnection connection) {
        return true;
    }

    public boolean acceptsOutgoingConnection(ElementConnection connection) {
        return getOutgoingConnections().isEmpty();
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
        return descriptors;
    }

    public Object getPropertyValue(Object id) {
        if (TYPE.equals(id)) {
            return new Integer(getJoin().getType());
        }
        return super.getPropertyValue(id);
    }

    public void resetPropertyValue(Object id) {
        if (TYPE.equals(id)) {
            getJoin().setType(Join.TYPE_UNDEFINED);
        } else {
            super.resetPropertyValue(id);
        }
    }

    public void setPropertyValue(Object id, Object value) {
        if (TYPE.equals(id)) {
            getJoin().setType(((Integer) value).intValue());
        } else {
            super.setPropertyValue(id, value);
        }
    }
}
