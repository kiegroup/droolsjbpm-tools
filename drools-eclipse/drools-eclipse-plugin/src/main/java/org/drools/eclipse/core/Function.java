package org.drools.eclipse.core;

/**
 * This represents a function. 
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class Function extends DroolsElement {

    private final String functionName;

    Function(Package parent, String functionName) {
        super(parent);
        this.functionName = functionName;
    }
    
	public Package getParentPackage() {
		return (Package) getParent();
	}
	
    public String getFunctionName() {
    	return functionName;
    }

	public int getType() {
		return FUNCTION;
	}
	
	public DroolsElement[] getChildren() {
		return NO_ELEMENTS;
	}

	public String toString() {
		return functionName;
	}

}
