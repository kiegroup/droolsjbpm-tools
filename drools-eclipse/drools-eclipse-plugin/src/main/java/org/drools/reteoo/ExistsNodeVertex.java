/**
 * 
 */
package org.drools.reteoo;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

/**
 * Wraps {@link ExistsNode} and adds visual extras like color information
 *
 */
public class ExistsNodeVertex extends BaseVertex {

    private static final String NODE_NAME = "ExistsNode";

    private final ExistsNode    node;

    /**
     * Constructor
     * 
     * @param node node to be wrapped
     */
    public ExistsNodeVertex(final ExistsNode node) {
        super();
        this.node = node;
    }

    /* (non-Javadoc)
     * @see org.drools.reteoo.BaseNodeVertex#getHtml()
     */
    public String getHtml() {
        return NODE_NAME + " : " + this.node.getId() + " : Chared count=" + this.node.getSharedCount();
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
        return ColorConstants.cyan;
    }

    /**
     * Node ID
     * 
     * @return id
     */
    public int getId() {
        return this.node.getId();
    }

}