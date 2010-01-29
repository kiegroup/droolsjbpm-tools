package org.drools.eclipse.core;

import org.eclipse.core.resources.IFile;

/**
 * This represents a drools model element.  It is linked to its direct
 * parents and its children. If relevant, Drools model elements refer
 * to the file they are defined in and the offset and length of that
 * element in the file. 
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public abstract class DroolsElement {

	public static final int RULESET = 0;
	public static final int PACKAGE = 1;
	public static final int RULE = 2;
	public static final int QUERY = 3;
	public static final int FUNCTION = 4;
	public static final int TEMPLATE = 5;
	public static final int EXPANDER = 6;
	public static final int GLOBAL = 7;
	public static final int IMPORT = 8;
	public static final int RULE_ATTRIBUTE = 9;
    public static final int PROCESS = 10;
    
    public static final int DEFAULT_RULE_GROUP = 11;
    public static final int ACTIVATION_GROUP = 12;
    public static final int AGENDA_GROUP = 13;
    public static final int RULEFLOW_GROUP = 14;
	
	protected static final DroolsElement[] NO_ELEMENTS = new DroolsElement[0];
	
	private DroolsElement parent;
	private IFile file;
	private int offset;
	private int length;
	
	protected DroolsElement(DroolsElement parent) {
		this.parent = parent;
	}
	
	public abstract int getType();
	
	public DroolsElement getParent() {
		return parent;
	}
	
	public abstract DroolsElement[] getChildren();

	public IFile getFile() {
		return file;
	}
	
	public int getOffset() {
		return offset;
	}

	public int getLength() {
		return length;
	}

	// These are helper methods for creating the model and should not
	// be used directly.  Use DroolsModelBuilder instead.

	void setFile(IFile file, int offset, int length) {
		this.file = file;
		this.offset = offset;
		this.length = length;
	}

}
