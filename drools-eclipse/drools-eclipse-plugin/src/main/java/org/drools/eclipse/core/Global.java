package org.drools.eclipse.core;

/**
 * This represents a global. 
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class Global extends DroolsElement {

    private final String globalName;

    Global(Package parent, String globalName) {
        super(parent);
        this.globalName = globalName;
    }
    
	public Package getParentPackage() {
		return (Package) getParent();
	}
	
    public String getGlobalName() {
    	return globalName;
    }

	public int getType() {
		return GLOBAL;
	}
	
	public DroolsElement[] getChildren() {
		return NO_ELEMENTS;
	}

	public String toString() {
		return globalName;
	}

}
