package org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn2;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.flow.common.editor.editpart.figure.AbstractElementFigure;
import org.drools.eclipse.flow.ruleflow.editor.editpart.EndNodeEditPart.EndNodeFigureInterface;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

public class BPMNEndNodeFigure extends AbstractElementFigure implements EndNodeFigureInterface {
    
    private static final Image ICON_TERMINATE = ImageDescriptor.createFromURL(
    	DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn2/large/end_terminate.png")).createImage();
    private static final Image ICON_EMPTY = ImageDescriptor.createFromURL(
        	DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn2/large/end_empty.png")).createImage();
        
    public BPMNEndNodeFigure() {
    	setSize(48, 48);
    }
    
    public void setText(String text) {
    }
    
    public void setBounds(Rectangle r) {
    	r.setSize(48, 48);
    	super.setBounds(r);
    }
    
    protected void customizeFigure() {
        setIcon(ICON_TERMINATE);
    }
    
    public void setSelected(boolean b) {
        super.setSelected(b);
        ((LineBorder) getBorder()).setWidth(b ? 3 : 0);
        repaint();
    }
    
	public void setTerminate(boolean terminate) {
    	setIcon(terminate ? ICON_TERMINATE : ICON_EMPTY);
	}

}
