package org.drools.eclipse.flow.ruleflow.editor.editpart;

import org.drools.eclipse.flow.common.editor.editpart.ElementEditPart;
import org.drools.eclipse.flow.ruleflow.core.NodeWrapper;
import org.drools.eclipse.flow.ruleflow.editor.editpart.figure.DefaultNodeFigure;
import org.drools.workflow.core.Node;
import org.drools.workflow.core.NodeExtension;
import org.eclipse.draw2d.IFigure;

public class DefaultNodeEditPart extends ElementEditPart {

    protected IFigure createFigure() {
        DefaultNodeFigure figure = new DefaultNodeFigure();
        Node node = ((NodeWrapper) getElementWrapper()).getNode();
        if (node instanceof NodeExtension) {
            figure.setIcon(((NodeExtension) node).getIcon());
        }
        return figure;
    }

}
