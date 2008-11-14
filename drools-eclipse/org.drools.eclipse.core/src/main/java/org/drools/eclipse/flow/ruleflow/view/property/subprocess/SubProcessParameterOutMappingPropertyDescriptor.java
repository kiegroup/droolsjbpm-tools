package org.drools.eclipse.flow.ruleflow.view.property.subprocess;

import org.drools.workflow.core.node.SubProcessNode;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class SubProcessParameterOutMappingPropertyDescriptor extends PropertyDescriptor {

    private SubProcessNode subProcessNode;
    
    public SubProcessParameterOutMappingPropertyDescriptor(Object id, String displayName, SubProcessNode subProcessNode) {
        super(id, displayName);
        this.subProcessNode = subProcessNode;
    }
    
    public CellEditor createPropertyEditor(Composite parent) {
        SubProcessParameterOutMappingCellEditor editor = new SubProcessParameterOutMappingCellEditor(parent, subProcessNode);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }
    
}
