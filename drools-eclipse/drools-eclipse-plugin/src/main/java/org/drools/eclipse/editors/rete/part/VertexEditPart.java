package org.drools.eclipse.editors.rete.part;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;

import org.drools.eclipse.editors.rete.figure.VertexFigure;
import org.drools.eclipse.editors.rete.model.GraphicalVertex;
import org.drools.eclipse.editors.rete.model.ModelElement;
import org.drools.eclipse.editors.rete.model.VertexPropertySource;
import org.drools.reteoo.BaseVertex;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.EllipseAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.ConnectionEditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * EditPart used for Vertices
 */
class VertexEditPart extends AbstractGraphicalEditPart
    implements
    PropertyChangeListener,
    org.eclipse.gef.NodeEditPart {

    private IPropertySource  propertySource;

    private ConnectionAnchor anchor;

    /* (non-Javadoc)
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#activate()
     */
    public void activate() {
        if ( !isActive() ) {
            super.activate();
            ((ModelElement) getModel()).addPropertyChangeListener( this );
            propertySource = new VertexPropertySource( getCastedModel() );
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editparts.AbstractEditPart#createEditPolicies()
     */
    protected void createEditPolicies() {
    }

    /*(non-Javadoc)
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#createFigure()
     */
    protected IFigure createFigure() {
        return new VertexFigure( getCastedModel().getFillColor(),
                                 getCastedModel().getDrawColor() );
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#deactivate()
     */
    public void deactivate() {
        if ( isActive() ) {
            super.deactivate();
            ((ModelElement) getModel()).removePropertyChangeListener( this );
            propertySource = null;
        }
    }

    private BaseVertex getCastedModel() {
        return (BaseVertex) getModel();
    }

    private ConnectionAnchor getConnectionAnchor() {
        if ( anchor == null ) {
            if ( getModel() instanceof BaseVertex ) anchor = new EllipseAnchor( getFigure() );
            else
            // if Nodes gets extended the conditions above must be updated
            throw new IllegalArgumentException( "unexpected model" );
        }
        return anchor;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getModelSourceConnections()
     */
    protected List getModelSourceConnections() {
        return getCastedModel().getSourceConnections();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getModelTargetConnections()
     */
    protected List getModelTargetConnections() {
        return getCastedModel().getTargetConnections();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
     */
    public ConnectionAnchor getSourceConnectionAnchor(ConnectionEditPart connection) {
        return getConnectionAnchor();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.gef.NodeEditPart#getSourceConnectionAnchor(org.eclipse.gef.Request)
     */
    public ConnectionAnchor getSourceConnectionAnchor(Request request) {
        return getConnectionAnchor();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.ConnectionEditPart)
     */
    public ConnectionAnchor getTargetConnectionAnchor(ConnectionEditPart connection) {
        return getConnectionAnchor();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.gef.NodeEditPart#getTargetConnectionAnchor(org.eclipse.gef.Request)
     */
    public ConnectionAnchor getTargetConnectionAnchor(Request request) {
        return getConnectionAnchor();
    }

    /* (non-Javadoc)
     * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
     */
    public void propertyChange(PropertyChangeEvent evt) {
        String prop = evt.getPropertyName();
        if ( GraphicalVertex.SIZE_PROP.equals( prop ) || GraphicalVertex.LOCATION_PROP.equals( prop ) ) {
            refreshVisuals();
        } else if ( GraphicalVertex.SOURCE_CONNECTIONS_PROP.equals( prop ) ) {
            refreshSourceConnections();
        } else if ( GraphicalVertex.TARGET_CONNECTIONS_PROP.equals( prop ) ) {
            refreshTargetConnections();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editparts.AbstractEditPart#refreshVisuals()
     */
    protected void refreshVisuals() {
        Rectangle bounds = new Rectangle( getCastedModel().getLocation(),
                                          getCastedModel().getSize() );
        ((GraphicalEditPart) getParent()).setLayoutConstraint( this,
                                                               getFigure(),
                                                               bounds );
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.editparts.AbstractGraphicalEditPart#getAdapter(java.lang.Class)
     */
    public Object getAdapter(Class key) {
        if ( key == IPropertySource.class ) {
            return propertySource;
        }
        return super.getAdapter( key );
    }

}