package org.drools.eclipse.flow.ruleflow.view.property.timers;

import org.drools.knowledge.definitions.process.WorkflowProcess;
import org.drools.workflow.core.node.EventBasedNode;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class TimersPropertyDescriptor extends PropertyDescriptor {
	
	private WorkflowProcess process;
	private EventBasedNode eventBasedNode;

	public TimersPropertyDescriptor(Object id, String displayName, EventBasedNode eventBasedNode, WorkflowProcess process) {
        super(id, displayName);
        this.eventBasedNode = eventBasedNode;
        this.process = process;
    }

    public CellEditor createPropertyEditor(Composite parent) {
    	TimersCellEditor editor = new TimersCellEditor(parent, process, eventBasedNode);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }
}
