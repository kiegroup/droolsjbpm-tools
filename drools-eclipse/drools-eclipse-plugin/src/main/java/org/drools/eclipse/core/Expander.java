package org.drools.eclipse.core;

/**
 * This represents an expander. 
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class Expander extends DroolsElement {

	private final String expander;

	Expander(Package parent, String expander) {
		super(parent);
		this.expander = expander;
	}
	
	public Package getParentPackage() {
		return (Package) getParent();
	}
	
	public String getExpander() {
		return expander;
	}

	public int getType() {
		return EXPANDER;
	}
	
	public DroolsElement[] getChildren() {
		return NO_ELEMENTS;
	}

	public String toString() {
		return expander;
	}

}
