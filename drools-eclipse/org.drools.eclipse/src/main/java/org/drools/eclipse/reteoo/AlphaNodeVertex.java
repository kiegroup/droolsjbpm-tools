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

import org.drools.core.base.ClassFieldReader;
import org.drools.core.rule.constraint.MvelConstraint;
import org.drools.core.spi.AlphaNodeFieldConstraint;
import org.drools.core.spi.Constraint;
import org.drools.core.spi.FieldValue;
import org.drools.core.spi.InternalReadAccessor;
import org.drools.core.reteoo.AlphaNode;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

/**
 * Wraps {@link org.drools.core.reteoo.AlphaNode} and adds visual extras like color information
 */
public class AlphaNodeVertex extends BaseVertex {

    private final AlphaNode node;

    private final String    NODE_NAME = "AlphaNode";

    /**
     * Constructor
     * 
     * @param node node to be wrapped
     */
    public AlphaNodeVertex(final AlphaNode node) {
        super();
        this.node = node;
    }

    /* (non-Javadoc)
     * @see org.drools.core.reteoo.BaseNodeVertex#getHtml()
     */
    public String getHtml() {
        AlphaNodeFieldConstraint constraint = this.node.getConstraint();
        if (constraint instanceof MvelConstraint) {
        	MvelConstraint mvelConstraint = (MvelConstraint) constraint;
            return NODE_NAME + "<BR/>expression : " + mvelConstraint.toString();
        }
        return NODE_NAME + "<BR/>";
    }

    /* (non-Javadoc)
     * @see org.drools.eclipse.editors.rete.model.BaseVertex#toString()
     */
    public String toString() {
        return this.node.toString();
    }

    /* (non-Javadoc)
     * @see org.drools.core.reteoo.BaseNodeVertex#getFillColor()
     */
    public Color getFillColor() {
        return ColorConstants.blue;
    }

    /**
     * Constraint has field extractor and this method is returning fieldName
     * it.
     * 
     * @return field name
     */
    public String getFieldName() {
        AlphaNodeFieldConstraint constraint = this.node.getConstraint();
        if (constraint instanceof MvelConstraint) {
        	MvelConstraint mvelConstraint = (MvelConstraint) constraint;
            InternalReadAccessor accessor = mvelConstraint.getFieldExtractor();
            if (accessor instanceof ClassFieldReader) {
                return ((ClassFieldReader) accessor).getFieldName();
            }
        }
        return null;
    }

    /**
     * Constraint's evaluator string
     * 
     * @return evaluator string
     */
    public String getEvaluator() {
        AlphaNodeFieldConstraint constraint = this.node.getConstraint();
        if (constraint instanceof MvelConstraint) {
        	MvelConstraint mvelConstraint = (MvelConstraint) constraint;
            return mvelConstraint.toString();
        }
        return null;
    }

    /**
     * Constraint field string
     * 
     * @return field string
     */
    public String getValue() {
        AlphaNodeFieldConstraint constraint = this.node.getConstraint();
        if (constraint instanceof MvelConstraint) {
        	MvelConstraint mvelConstraint = (MvelConstraint) constraint;
        	FieldValue field = mvelConstraint.getField();
            return field != null ? field.toString() : null;
        }
        return null;
    }

    /**
     * Constraint
     * 
     * @return constraint
     */
    public Constraint getConstraint() {
        return this.node.getConstraint();
    }

}
