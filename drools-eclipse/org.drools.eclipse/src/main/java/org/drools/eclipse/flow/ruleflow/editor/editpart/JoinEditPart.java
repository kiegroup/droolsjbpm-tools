package org.drools.eclipse.flow.ruleflow.editor.editpart;
/*
 * Copyright 2005 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.flow.common.editor.core.ModelEvent;
import org.drools.eclipse.flow.common.editor.editpart.ElementEditPart;
import org.drools.eclipse.flow.common.editor.editpart.figure.AbstractElementFigure;
import org.drools.eclipse.flow.ruleflow.core.JoinWrapper;
import org.drools.eclipse.flow.ruleflow.skin.SkinManager;
import org.drools.eclipse.flow.ruleflow.skin.SkinProvider;
import org.drools.eclipse.preferences.IDroolsConstants;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.Ellipse;
import org.eclipse.draw2d.EllipseAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.Request;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * EditPart for a join node.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class JoinEditPart extends ElementEditPart {

	private String SKIN =
		DroolsEclipsePlugin.getDefault().getPreferenceStore().getString(IDroolsConstants.SKIN);
	
    private static final Color color = new Color(Display.getCurrent(), 70, 130, 180);
    
    protected IFigure createFigure() {
    	SkinProvider skinProvider = SkinManager.getInstance().getSkinProvider(SKIN);
    	IFigure result = skinProvider.createJoinFigure();
    	Rectangle constraint = getElementWrapper().getConstraint();
    	constraint.width = result.getSize().width;
    	constraint.height = result.getSize().height;
    	getElementWrapper().setConstraint(constraint);
    	return result;
    }

    public void modelChanged(ModelEvent event) {
        if (event.getChange() == JoinWrapper.CHANGE_TYPE) {
            refreshVisuals();
        } else {
        	super.modelChanged(event);
        }
    }
    
    protected void refreshVisuals() {
    	super.refreshVisuals();
    	int type = ((JoinWrapper) getModel()).getJoin().getType();
		((JoinFigureInterface) getFigure()).setType(type);
    }
    
    public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
        return new EllipseAnchor(getFigure());
    }

    public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
        return new EllipseAnchor(getFigure());
    }

    public ConnectionAnchor getSourceConnectionAnchor(Request request) {
        return new EllipseAnchor(getFigure());
    }

    public ConnectionAnchor getTargetConnectionAnchor(Request request) {
        return new EllipseAnchor(getFigure());
    }
    
    public static interface JoinFigureInterface extends IFigure {
    	void setType(int type);
    }

    public static class JoinFigure extends AbstractElementFigure implements JoinFigureInterface {
        
        private Ellipse ellipse;
        
        protected void customizeFigure() {
            ellipse = new Ellipse();
            add(ellipse, 0);
            ellipse.setBackgroundColor(color);
            ellipse.setBounds(getBounds());
        }
        
        public void setBounds(Rectangle rectangle) {
            super.setBounds(rectangle);
            ellipse.setBounds(rectangle);
        }
        
        public void setSelected(boolean b) {
            super.setSelected(b);
            ellipse.setLineWidth(b ? 3 : 1);
            repaint();
        }

		public void setType(int type) {
			// Do nothing
		}

    }

}
