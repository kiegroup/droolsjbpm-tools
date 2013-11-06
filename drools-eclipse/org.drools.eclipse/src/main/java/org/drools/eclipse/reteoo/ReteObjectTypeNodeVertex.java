/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.eclipse.reteoo;

import org.drools.core.reteoo.ReteObjectTypeNode;
import org.drools.core.spi.ObjectType;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

/**
 * Wraps {@link org.drools.core.reteoo.ObjectTypeNode} and adds visual extras like color information
 */
public class ReteObjectTypeNodeVertex extends BaseVertex {

    private static final String  NODE_NAME = "ReteObjectTypeNode";

    private final ReteObjectTypeNode node;

    /**
     * Constructor
     * 
     * @param node node to be wrapped
     */
    public ReteObjectTypeNodeVertex(final ReteObjectTypeNode node) {
        super();
        this.node = node;
    }

    /* (non-Javadoc)
     * @see org.drools.core.reteoo.BaseNodeVertex#getHtml()
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
     * @see org.drools.core.reteoo.BaseNodeVertex#getFillColor()
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
