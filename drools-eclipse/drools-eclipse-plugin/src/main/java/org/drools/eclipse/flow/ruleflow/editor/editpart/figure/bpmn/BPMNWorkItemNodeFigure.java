package org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn;

import org.drools.eclipse.flow.common.editor.editpart.figure.AbstractElementFigure;
import org.drools.eclipse.flow.ruleflow.editor.editpart.WorkItemEditPart.WorkItemFigureInterface;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;

public class BPMNWorkItemNodeFigure extends AbstractElementFigure implements WorkItemFigureInterface {
    
    private RoundedRectangle rectangle;
    
    public BPMNWorkItemNodeFigure() {
    	setSize(80, 48);
    }
    
    protected void customizeFigure() {
        rectangle = new RoundedRectangle();
        rectangle.setCornerDimensions(new Dimension(25, 25));
        add(rectangle, 0);
        rectangle.setBounds(getBounds());
        setSelected(false);
    }
    
    public void setBounds(Rectangle rectangle) {
        super.setBounds(rectangle);
        this.rectangle.setBounds(rectangle);
    }
    
    public void setSelected(boolean b) {
        super.setSelected(b);
        rectangle.setLineWidth(b ? 3 : 1);
        repaint();
    }
}
