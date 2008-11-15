package org.drools.eclipse.editors.rete.model;

import org.drools.reteoo.BaseVertex;

/**
 * A connection between two distinct vertices.
 */
public class Connection extends ModelElement {

    private boolean    isConnected;

    private BaseVertex source;

    private BaseVertex target;

    /** 
     * Creating a connection between two distinct vertices.
     * 
     * @param source a source endpoint
     * @param target a target endpoint
     * @throws IllegalArgumentException if any of the parameters are null or source == target
     */
    public Connection(BaseVertex source,
                      BaseVertex target) {
        this.source = source;
        this.target = target;
        source.addConnection( this );
        target.addConnection( this );
        isConnected = true;
    }

    /** 
     * Disconnect this connection from the vertices it is attached to.
     */
    public void disconnect() {
        if ( isConnected ) {
            source.removeConnection( this );
            target.removeConnection( this );
            isConnected = false;
        }
    }

    /**
     * Returns the source endpoint of this connection.
     * 
     * @return BaseVertex vertex
     */
    public BaseVertex getSource() {
        return source;
    }

    /**
     * Returns the target endpoint of this connection.
     * 
     * @return BaseVertex vertex
     */
    public BaseVertex getTarget() {
        return target;
    }

    /**
     * Gets opposite of specified vertex.
     * 
     * Returning <code>null</code> if specified not does not belong into this connection.
     * 
     * @param vertex
     * @return opposite of vertex
     */
    public BaseVertex getOpposite(BaseVertex vertex) {
        // If null or not part of this connection
        if ( vertex == null || (!vertex.equals( getSource() ) && !vertex.equals( getTarget() )) ) {
            return null;
        }
        if ( vertex.equals( getSource() ) ) {
            return getTarget();
        }
        return getSource();
    }

}