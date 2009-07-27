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
public class ObjectTypeNodeVertex extends BaseVertex {

    private static final String  NODE_NAME = "ObjectTypeNode";

    private final ObjectTypeNode node;

    /**
     * Constructor
     * 
     * @param node node to be wrapped
     */
    public ObjectTypeNodeVertex(final ObjectTypeNode node) {
        super();
        this.node = node;
    }

    /* (non-Javadoc)
     * @see org.drools.reteoo.BaseNodeVertex#getHtml()
     */
    public String getHtml() {
        return NODE_NAME + " : " + this.node.getObjectType();
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
        return ColorConstants.red;
    }

    /**
     * {@link ObjectType} as {@link String}
     * 
     * @return object type as string
     */
    public String getObjectType() {
        return node.getObjectType().toString();
    }
    
    public String getExpirationOffset() {
        return String.valueOf( node.getExpirationOffset() );
    }
}