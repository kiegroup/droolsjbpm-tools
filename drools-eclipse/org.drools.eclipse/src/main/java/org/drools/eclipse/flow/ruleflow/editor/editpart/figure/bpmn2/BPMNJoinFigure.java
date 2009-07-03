package org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn2;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.flow.common.editor.editpart.figure.AbstractElementFigure;
import org.drools.eclipse.flow.ruleflow.editor.editpart.JoinEditPart.JoinFigureInterface;
import org.drools.workflow.core.node.Join;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

public class BPMNJoinFigure extends AbstractElementFigure implements JoinFigureInterface {
    
    private static final Image ICON_COMPLEX = ImageDescriptor.createFromURL(
		DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn2/large/gateway_complex.png")).createImage();
    private static final Image ICON_AND = ImageDescriptor.createFromURL(
		DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn2/large/gateway_parallel.png")).createImage();
    private static final Image ICON_XOR = ImageDescriptor.createFromURL(
		DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn2/large/gateway_exclusive.png")).createImage();
                
	public BPMNJoinFigure() {
		super.setSize(49, 49);
	}
	
    public void setText(String text) {
    }
    
    public void setBounds(Rectangle r) {
    	r.setSize(49, 49);
    	super.setBounds(r);
    }
    
    public void setType(int type) {
    	if (type == Join.TYPE_AND) {
    		setIcon(ICON_AND);
    	} else if (type == Join.TYPE_XOR) {
    		setIcon(ICON_XOR);
    	} else {
    		setIcon(ICON_COMPLEX);
    	}
    }
    
    protected void customizeFigure() {
        setIcon(ICON_COMPLEX);
    }
    
}
