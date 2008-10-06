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

import java.util.ArrayList;
import java.util.List;

import org.drools.eclipse.flow.common.editor.core.DefaultElementWrapper;
import org.drools.eclipse.flow.common.editor.core.ElementConnection;
import org.drools.eclipse.flow.common.editor.core.ElementWrapper;
import org.drools.process.core.event.EventFilter;
import org.drools.process.core.event.EventTypeFilter;
import org.drools.workflow.core.node.EventNode;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * Wrapper for a start node.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class EventNodeWrapper extends AbstractNodeWrapper {

    private static final long serialVersionUID = 400L;
    private static IPropertyDescriptor[] descriptors;

    public static final String VARIABLE_NAME = "variableName";
    public static final String EVENT_TYPE = "eventType";
    public static final String SCOPE = "scope";
    static {
        descriptors = new IPropertyDescriptor[DefaultElementWrapper.descriptors.length + 3];
        System.arraycopy(DefaultElementWrapper.descriptors, 0, descriptors, 0, DefaultElementWrapper.descriptors.length);
        descriptors[descriptors.length - 3] = 
            new ComboBoxPropertyDescriptor(SCOPE, "Scope", new String[] { "internal", "external" });
        descriptors[descriptors.length - 2] = 
            new TextPropertyDescriptor(VARIABLE_NAME, "VariableName");
        descriptors[descriptors.length - 1] = 
            new TextPropertyDescriptor(EVENT_TYPE, "EventType");
    }

    public EventNodeWrapper() {
        setNode(new EventNode());
        setName("Event");
    }
    
    public EventNode getEventNode() {
        return (EventNode) getNode();
    }
    
    public boolean acceptsIncomingConnection(ElementConnection connection, ElementWrapper source) {
        return false;
    }

    public boolean acceptsOutgoingConnection(ElementConnection connection, ElementWrapper target) {
        return super.acceptsOutgoingConnection(connection, target)
        	&& getOutgoingConnections().isEmpty();
    }
    
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return descriptors;
    }

    public Object getPropertyValue(Object id) {
        if (VARIABLE_NAME.equals(id)) {
        	String variableName = getEventNode().getVariableName();
            return variableName == null ? "" : variableName;
        }
        if (EVENT_TYPE.equals(id)) {
        	if (getEventNode().getEventFilters().isEmpty()) {
        		return "";
        	}
        	return ((EventTypeFilter) getEventNode().getEventFilters().get(0)).getType();
        }
        if (SCOPE.equals(id)) {
            return "external".equals(getEventNode().getScope()) ? 1 : 0;
        }
        return super.getPropertyValue(id);
    }

    public void resetPropertyValue(Object id) {
        if (VARIABLE_NAME.equals(id)) {
            getEventNode().setVariableName(null);
        } else if (EVENT_TYPE.equals(id)) {
            getEventNode().setEventFilters(new ArrayList<EventFilter>());
        } else if (SCOPE.equals(id)) {
            getEventNode().setScope("internal");
        } else {
            super.resetPropertyValue(id);
        }
    }

    public void setPropertyValue(Object id, Object value) {
        if (VARIABLE_NAME.equals(id)) {
            getEventNode().setVariableName((String) value);
        } else if (EVENT_TYPE.equals(id)) {
        	List<EventFilter> eventFilters = new ArrayList<EventFilter>();
        	EventTypeFilter eventFilter = new EventTypeFilter();
        	eventFilter.setType((String) value);
        	eventFilters.add(eventFilter);
            getEventNode().setEventFilters(eventFilters);
        } else if (SCOPE.equals(id)) {
            getEventNode().setScope((Integer) value == 1 ? "external" : "internal");
        } else {
            super.setPropertyValue(id, value);
        }
    }

}
