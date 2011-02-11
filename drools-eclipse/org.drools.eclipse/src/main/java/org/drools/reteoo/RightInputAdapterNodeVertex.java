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

package org.drools.reteoo;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

/**
 * Wraps {@link RightInputAdapterNode} and adds visual extras like color information
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
