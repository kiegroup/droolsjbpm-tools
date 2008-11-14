package org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.flow.common.editor.editpart.figure.AbstractElementFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

public class BPMNEventNodeFigure  extends AbstractElementFigure {
    
    private static final Image icon = ImageDescriptor.createFromURL(
		DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/large/intermediate_message.png")).createImage();

    public BPMNEventNodeFigure() {
    	setSize(48, 48);
    }
    
    public void setBounds(Rectangle r) {
    	r.setSize(48, 48);
    	super.setBounds(r);
    }
    
    protected void customizeFigure() {
        setIcon(icon);
    }
    
    public void setText(String text) {
    }
    
}
