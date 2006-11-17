/**
 * 
 */
package org.drools.reteoo;

import org.drools.base.ClassFieldExtractor;
import org.drools.rule.LiteralConstraint;
import org.drools.spi.Constraint;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.swt.graphics.Color;

/**
 * Wraps {@link AlphaNode} and adds visual extras like color information
 *
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
     * @see org.drools.reteoo.BaseNodeVertex#getHtml()
     */
    public String getHtml() {
        final LiteralConstraint constraint = (LiteralConstraint) this.node.getConstraint();
        final ClassFieldExtractor extractor = (ClassFieldExtractor) constraint.getFieldExtractor();
        return NODE_NAME + "<BR/>field : " + extractor.getFieldName() + "<BR/>evaluator : " + constraint.getEvaluator() + "<BR/>value :  " + constraint.getField();
    }

    /* (non-Javadoc)
     * @see org.drools.ide.editors.rete.model.BaseVertex#toString()
     */
    public String toString() {
        return this.node.toString();
    }

    /* (non-Javadoc)
     * @see org.drools.reteoo.BaseNodeVertex#getFillColor()
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
        LiteralConstraint constraint = (LiteralConstraint) this.node.getConstraint();
        ClassFieldExtractor extractor = (ClassFieldExtractor) constraint.getFieldExtractor();
        return extractor.getFieldName();
    }

    /**
     * Constraint's evaluator string
     * 
     * @return evaluator string
     */
    public String getEvaluator() {
        LiteralConstraint constraint = (LiteralConstraint) this.node.getConstraint();
        return constraint.getEvaluator().toString();
    }

    /**
     * Constraint field string
     * 
     * @return field string
     */
    public String getValue() {
        LiteralConstraint constraint = (LiteralConstraint) this.node.getConstraint();
        return constraint.getField().toString();
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