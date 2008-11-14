package org.drools.eclipse.flow.common.editor.editpart;

import java.util.List;

import org.drools.eclipse.flow.common.editor.core.ElementContainerElementWrapper;
import org.drools.eclipse.flow.common.editor.core.ModelEvent;
import org.drools.eclipse.flow.common.editor.editpart.figure.ElementContainerFigure;
import org.drools.eclipse.flow.common.editor.policy.ElementContainerLayoutEditPolicy;
import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.MouseWheelHelper;
import org.eclipse.gef.editparts.ViewportMouseWheelHelper;

public class ElementContainerEditPart extends ElementEditPart {

    protected ElementContainerElementWrapper getElementContainerElementWrapper() {
        return (ElementContainerElementWrapper) getModel();
    }

    protected IFigure createFigure() {
        return new ElementContainerFigure();
    }

    protected void createEditPolicies() {
        super.createEditPolicies();
        installEditPolicy(EditPolicy.DIRECT_EDIT_ROLE, null);
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new ElementContainerLayoutEditPolicy());
    }
    
    public void modelChanged(ModelEvent event) {
        if (event.getChange() == ElementContainerElementWrapper.ADD_ELEMENT) {
            refreshChildren();
        } else if (event.getChange() == ElementContainerElementWrapper.REMOVE_ELEMENT) {
            refreshChildren();
        } else {
            super.modelChanged(event);
        }
    }
    
    public Object getAdapter(Class key) {
        if (key == MouseWheelHelper.class) {
            return new ViewportMouseWheelHelper(this);
        }
        return super.getAdapter(key);
    }
    
    protected List getModelChildren() {
        return getElementContainerElementWrapper().getElements();
    }

    public IFigure getContentPane() {
        return ((ElementContainerFigure) getFigure()).getPane();
    }
}
