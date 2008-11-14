/**
 * 
 */
package org.drools.reteoo;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

/**
 * Wraps {@link LeftInputAdapterNode} and adds visual extras like color information
 *
 */
public class LeftInputAdapterNodeVertex extends BaseVertex {

    private static final String        NODE_NAME = "LeftInputAdapterNode";

    private final LeftInputAdapterNode node;

    /**
     * Constructor
     * 
     * @param node node to be wrapped
     */
    public LeftInputAdapterNodeVertex(final LeftInputAdapterNode node) {
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
        return this.node.toString();
    }

    /* (non-Javadoc)
     * @see org.drools.reteoo.BaseNodeVertex#getFillColor()
     */
    public Color getFillColor() {
        return ColorConstants.yellow;
    }

}