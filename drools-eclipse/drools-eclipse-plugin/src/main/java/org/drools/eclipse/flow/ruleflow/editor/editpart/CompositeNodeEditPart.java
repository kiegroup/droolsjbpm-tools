package org.drools.eclipse.flow.ruleflow.editor.editpart;

import java.util.ArrayList;
import java.util.List;

import org.drools.eclipse.flow.common.editor.editpart.ElementContainerEditPart;
import org.drools.eclipse.flow.common.editor.editpart.figure.ElementContainerFigure;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.Request;
import org.eclipse.gef.requests.DropRequest;

public class CompositeNodeEditPart extends ElementContainerEditPart {
	
    protected IFigure createFigure() {
        return new CompositeNodeFigure();
    }
    
    public ConnectionAnchor getSourceConnectionAnchor(Request request) {
    	Point p = ((DropRequest) request).getLocation();
    	return ((CompositeNodeFigure) getFigure()).getOutgoingConnectionAnchorAt(p);
    }
    
    public static class CompositeNodeFigure extends ElementContainerFigure {
    	
        private List<ConnectionAnchor> outgoingConnectionAnchors = new ArrayList<ConnectionAnchor>();

        public ConnectionAnchor getOutgoingConnectionAnchorAt(Point p) {
        	ConnectionAnchor closest = null;
        	long min = Long.MAX_VALUE;
        	for (ConnectionAnchor c: outgoingConnectionAnchors) {
        		Point p2 = c.getLocation(null);
        		long d = p.getDistance2(p2);
        		if (d < min) {
        			min = d;
        			closest = c;
        		}
        	}
        	return closest;
        }
        
        public void layoutConnectionAnchors() {
//        	FixedConnectionAnchor c = (FixedConnectionAnchor) outgoingConnectionAnchors.get(0);
//        	c.setOffsetV(getBounds().height);
//        	c.setOffsetH(0);
//        	c = (FixedConnectionAnchor) outgoingConnectionAnchors.get(1);
//        	c.setOffsetV(getBounds().height);
//        	c.setOffsetH(getBounds().width);
        }
        
        public void validate() {
        	if(isValid()) return;
        	layoutConnectionAnchors();
        	super.validate();
        }
        
    }
        
}
