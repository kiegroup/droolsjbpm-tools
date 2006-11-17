/**
 * 
 */
package org.drools.reteoo;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

/**
 * Wraps {@link TerminalNode} and adds visual extras like color information
 *
 */
public class TerminalNodeVertex extends BaseVertex {
    
    private static final String NODE_NAME = "TerminalNode";
    
    private final TerminalNode node;

    /**
     * Constructor
     * 
     * @param node node to be wrapped
     */
    public TerminalNodeVertex(final TerminalNode node) {
        super();
        this.node = node;
    }

    /* (non-Javadoc)
     * @see org.drools.reteoo.BaseNodeVertex#getHtml()
     */
    public String getHtml() {
        return NODE_NAME+" : " + this.node.getId() + " : " + this.node.getRule();
    }

    /* (non-Javadoc)
     * @see org.drools.ide.editors.rete.model.BaseVertex#toString()
     */
    public String toString() {
        return NODE_NAME;
    }

    /* (non-Javadoc)
     * @see org.drools.reteoo.BaseNodeVertex#getFillColor()
     */
    public Color getFillColor() {
        return ColorConstants.darkGray;
    }

    /**
     * Node ID
     * 
     * @return id
     */
    public int getId() {
        return this.node.getId();
    }

    /**
     * @return
     */
    public String getRuleName() {
        return node.getRule().getName();
    }

}