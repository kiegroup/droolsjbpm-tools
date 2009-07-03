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
import org.drools.eclipse.flow.common.editor.editpart.ElementEditPart;
import org.drools.eclipse.flow.common.editor.editpart.figure.AbstractElementFigure;
import org.drools.eclipse.flow.ruleflow.skin.SkinManager;
import org.drools.eclipse.flow.ruleflow.skin.SkinProvider;
import org.drools.eclipse.preferences.IDroolsConstants;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

/**
 * EditPart for an end node.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class EndNodeEditPart extends ElementEditPart {

	private String SKIN =
		DroolsEclipsePlugin.getDefault().getPreferenceStore().getString(IDroolsConstants.SKIN);
	
    protected IFigure createFigure() {
    	SkinProvider skinProvider = SkinManager.getInstance().getSkinProvider(SKIN);
    	IFigure result = skinProvider.createEndNodeFigure();
    	Rectangle constraint = getElementWrapper().getConstraint();
    	constraint.width = result.getSize().width;
    	constraint.height = result.getSize().height;
    	getElementWrapper().setConstraint(constraint);
    	return result;
    }

    public static class EndNodeFigure extends AbstractElementFigure {
        
        private static final Image icon = ImageDescriptor.createFromURL(
        	DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/process_stop.gif")).createImage();
            
        protected void customizeFigure() {
            setIcon(icon);
            setBorder(new LineBorder(1));
        }
        
        public void setSelected(boolean b) {
            super.setSelected(b);
            ((LineBorder) getBorder()).setWidth(b ? 3 : 1);
            repaint();
        }
    }

}
