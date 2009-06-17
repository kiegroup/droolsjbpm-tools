package org.drools.eclipse.flow.ruleflow.core;

import java.util.Map;

import org.drools.eclipse.flow.common.editor.core.DefaultElementWrapper;
import org.drools.eclipse.flow.ruleflow.view.property.timers.TimersPropertyDescriptor;
import org.drools.process.core.timer.Timer;
import org.drools.workflow.core.DroolsAction;
import org.drools.workflow.core.WorkflowProcess;
import org.drools.workflow.core.node.StateBasedNode;
import org.eclipse.ui.views.properties.IPropertyDescriptor;

public class StateBasedNodeWrapper extends ExtendedNodeWrapper {

    public static final String TIMERS = "Timers";

	private static final long serialVersionUID = 4L;
	
    protected IPropertyDescriptor[] descriptors;
    
    public StateBasedNode getStateBasedNode() {
    	return (StateBasedNode) getNode();
    }

    protected void initDescriptors() {
    	descriptors = new IPropertyDescriptor[DefaultElementWrapper.DESCRIPTORS.length + 1];
        System.arraycopy(DefaultElementWrapper.DESCRIPTORS, 0, descriptors, 0, DefaultElementWrapper.DESCRIPTORS.length);
        descriptors[descriptors.length - 1] = 
            new TimersPropertyDescriptor(TIMERS, "Timers", getStateBasedNode(),
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
        	for (Map.Entry<Timer, DroolsAction> entry: ((Map<Timer, DroolsAction>) value).entrySet()) {
        		getStateBasedNode().addTimer(entry.getKey(), entry.getValue());
        	}
        } else {
            super.setPropertyValue(id, value);
        }
    }
    
}
