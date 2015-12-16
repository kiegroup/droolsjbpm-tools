/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

import org.drools.core.reteoo.QueryTerminalNode;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

/**
 * Wraps {@link org.drools.core.reteoo.QueryTerminalNode} and adds visual extras like color information
 */
public class QueryTerminalNodeVertex extends BaseVertex {
    
    private static final String NODE_NAME = "QueryTerminalNode";
    
    private final QueryTerminalNode node;

    /**
     * Constructor
     * 
     * @param node node to be wrapped
     */
    public QueryTerminalNodeVertex(final QueryTerminalNode node) {
        super();
        this.node = node;
    }

    /* (non-Javadoc)
     * @see org.drools.core.reteoo.BaseNodeVertex#getHtml()
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
     * @see org.drools.core.reteoo.BaseNodeVertex#getFillColor()
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
    public String getQueryName() {
        return node.getRule().getName();
    }

}
