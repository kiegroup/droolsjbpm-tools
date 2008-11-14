package org.drools.eclipse.flow.ruleflow.view.property.subprocess;

import org.drools.workflow.core.node.SubProcessNode;
import org.drools.workflow.core.node.WorkItemNode;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class SubProcessParameterInMappingPropertyDescriptor extends PropertyDescriptor {

    private SubProcessNode subProcessNode;
    
    public SubProcessParameterInMappingPropertyDescriptor(Object id, String displayName, SubProcessNode subProcessNode) {
        super(id, displayName);
        this.subProcessNode = subProcessNode;
    }
    
    public CellEditor createPropertyEditor(Composite parent) {
        SubProcessParameterInMappingCellEditor editor = new SubProcessParameterInMappingCellEditor(parent, subProcessNode);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }
    
}
