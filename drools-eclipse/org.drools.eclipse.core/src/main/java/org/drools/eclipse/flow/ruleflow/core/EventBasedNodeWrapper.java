package org.drools.eclipse.flow.ruleflow.core;

import java.util.Map;

import org.drools.eclipse.flow.common.editor.core.DefaultElementWrapper;
import org.drools.eclipse.flow.ruleflow.view.property.timers.TimersPropertyDescriptor;
import org.drools.process.core.timer.Timer;
import org.drools.workflow.core.DroolsAction;
import org.drools.workflow.core.WorkflowProcess;
import org.drools.workflow.core.node.EventBasedNode;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

public class EventBasedNodeWrapper extends ExtendedNodeWrapper {

    public static final String TIMERS = "Timers";

	private static final long serialVersionUID = 1L;
	
    protected IPropertyDescriptor[] descriptors;
    
    public EventBasedNode getEventBasedNode() {
    	return (EventBasedNode) getNode();
    }

    protected void initDescriptors() {
    	descriptors = new IPropertyDescriptor[DefaultElementWrapper.descriptors.length + 1];
        System.arraycopy(DefaultElementWrapper.descriptors, 0, descriptors, 0, DefaultElementWrapper.descriptors.length);
        descriptors[descriptors.length - 1] = 
            new TimersPropertyDescriptor(TIMERS, "Timers", getEventBasedNode(),
        		(WorkflowProcess) getParent().getProcessWrapper().getProcess());
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
    	if (descriptors == null) {
    		initDescriptors();
    	}
        return descriptors;
    }

    public Object getPropertyValue(Object id) {
        if (TIMERS.equals(id)) {
            return getEventBasedNode().getTimers();
        }
        return super.getPropertyValue(id);
    }

    public void resetPropertyValue(Object id) {
        if (TIMERS.equals(id)) {
        	getEventBasedNode().internalSetTimers(null);
        } else {
            super.resetPropertyValue(id);
        }
    }

	@SuppressWarnings("unchecked")
	public void setPropertyValue(Object id, Object value) {
        if (TIMERS.equals(id)) {
        	getEventBasedNode().internalSetTimers((Map<Timer, DroolsAction>) value);
        } else {
            super.setPropertyValue(id, value);
        }
    }
    
}
