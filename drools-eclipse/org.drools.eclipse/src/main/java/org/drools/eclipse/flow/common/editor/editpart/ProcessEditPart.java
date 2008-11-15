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

import java.util.Iterator;
import java.util.List;

import org.drools.eclipse.flow.common.editor.core.ElementWrapper;
import org.drools.eclipse.flow.common.editor.core.ModelEvent;
import org.drools.eclipse.flow.common.editor.core.ModelListener;
import org.drools.eclipse.flow.common.editor.core.ProcessWrapper;
import org.drools.eclipse.flow.common.editor.editpart.figure.ElementFigure;
import org.drools.eclipse.flow.common.editor.policy.ElementContainerLayoutEditPolicy;
import org.eclipse.draw2d.Animation;
import org.eclipse.draw2d.AutomaticRouter;
import org.eclipse.draw2d.BendpointConnectionRouter;
import org.eclipse.draw2d.ConnectionLayer;
import org.eclipse.draw2d.FanRouter;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.ManhattanConnectionRouter;
import org.eclipse.draw2d.ShortestPathConnectionRouter;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.SnapToHelper;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.swt.SWT;

/**
 * Default implementation of a process EditPart.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class ProcessEditPart extends AbstractGraphicalEditPart implements ModelListener {
    
//	private GraphLayoutManager graphLayoutManager;
	
    protected IFigure createFigure() {
        Figure f = new Figure();
        f.setLayoutManager(new XYLayout());
//        graphLayoutManager = new GraphLayoutManager(this);
//        f.setLayoutManager(graphLayoutManager);
//        graphLayoutManager.layout(f);
        return f;
    }

    protected void createEditPolicies() {
        installEditPolicy(EditPolicy.NODE_ROLE, null);
        installEditPolicy(EditPolicy.GRAPHICAL_NODE_ROLE, null);
        installEditPolicy(EditPolicy.SELECTION_FEEDBACK_ROLE, null);
        installEditPolicy(EditPolicy.LAYOUT_ROLE, new ElementContainerLayoutEditPolicy());
        installEditPolicy(EditPolicy.COMPONENT_ROLE, new RootComponentEditPolicy());
    }

    protected List getModelChildren() {
        return getProcessWrapper().getElements();
    }
    
    public ProcessWrapper getProcessWrapper() {
    	return (ProcessWrapper) getModel();
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
        } else if (event.getChange() == ProcessWrapper.CHANGE_ROUTER_LAYOUT) {
    		refreshVisuals();
    	}
    }
    
    public Object getAdapter(Class adapter) {
    	if (adapter == SnapToHelper.class) {
    		Boolean val = (Boolean) getViewer().getProperty(SnapToGrid.PROPERTY_GRID_ENABLED);
    		if (val != null && val.booleanValue()) {
    			return new SnapToGrid(this);
    		}
    	}
    	return super.getAdapter(adapter);
    }
    
    protected void refreshVisuals() {
    	Animation.markBegin();
    	ConnectionLayer layer = (ConnectionLayer) getLayer(LayerConstants.CONNECTION_LAYER);
        if ((getViewer().getControl().getStyle() & SWT.MIRRORED ) == 0) {
            layer.setAntialias(SWT.ON);
        }

    	if (getProcessWrapper().getRouterLayout().equals(ProcessWrapper.ROUTER_LAYOUT_MANUAL)) {
    		AutomaticRouter router = new FanRouter();
    		router.setNextRouter(new BendpointConnectionRouter());
    		layer.setConnectionRouter(router);
    	} else if (getProcessWrapper().getRouterLayout().equals(ProcessWrapper.ROUTER_LAYOUT_MANHATTAN)) {
    		layer.setConnectionRouter(new ManhattanConnectionRouter());
    	} else {
    		layer.setConnectionRouter(new ShortestPathConnectionRouter(getFigure()));
    	}
    	Animation.run(400);
    }
    
	public boolean setTableModelBounds() {
		List tableParts = getChildren();
		for (Iterator iter = tableParts.iterator(); iter.hasNext(); ) {
			ElementEditPart elementEditPart = (ElementEditPart) iter.next();
			ElementFigure elementFigure = (ElementFigure) elementEditPart.getFigure();
			if (elementFigure == null) {
				continue;
			}
			Rectangle constraint = elementFigure.getBounds().getCopy();
			ElementWrapper elementWrapper = elementEditPart.getElementWrapper();
			elementWrapper.setConstraint(constraint);
		}
		return true;
	}
}
