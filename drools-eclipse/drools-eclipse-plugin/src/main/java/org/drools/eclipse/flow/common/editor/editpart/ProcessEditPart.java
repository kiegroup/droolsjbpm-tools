package org.drools.eclipse.flow.common.editor.editpart;
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

import java.util.List;

import org.drools.eclipse.flow.common.editor.core.ModelEvent;
import org.drools.eclipse.flow.common.editor.core.ModelListener;
import org.drools.eclipse.flow.common.editor.core.ProcessWrapper;
import org.drools.eclipse.flow.common.editor.policy.ProcessLayoutEditPolicy;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;

/**
 * Default implementation of a process EditPart.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class ProcessEditPart extends AbstractGraphicalEditPart implements ModelListener {
    
    protected IFigure createFigure() {
        Figure f = new Figure();
        f.setOpaque(true);
        f.setLayoutManager(new XYLayout());
        return f;
    }

    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.NODE_ROLE, null);
        installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, null);
        installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, null);
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new ProcessLayoutEditPolicy());
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
    }

    protected List getModelChildren() {
        return ((ProcessWrapper) getModel()).getElements();
    }

    public void activate() {
        super.activate();
        ((ProcessWrapper) getModel()).addListener(this);
    }

    public void deactivate() {
        ((ProcessWrapper) getModel()).removeListener(this);
        super.deactivate();
    }

    public void modelChanged(ModelEvent event) {
        if (event.getChange() == ProcessWrapper.CHANGE_ELEMENTS) {
            refreshChildren();
        }
    }
}
