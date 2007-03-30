package org.drools.eclipse.editors.rete.commands;

import org.drools.reteoo.BaseVertex;
import org.eclipse.draw2d.geometry.Rectangle;

import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;

/**
 * A command to move a vertex.
 * 
 */
public class NodeSetConstraintCommand extends Command {

    /** Stores the new size and location. */
    private final Rectangle           newBounds;

    /** Stores the old size and location. */
    private Rectangle                 oldBounds;

    /** A request to move/resize an edit part. */
    private final ChangeBoundsRequest request;

    /** BaseVertex to manipulate. */
    private final BaseVertex          vertex;

    /**
     * Create a command that can resize and/or move a vertex.
     *  
     * @param vertex the vertex to manipulate
     * @param req       the move request
     * @param newBounds the new location. size is ignored
     * @throws IllegalArgumentException if any of the parameters is null
     */
    public NodeSetConstraintCommand(BaseVertex vertex,
                                    ChangeBoundsRequest req,
                                    Rectangle newBounds) {
        if ( vertex == null || req == null || newBounds == null ) {
            throw new IllegalArgumentException();
        }
        this.vertex = vertex;
        this.request = req;
        this.newBounds = newBounds.getCopy();
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#canExecute()
     */
    public boolean canExecute() {
        Object type = request.getType();
        return (RequestConstants.REQ_MOVE.equals( type ) || RequestConstants.REQ_MOVE_CHILDREN.equals( type ));
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#execute()
     */
    public void execute() {
        oldBounds = new Rectangle( vertex.getLocation(),
                                   vertex.getSize() );
        redo();
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#redo()
     */
    public void redo() {
        vertex.setSize( newBounds.getSize() );
        vertex.setLocation( newBounds.getLocation() );
    }

    /* (non-Javadoc)
     * @see org.eclipse.gef.commands.Command#undo()
     */
    public void undo() {
        vertex.setSize( oldBounds.getSize() );
        vertex.setLocation( oldBounds.getLocation() );
    }
}
