package org.drools.eclipse.flow.ruleflow.view.property.action;

import org.drools.knowledge.definitions.process.WorkflowProcess;
import org.drools.workflow.core.impl.ExtendedNodeImpl;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class OnEntryActionsPropertyDescriptor extends PropertyDescriptor {
	
	private WorkflowProcess process;
	private ExtendedNodeImpl extendedNode;

	public OnEntryActionsPropertyDescriptor(Object id, String displayName, ExtendedNodeImpl extendedNode, WorkflowProcess process) {
        super(id, displayName);
        this.extendedNode = extendedNode;
        this.process = process;
    }

    public CellEditor createPropertyEditor(Composite parent) {
    	OnEntryActionsCellEditor editor = new OnEntryActionsCellEditor(parent, process, extendedNode);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }
}
