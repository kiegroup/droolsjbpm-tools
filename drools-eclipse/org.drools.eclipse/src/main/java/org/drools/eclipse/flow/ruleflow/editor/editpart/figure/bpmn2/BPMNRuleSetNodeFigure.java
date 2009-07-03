package org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn2;

import org.drools.eclipse.flow.common.editor.editpart.figure.AbstractElementFigure;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

public class BPMNRuleSetNodeFigure extends AbstractElementFigure {
    
    private static final Color color = new Color(Display.getCurrent(), 255, 255, 225);

    private RoundedRectangle rectangle;
    
    public BPMNRuleSetNodeFigure() {
    	setSize(80, 48);
    }
    
    protected void customizeFigure() {
        rectangle = new RoundedRectangle();
        rectangle.setCornerDimensions(new Dimension(25, 25));
        rectangle.setBackgroundColor(color);
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
