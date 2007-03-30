/**
 * 
 */
package org.drools.reteoo;

import org.drools.base.ClassFieldExtractor;
import org.drools.rule.LiteralConstraint;
import org.drools.spi.AlphaNodeFieldConstraint;
import org.drools.spi.Constraint;
import org.drools.spi.FieldExtractor;
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
    	AlphaNodeFieldConstraint constraint = this.node.getConstraint();
        if (constraint instanceof LiteralConstraint) {
        	LiteralConstraint literalConstraint = (LiteralConstraint) constraint;
            FieldExtractor extractor = literalConstraint.getFieldExtractor();
            if (extractor instanceof ClassFieldExtractor) {
            	ClassFieldExtractor classFieldExtractor = (ClassFieldExtractor) extractor;
            	return NODE_NAME + "<BR/>field : " + classFieldExtractor.getFieldName() + "<BR/>evaluator : " + literalConstraint.getEvaluator() + "<BR/>value :  " + literalConstraint.getField();
            }
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
    	AlphaNodeFieldConstraint constraint = this.node.getConstraint();
        if (constraint instanceof LiteralConstraint) {
        	LiteralConstraint literalConstraint = (LiteralConstraint) constraint;
            FieldExtractor extractor = literalConstraint.getFieldExtractor();
            if (extractor instanceof ClassFieldExtractor) {
            	return ((ClassFieldExtractor) extractor).getFieldName();
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
        if (constraint instanceof LiteralConstraint) {
        	LiteralConstraint literalConstraint = (LiteralConstraint) constraint;
        	return literalConstraint.getEvaluator().toString();
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
        if (constraint instanceof LiteralConstraint) {
        	LiteralConstraint literalConstraint = (LiteralConstraint) constraint;
        	return literalConstraint.getField().toString();
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