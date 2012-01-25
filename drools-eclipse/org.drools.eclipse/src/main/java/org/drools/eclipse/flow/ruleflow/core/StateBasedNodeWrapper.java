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

import java.util.Map;

import org.drools.definition.process.NodeContainer;
import org.drools.eclipse.flow.ruleflow.view.property.timers.TimersPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.jbpm.process.core.timer.Timer;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.DroolsAction;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.WorkflowProcess;
import org.jbpm.workflow.core.node.StateBasedNode;

public class StateBasedNodeWrapper extends ExtendedNodeWrapper {

    public static final String TIMERS = "Timers";

    private static final long serialVersionUID = 510l;

    protected IPropertyDescriptor[] descriptors;
    
    public StateBasedNode getStateBasedNode() {
        return (StateBasedNode) getNode();
    }

    protected void initDescriptors() {
    	if (isFullProperties()) {
	        descriptors = new IPropertyDescriptor[AbstractNodeWrapper.DESCRIPTORS.length + 1];
	        System.arraycopy(AbstractNodeWrapper.DESCRIPTORS, 0, descriptors, 0, AbstractNodeWrapper.DESCRIPTORS.length);
	        descriptors[descriptors.length - 1] = 
	            new TimersPropertyDescriptor(TIMERS, "Timers", getStateBasedNode(),
	                (WorkflowProcess) getParent().getProcessWrapper().getProcess());
    	} else {
    		descriptors = AbstractNodeWrapper.DESCRIPTORS;
    	}
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
        if (descriptors == null) {
            initDescriptors();
        }
        return descriptors;
    }

    public Object getPropertyValue(Object id) {
        if (TIMERS.equals(id)) {
            return getStateBasedNode().getTimers();
        }
        return super.getPropertyValue(id);
    }

    public void resetPropertyValue(Object id) {
        if (TIMERS.equals(id)) {
            getStateBasedNode().removeAllTimers();
        } else {
            super.resetPropertyValue(id);
        }
    }

    @SuppressWarnings("unchecked")
    public void setPropertyValue(Object id, Object value) {
        if (TIMERS.equals(id)) {
            getStateBasedNode().removeAllTimers();
            // adding one by one so the ids are set correctly
            if (value != null) {
                for (Map.Entry<Timer, DroolsAction> entry: ((Map<Timer, DroolsAction>) value).entrySet()) {
                    getStateBasedNode().addTimer(entry.getKey(), entry.getValue());
                }
            }
        } else {
            super.setPropertyValue(id, value);
        }
    }
    
}
