package org.drools.eclipse.core;

/**
 * This represents a function. 
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class Process extends DroolsElement {

    private final String processId;

    Process(Package parent, String processId) {
        super(parent);
        this.processId = processId;
    }
    
	public Package getParentPackage() {
		return (Package) getParent();
	}
	
    public String getProcessId() {
    	return processId;
    }

	public int getType() {
		return PROCESS;
	}
	
	public DroolsElement[] getChildren() {
		return NO_ELEMENTS;
	}

	public String toString() {
		return processId;
	}

}
