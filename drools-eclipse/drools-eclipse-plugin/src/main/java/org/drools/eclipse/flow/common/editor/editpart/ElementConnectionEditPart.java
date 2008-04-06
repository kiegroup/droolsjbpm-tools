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

import java.util.ArrayList;
import java.util.List;

import org.drools.eclipse.flow.common.editor.core.ElementConnection;
import org.drools.eclipse.flow.common.editor.core.ElementConnectionFactory;
import org.drools.eclipse.flow.common.editor.core.ModelEvent;
import org.drools.eclipse.flow.common.editor.core.ModelListener;
import org.drools.eclipse.flow.common.editor.policy.ConnectionBendpointEditPolicy;
import org.drools.eclipse.flow.common.editor.policy.ConnectionEditPolicy;
import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.BendpointConnectionRouter;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolygonDecoration;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;

/**
 * Implementation of a connection EditPart.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public abstract class ElementConnectionEditPart extends AbstractConnectionEditPart implements ModelListener {
    
    protected void createEditPolicies() {
    	ConnectionEditPolicy connectionEditPolicy = new ConnectionEditPolicy();
    	connectionEditPolicy.setDefaultElementConnectionFactory(getDefaultElementConnectionFactory());
        installEditPolicy(EditPolicy.CONNECTION_ENDPOINTS_ROLE, new ConnectionEndpointEditPolicy());
        installEditPolicy(EditPolicy.CONNECTION_ROLE, connectionEditPolicy);
        installEditPolicy(EditPolicy.CONNECTION_BENDPOINTS_ROLE, new ConnectionBendpointEditPolicy());
    }
    
    protected abstract ElementConnectionFactory getDefaultElementConnectionFactory();

    protected IFigure createFigure() {
        PolylineConnection result = new PolylineConnection();
        result.setConnectionRouter(new BendpointConnectionRouter());
        result.setTargetDecoration(new PolygonDecoration());
        return result;
    }
    
    public void setSelected(int value) {
        super.setSelected(value);
        if (value != EditPart.SELECTED_NONE) {
            ((PolylineConnection)getFigure()).setLineWidth(2);
        } else {
            ((PolylineConnection)getFigure()).setLineWidth(1);
        }
    }
    
    public void modelChanged(ModelEvent event) {
        if (event.getChange() == ElementConnection.CHANGE_BENDPOINTS) {
            refreshBendpoints();
        }
    }

    public void activate() {
        super.activate();
        ((ElementConnection) getModel()).addListener(this);
    }

    public void deactivate() {
        ((ElementConnection) getModel()).removeListener(this);
        super.deactivate();
    }

    protected void refreshBendpoints() {
        List bendpoints = ((ElementConnection) getModel()).getBendpoints();
        List constraint = new ArrayList();
        for (int i = 0; i < bendpoints.size(); i++) {
            constraint.add(new AbsoluteBendpoint((Point) bendpoints.get(i)));
        }
        getConnectionFigure().setRoutingConstraint(constraint);
    }

    protected void refreshVisuals() {
        refreshBendpoints();
    }
}