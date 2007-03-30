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
import java.util.List;

import org.drools.eclipse.editors.rete.commands.NodeSetConstraintCommand;
import org.drools.eclipse.editors.rete.model.ModelElement;
import org.drools.eclipse.editors.rete.model.ReteGraph;
import org.drools.reteoo.BaseVertex;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MarginBorder;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.NodeEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editpolicies.RootComponentEditPolicy;
import org.eclipse.gef.editpolicies.XYLayoutEditPolicy;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.requests.CreateRequest;

/**
 * EditPart for ReteGraph
 * 
 */
public class DiagramEditPart extends AbstractGraphicalEditPart
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
        installEditPolicy( EditPolicy.COMPONENT_ROLE,
                           new RootComponentEditPolicy() );
        installEditPolicy( EditPolicy.LAYOUT_ROLE,
                           new NodesXYLayoutEditPolicy() );
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
     */
    protected IFigure createFigure() {
        Figure f = new FreeformLayer();
        f.setBorder( new MarginBorder( 3 ) );
        f.setLayoutManager( new FreeformLayout() );
        return f;
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

    private ReteGraph getCastedModel() {
        return (ReteGraph) getModel();
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editparts.AbstractEditPart#getModelChildren()
     */
    protected List getModelChildren() {
        return getCastedModel().getChildren(); // return a list of nodes
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        String prop = evt.getPropertyName();
        if ( ReteGraph.PROP_CHILD_ADDED.equals( prop ) || ReteGraph.PROP_CHILD_REMOVED.equals( prop ) ) {
            refreshChildren();
        }
    }

    private static class NodesXYLayoutEditPolicy extends XYLayoutEditPolicy {

        /* (non-Javadoc)
         * @see ConstrainedLayoutEditPolicy#createChangeConstraintCommand(ChangeBoundsRequest, EditPart, Object)
         */
        protected Command createChangeConstraintCommand(ChangeBoundsRequest request,
                                                        EditPart child,
                                                        Object constraint) {
            if ( child instanceof NodeEditPart && constraint instanceof Rectangle ) {
                // return a command that can move and/or resize a BaseVertex
                return new NodeSetConstraintCommand( (BaseVertex) child.getModel(),
                                                     request,
                                                     (Rectangle) constraint );
            }
            return super.createChangeConstraintCommand( request,
                                                        child,
                                                        constraint );
        }

        /* (non-Javadoc)
         * @see ConstrainedLayoutEditPolicy#createChangeConstraintCommand(EditPart, Object)
         */
        protected Command createChangeConstraintCommand(EditPart child,
                                                        Object constraint) {
            return null;
        }

        /* (non-Javadoc)
         * @see LayoutEditPolicy#getCreateCommand(CreateRequest)
         */
        protected Command getCreateCommand(CreateRequest request) {
            return null;
        }

    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class key) {
        return super.getAdapter( key );
    }

}