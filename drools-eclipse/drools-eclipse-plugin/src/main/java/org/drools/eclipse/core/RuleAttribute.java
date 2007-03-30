package org.drools.eclipse.core;

/**
 * This represents a rule attribute. 
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class RuleAttribute extends DroolsElement {

    private final String attributeName;
    private final Object attributeValue;

    RuleAttribute(Rule parent, String attributeName, Object attributeValue) {
    	super(parent);
        this.attributeName = attributeName;
        this.attributeValue = attributeValue;
    }

	public Rule getParentRule() {
		return (Rule) getParent();
	}
	
	public String getAttributeName() {
		return attributeName;
	}
	
	public int getType() {
		return RULE_ATTRIBUTE;
	}
	
	public DroolsElement[] getChildren() {
		return NO_ELEMENTS;
	}

	public String toString() {
		return attributeName + " = " + attributeValue;
	}

}
