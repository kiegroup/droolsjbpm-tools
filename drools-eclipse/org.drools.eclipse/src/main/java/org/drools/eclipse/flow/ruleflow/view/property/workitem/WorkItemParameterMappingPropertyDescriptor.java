package org.drools.eclipse.flow.ruleflow.view.property.workitem;

import org.drools.workflow.core.node.WorkItemNode;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class WorkItemParameterMappingPropertyDescriptor extends PropertyDescriptor {

    private WorkItemNode workItemNode;
    
    public WorkItemParameterMappingPropertyDescriptor(Object id, String displayName, WorkItemNode workItemNode) {
        super(id, displayName);
        this.workItemNode = workItemNode;
    }
    
    public CellEditor createPropertyEditor(Composite parent) {
        WorkItemParameterMappingCellEditor editor = new WorkItemParameterMappingCellEditor(parent, workItemNode);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }
    
}
