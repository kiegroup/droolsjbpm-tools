package org.drools.eclipse.core;

/**
 * This represents a query. 
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class Query extends DroolsElement {

    private final String query;

    Query(Package parent, String query) {
        super(parent);
        this.query = query;
    }

	public Package getParentPackage() {
		return (Package) getParent();
	}
	
	public String getQueryName() {
		return query;
	}
	
	public int getType() {
		return QUERY;
	}
	
	public DroolsElement[] getChildren() {
		return NO_ELEMENTS;
	}

	public String toString() {
		return query;
	}

}
