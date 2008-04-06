package org.drools.eclipse.flow.common.view.property;

import org.drools.eclipse.flow.common.editor.editpart.ElementConnectionEditPart;
import org.drools.eclipse.flow.common.editor.editpart.ElementEditPart;
import org.drools.eclipse.flow.common.editor.editpart.ProcessEditPart;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.graphics.Image;

public class ProcessLabelProvider implements ILabelProvider {

    public Image getImage(Object element) {
        return null;
    }

    public String getText(Object element) {
        if (element instanceof IStructuredSelection) {
            element = ((IStructuredSelection)element).getFirstElement();
        }
        if (element instanceof ElementEditPart) {
            return "Element " + ((ElementEditPart) element).getElementWrapper().getName();
        } else if (element instanceof ProcessEditPart) {
            return "Process " + ((ProcessEditPart) element).getProcessWrapper().getName();
        } else if (element instanceof ElementConnectionEditPart) {
            element = ((ElementConnectionEditPart) element).getModel().toString();
        }
        return element.toString();
    }

    public boolean isLabelProperty(Object element, String property) {
        return false;
    }

    public void addListener(ILabelProviderListener listener) {
    }

    public void removeListener(ILabelProviderListener listener) {
    }

    public void dispose() {
    }

}
