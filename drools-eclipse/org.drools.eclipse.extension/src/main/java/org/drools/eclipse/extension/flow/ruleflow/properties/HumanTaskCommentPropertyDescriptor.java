package org.drools.eclipse.extension.flow.ruleflow.properties;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;
import org.jbpm.workflow.core.node.WorkItemNode;

public class HumanTaskCommentPropertyDescriptor extends PropertyDescriptor {

    private WorkItemNode workItemNode;

	public HumanTaskCommentPropertyDescriptor(Object id, String displayName, WorkItemNode workItemNode) {
		super(id, displayName);
        this.workItemNode = workItemNode;
	}
    
    public CellEditor createPropertyEditor(Composite parent) {
    	HumanTaskCommentCellEditor editor = new HumanTaskCommentCellEditor(parent, workItemNode);
    	return editor;
    }

}
