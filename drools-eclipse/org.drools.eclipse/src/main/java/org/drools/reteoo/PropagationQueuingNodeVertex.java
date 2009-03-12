/**
 * 
 */
package org.drools.reteoo;

import org.drools.spi.ObjectType;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

/**
 * Wraps {@link ObjectTypeNode} and adds visual extras like color information
 *
 */
public class PropagationQueuingNodeVertex extends BaseVertex {

    private static final String NODE_NAME = "PropagationQueingNode";

    private final PropagationQueuingNode node;

    /**
     * Constructor
     * 
     * @param node node to be wrapped
     */
    public PropagationQueuingNodeVertex(final PropagationQueuingNode node) {
        super();
        this.node = node;
    }

    /* (non-Javadoc)
     * @see org.drools.reteoo.BaseNodeVertex#getHtml()
     */
    public String getHtml() {
        return NODE_NAME;
    }

    /* (non-Javadoc)
     * @see org.drools.eclipse.editors.rete.model.BaseVertex#toString()
     */
    public String toString() {
        return NODE_NAME;
    }

    /* (non-Javadoc)
     * @see org.drools.reteoo.BaseNodeVertex#getFillColor()
     */
    public Color getFillColor() {
        return ColorConstants.darkBlue;
    }

    /**
     * Node ID
     * 
     * @return node id
     */
    public int getId() {
        return node.getId();
    }

}