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

package org.drools.eclipse.flow.ruleflow.core;

import org.drools.eclipse.flow.common.editor.core.ElementConnection;
import org.drools.eclipse.flow.common.editor.core.ElementWrapper;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.jbpm.process.core.timer.Timer;
import org.jbpm.workflow.core.node.TimerNode;

/**
 * Wrapper for a timer node.
 */
public class TimerWrapper extends AbstractNodeWrapper {

    private static final long serialVersionUID = 510l;

    private IPropertyDescriptor[] descriptors;

    public static final String TIMER_DELAY = "TimerDelay";
    public static final String TIMER_PERIOD = "TimerPeriod";

    public TimerWrapper() {
        setNode(new TimerNode());
        getTimerNode().setName("Timer");
        getTimerNode().setTimer(new Timer());
    }
    
    private void setDescriptors() {
        descriptors = new IPropertyDescriptor[AbstractNodeWrapper.DESCRIPTORS.length + 2];
        System.arraycopy(AbstractNodeWrapper.DESCRIPTORS, 0, descriptors, 0, AbstractNodeWrapper.DESCRIPTORS.length);
        descriptors[descriptors.length - 2] = 
            new TextPropertyDescriptor(TIMER_DELAY, "Timer Delay");
        descriptors[descriptors.length - 1] = 
            new TextPropertyDescriptor(TIMER_PERIOD, "Timer Period");
    }
    
    public TimerNode getTimerNode() {
        return (TimerNode) getNode();
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
        Timer timer = getTimerNode().getTimer();
        if (TIMER_DELAY.equals(id)) {
            return timer == null ? "" :
                (timer.getDelay() == null? "" : timer.getDelay());
        }
        if (TIMER_PERIOD.equals(id)) {
            return timer == null ? "" :
                (timer.getPeriod() == null ? "" : timer.getPeriod());
        }
        return super.getPropertyValue(id);
    }

    public void resetPropertyValue(Object id) {
        Timer timer = getTimerNode().getTimer();
        if (TIMER_DELAY.equals(id)) {
            if (timer == null) {
                timer = new Timer();
                getTimerNode().setTimer(timer);
            } else {
                timer.setDelay(null);
            }
        } else if (TIMER_PERIOD.equals(id)) {
            if (timer == null) {
                timer = new Timer();
                getTimerNode().setTimer(timer);
            } else {
                timer.setPeriod(null);
            }
        } else {
            super.resetPropertyValue(id);
        }
    }

    public void setPropertyValue(Object id, Object value) {
        Timer timer = getTimerNode().getTimer();
        if (timer == null) {
            timer = new Timer();
            getTimerNode().setTimer(timer);
        }
        if (TIMER_DELAY.equals(id)) {
            String s = ((String) value).trim();
            if (s.length() == 0) {
                s = null;
            }
            timer.setDelay(s);
        } else if (TIMER_PERIOD.equals(id)) {
            String s = ((String) value).trim();
            if (s.length() == 0) {
                s = null;
            }
            timer.setPeriod(s);
        } else {
            super.setPropertyValue(id, value);
        }
    }
}
