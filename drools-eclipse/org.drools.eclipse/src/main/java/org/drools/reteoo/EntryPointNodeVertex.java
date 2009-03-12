/**
 * 
 */
package org.drools.reteoo;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

/**
 * Wraps {@link ObjectTypeNode} and adds visual extras like color information
 *
 */
public class EntryPointNodeVertex extends BaseVertex {

    private static final String NODE_NAME = "EntryPointNode";

    private final EntryPointNode node;

    /**
     * Constructor
     * 
     * @param node node to be wrapped
     */
    public EntryPointNodeVertex(final EntryPointNode node) {
        super();
        this.node = node;
    }

    /* (non-Javadoc)
     * @see org.drools.reteoo.BaseNodeVertex#getHtml()
     */
    public String getHtml() {
        return NODE_NAME + ":" + node.getEntryPoint();
    }

    /* (non-Javadoc)
     * @see org.drools.eclipse.editors.rete.model.BaseVertex#toString()
     */
    public String toString() {
        return NODE_NAME + ":" + node.getEntryPoint();
    }

    /* (non-Javadoc)
     * @see org.drools.reteoo.BaseNodeVertex#getFillColor()
     */
    public Color getFillColor() {
        return ColorConstants.darkGreen;
    }

    /**
     * Node ID
     * 
     * @return node id
     */
    public int getId() {
        return node.getId();
    }
    
    public String getEntryPointName() {
    	return node.getEntryPoint().toString();
    }

}