package org.drools.eclipse.flow.ruleflow.view.property.color;

import java.lang.reflect.InvocationTargetException;

import org.drools.eclipse.flow.ruleflow.view.property.action.ActionCellEditor;
import org.drools.workflow.core.WorkflowProcess;
import org.drools.workflow.core.node.ActionNode;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class ColorPropertyDescriptor extends PropertyDescriptor {

    public ColorPropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
    }
    
    public CellEditor createPropertyEditor(Composite parent) {
    	ColorCellEditor editor = new ColorCellEditor(parent);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }
    
}
