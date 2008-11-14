package org.drools.eclipse.core;

/**
 * This represents a template. 
 *
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class Template extends DroolsElement {

    private final String templateName;

    Template(Package parent, String templateName) {
        super(parent);
        this.templateName = templateName;
    }

	public Package getParentPackage() {
		return (Package) getParent();
	}
	
	public String getTemplateName() {
		return templateName;
	}
	
	public int getType() {
		return TEMPLATE;
	}
	
	public DroolsElement[] getChildren() {
		return NO_ELEMENTS;
	}

	public String toString() {
		return templateName;
	}

}
