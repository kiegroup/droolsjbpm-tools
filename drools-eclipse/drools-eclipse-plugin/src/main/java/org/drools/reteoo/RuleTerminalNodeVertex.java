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
public class RuleTerminalNodeVertex extends BaseVertex {
    
    private static final String NODE_NAME = "TerminalNode";
    
    private final RuleTerminalNode node;

    /**
     * Constructor
     * 
     * @param node node to be wrapped
     */
    public RuleTerminalNodeVertex(final RuleTerminalNode node) {
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
     * @see org.drools.eclipse.editors.rete.model.BaseVertex#toString()
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