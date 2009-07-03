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
import org.drools.eclipse.flow.common.editor.editpart.ElementContainerEditPart;
import org.drools.eclipse.flow.ruleflow.skin.SkinManager;
import org.drools.eclipse.flow.ruleflow.skin.SkinProvider;
import org.drools.eclipse.preferences.IDroolsConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;

/**
 * EditPart for a ForEach node.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class ForEachNodeEditPart extends ElementContainerEditPart {

	private String SKIN =
		DroolsEclipsePlugin.getDefault().getPreferenceStore().getString(IDroolsConstants.SKIN);

    protected IFigure createFigure() {
    	SkinProvider skinProvider = SkinManager.getInstance().getSkinProvider(SKIN);
    	IFigure result = skinProvider.createForEachNodeFigure();
    	Rectangle constraint = getElementWrapper().getConstraint();
    	constraint.width = result.getSize().width;
    	constraint.height = result.getSize().height;
    	getElementWrapper().setConstraint(constraint);
    	return result;
    }
	
//    public ConnectionAnchor getSourceConnectionAnchor(Request request) {
//    	Point p = ((DropRequest) request).getLocation();
//    	Rectangle figureBounds = getFigure().getBounds();
//    	if (p.x > figureBounds.x && p.x < figureBounds.x + figureBounds.width &&
//    			p.y > figureBounds.y && p.y < figureBounds.y + figureBounds.height) {
//        	FixedConnectionAnchor c = new FixedConnectionAnchor(getFigure());
//        	c.setOffsetV(getFigure().getBounds().height/2);
//        	c.setOffsetH(0);
//            return c;
//    	}
//    	FixedConnectionAnchor c = new FixedConnectionAnchor(getFigure());
//    	c.setOffsetV(getFigure().getBounds().height/2);
//    	c.setOffsetH(getFigure().getBounds().width);
//        return c;
//    }
//
//    public ConnectionAnchor getTargetConnectionAnchor(Request request) {
//    	Point p = ((DropRequest) request).getLocation();
//    	Rectangle figureBounds = getFigure().getBounds();
//    	if (p.x > figureBounds.x && p.x < figureBounds.x + figureBounds.width &&
//    			p.y > figureBounds.y && p.y < figureBounds.y + figureBounds.height) {
//        	FixedConnectionAnchor c = new FixedConnectionAnchor(getFigure());
//        	c.setOffsetV(getFigure().getBounds().height/2);
//        	c.setOffsetH(getFigure().getBounds().width);
//            return c;
//    	}
//    	FixedConnectionAnchor c = new FixedConnectionAnchor(getFigure());
//    	c.setOffsetV(getFigure().getBounds().height/2);
//    	c.setOffsetH(0);
//        return c;
//    }

}
