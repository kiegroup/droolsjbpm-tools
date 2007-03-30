/**
 * 
 */
package org.drools.reteoo;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

/**
 * Wraps {@link RightInputAdapterNode} and adds visual extras like color information
 *
 */
public class RightInputAdapterNodeVertex extends BaseVertex {

    private static final String NODE_NAME = "RightInputAdapterNode";

    /**
     * Constructor
     * 
     * @param node node to be wrapped
     */
    public RightInputAdapterNodeVertex(final RightInputAdapterNode node) {
        super();
    }

    public String getHtml() {
        return NODE_NAME;
    }

    public String toString() {
        return NODE_NAME;
    }

    public Color getFillColor() {
        return ColorConstants.orange;
    }

}