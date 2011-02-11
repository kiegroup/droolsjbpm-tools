/*
 * Copyright 2010 JBoss Inc
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

package org.drools.eclipse.editors.rete.commands;

import org.drools.reteoo.BaseVertex;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.RequestConstants;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.ChangeBoundsRequest;

/**
 * A command to move a vertex.
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
