package org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn2;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.flow.common.editor.editpart.figure.AbstractElementFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

public class BPMNTimerNodeFigure extends AbstractElementFigure {
    
    private static final Image ICON = ImageDescriptor.createFromURL(
		DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/large/intermediate_timer.png")).createImage();
    
    public BPMNTimerNodeFigure() {
    	setSize(48, 48);
    }
        
    public void setBounds(Rectangle r) {
    	r.setSize(48, 48);
    	super.setBounds(r);
    }
    
    protected void customizeFigure() {
        setIcon(ICON);
    }
    
    public void setText(String text) {
    }
    
}