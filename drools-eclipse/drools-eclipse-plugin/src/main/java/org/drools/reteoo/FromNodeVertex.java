/**
 * 
 */
package org.drools.reteoo;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

/**
 * Wraps {@link FromNode} and adds visual extras like color information
 *
 */
public class FromNodeVertex extends BaseVertex {

    private static final String NODE_NAME = "FromNode";

    private final FromNode      node;

    /**
     * Constructor
     * 
     * @param node node to be wrapped
     */
    public FromNodeVertex(final FromNode node) {
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
        return ColorConstants.lightBlue;
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