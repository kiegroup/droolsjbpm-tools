package org.drools.eclipse.flow.ruleflow.view.property.metadata;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

public class MetaDataPropertyDescriptor extends PropertyDescriptor {

    public MetaDataPropertyDescriptor(Object id, String displayName) {
        super(id, displayName);
    }
    
    public CellEditor createPropertyEditor(Composite parent) {
        MetaDataCellEditor editor = new MetaDataCellEditor(parent);
        if (getValidator() != null) {
            editor.setValidator(getValidator());
        }
        return editor;
    }
    
}
