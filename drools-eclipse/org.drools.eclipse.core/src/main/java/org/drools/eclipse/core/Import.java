package org.drools.eclipse.core;

/**
 * This represents an import. 
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class Import extends DroolsElement {

    private final String importClass;

    Import(Package parent, String importClass) {
        super(parent);
        this.importClass = importClass;
    }
    
	public Package getParentPackage() {
		return (Package) getParent();
	}
	
    public String getImportClass() {
    	return importClass;
    }

	public int getType() {
		return IMPORT;
	}
	
	public DroolsElement[] getChildren() {
		return NO_ELEMENTS;
	}

	public String toString() {
		return importClass;
	}

}
