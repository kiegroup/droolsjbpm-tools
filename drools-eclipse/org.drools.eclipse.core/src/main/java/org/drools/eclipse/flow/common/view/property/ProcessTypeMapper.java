package org.drools.eclipse.flow.common.view.property;

import org.drools.eclipse.flow.common.editor.editpart.ElementConnectionEditPart;
import org.drools.eclipse.flow.common.editor.editpart.ElementEditPart;
import org.drools.eclipse.flow.common.editor.editpart.ProcessEditPart;
import org.eclipse.ui.views.properties.tabbed.ITypeMapper;

public class ProcessTypeMapper implements ITypeMapper {
    
    public ProcessTypeMapper() {
    }

    public Class<?> mapType(Object object) {
        if (object instanceof ElementEditPart) {
            return ((ElementEditPart) object).getModel().getClass();
        }
        if (object instanceof ProcessEditPart) {
            return ((ProcessEditPart) object).getModel().getClass();
        }
        if (object instanceof ElementConnectionEditPart) {
            return ((ElementConnectionEditPart) object).getModel().getClass();
        }
        return object.getClass();
    }

}
