package org.drools.eclipse.flow.ruleflow.view.property.timers;

import org.drools.workflow.core.WorkflowProcess;
import org.drools.workflow.core.node.StateBasedNode;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class TimersPropertyDescriptor extends PropertyDescriptor {
	
	private WorkflowProcess process;
	private StateBasedNode stateBasedNode;

	public TimersPropertyDescriptor(Object id, String displayName, StateBasedNode stateBasedNode, WorkflowProcess process) {
        super(id, displayName);
        this.stateBasedNode = stateBasedNode;
        this.process = process;
    }

    public CellEditor createPropertyEditor(Composite parent) {
    	TimersCellEditor editor = new TimersCellEditor(parent, process, stateBasedNode);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }
}
