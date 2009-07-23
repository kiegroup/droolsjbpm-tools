package org.drools.eclipse.flow.ruleflow.editor.editpart;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.flow.common.editor.editpart.ElementContainerEditPart;
import org.drools.eclipse.flow.common.editor.editpart.figure.ElementContainerFigure;
import org.drools.eclipse.flow.ruleflow.skin.SkinManager;
import org.drools.eclipse.flow.ruleflow.skin.SkinProvider;
import org.drools.eclipse.preferences.IDroolsConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Rectangle;

public class CompositeNodeEditPart extends ElementContainerEditPart {
	
	private String SKIN =
		DroolsEclipsePlugin.getDefault().getPreferenceStore().getString(IDroolsConstants.SKIN);
	
    protected IFigure createFigure() {
    	SkinProvider skinProvider = SkinManager.getInstance().getSkinProvider(SKIN);
    	IFigure result = skinProvider.createCompositeNodeFigure();
    	Rectangle constraint = getElementWrapper().getConstraint();
    	if (constraint.width == -1) {
    		constraint.width = result.getSize().width;
    	}
    	if (constraint.height == -1) {
    		constraint.height = result.getSize().height;
    	}
    	getElementWrapper().setConstraint(constraint);
    	return result;
    }
    
//    public ConnectionAnchor getSourceConnectionAnchor(Request request) {
//    	Point p = ((DropRequest) request).getLocation();
//    	return ((CompositeNodeFigure) getFigure()).getOutgoingConnectionAnchorAt(p);
//    }
    
    public static class CompositeNodeFigure extends ElementContainerFigure {
    	
//        private List<ConnectionAnchor> outgoingConnectionAnchors = new ArrayList<ConnectionAnchor>();

        public CompositeNodeFigure() {
            setBorder(new LineBorder(1));
        }
        
        public void setSelected(boolean b) {
        	super.setSelected(b);
            ((LineBorder) getBorder()).setWidth(b ? 3 : 1);
        }

//        public ConnectionAnchor getOutgoingConnectionAnchorAt(Point p) {
//        	ConnectionAnchor closest = null;
//        	long min = Long.MAX_VALUE;
//        	for (ConnectionAnchor c: outgoingConnectionAnchors) {
//        		Point p2 = c.getLocation(null);
//        		long d = p.getDistance2(p2);
//        		if (d < min) {
//        			min = d;
//        			closest = c;
//        		}
//        	}
//        	return closest;
//        }
        
//        public void layoutConnectionAnchors() {
//        	FixedConnectionAnchor c = (FixedConnectionAnchor) outgoingConnectionAnchors.get(0);
//        	c.setOffsetV(getBounds().height);
//        	c.setOffsetH(0);
//        	c = (FixedConnectionAnchor) outgoingConnectionAnchors.get(1);
//        	c.setOffsetV(getBounds().height);
//        	c.setOffsetH(getBounds().width);
//        }
        
//        public void validate() {
//        	if(isValid()) return;
//        	layoutConnectionAnchors();
//        	super.validate();
//        }
        
    }
        
}
