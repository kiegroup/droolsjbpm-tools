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

import org.drools.eclipse.flow.common.editor.editpart.ElementEditPart;
import org.drools.eclipse.flow.common.editor.editpart.figure.AbstractElementFigure;
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

    private static final Color color = new Color(Display.getCurrent(), 70, 130, 180);
    
    protected IFigure createFigure() {
        return new JoinFigure();
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

    public class JoinFigure extends AbstractElementFigure {
        
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
    }
}
