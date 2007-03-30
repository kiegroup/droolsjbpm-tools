/*
 * Copyright 2006 JBoss Inc
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
package org.drools.eclipse.editors.rete.part;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.drools.eclipse.editors.rete.figure.ConnectionFigure;
import org.drools.eclipse.editors.rete.model.ModelElement;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.draw2d.PolylineDecoration;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.editparts.AbstractConnectionEditPart;
import org.eclipse.gef.editpolicies.ConnectionEndpointEditPolicy;

/**
 * Edit part for Connection model elements.
 */
class ConnectionEditPart extends AbstractConnectionEditPart
    implements
    PropertyChangeListener {

    /* (non-Javadoc)
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
     */
    public void activate() {
        if ( !isActive() ) {
            super.activate();
            ((ModelElement) getModel()).addPropertyChangeListener( this );
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
     */
    protected void createEditPolicies() {
        installEditPolicy( EditPolicy.CONNECTION_ENDPOINTS_ROLE,
                           new ConnectionEndpointEditPolicy() );
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
     */
    protected IFigure createFigure() {
        PolylineConnection connection = new ConnectionFigure();
        PolylineDecoration decoration = new PolylineDecoration();
        connection.setTargetDecoration( decoration );
        return connection;
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#deactivate()
     */
    public void deactivate() {
        if ( isActive() ) {
            super.deactivate();
            ((ModelElement) getModel()).removePropertyChangeListener( this );
        }
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        // Doing nothing   
    }

}