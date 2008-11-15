package org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn;

import org.drools.eclipse.flow.common.editor.editpart.figure.ElementContainerFigure;
import org.eclipse.draw2d.LineBorder;

public class BPMNCompositeNodeFigure extends ElementContainerFigure {
	
    public BPMNCompositeNodeFigure() {
        setBorder(new LineBorder(1));
    }
    
    public void setSelected(boolean b) {
    	super.setSelected(b);
        ((LineBorder) getBorder()).setWidth(b ? 3 : 1);
    }
    
}