/**
 * 
 */
package org.drools.reteoo;

import org.drools.spi.Constraint;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

/**
 * Wraps {@link JoinNode} and adds visual extras like color information
 *
 */
public class JoinNodeVertex extends BaseVertex {

    private static final String NODE_NAME = "JoinNode";

    private final JoinNode      node;

    /**
     * Constructor
     * 
     * @param node node to be wrapped
     */
    public JoinNodeVertex(final JoinNode node) {
        super();
        this.node = node;
    }

    /* (non-Javadoc)
     * @see org.drools.reteoo.BaseNodeVertex#getHtml()
     */
    public String getHtml() {
        return NODE_NAME + "<BR/>" + dumpConstraints( this.node.getConstraints() );
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
        return ColorConstants.green;
    }

    /**
     * Node constraints
     * 
     * @return array of constraints
     */
    public Constraint[] getConstraints() {
        return node.getConstraints();
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