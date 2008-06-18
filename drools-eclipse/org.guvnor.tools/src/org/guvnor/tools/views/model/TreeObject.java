package org.guvnor.tools.views.model;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.ui.views.properties.IPropertySource;
import org.guvnor.tools.GuvnorRepository;
import org.guvnor.tools.utils.webdav.ResourceProperties;

public class TreeObject implements IAdaptable {
	
	private GuvnorRepository theRep;
	
	public enum Type {
		NONE,
		REPOSITORY,
		PACKAGE,
		RESOURCE
	}
	
	private String 		name;
	private Type 		nodeType;
	private TreeParent 	parent;
	
	private ResourceProperties props;
	
	public TreeObject(String name, Type nodeType) {
		this.name = name;
		this.nodeType = nodeType;
	}
	public String getName() {
		return name;
	}
	public Type getNodeType() {
		return nodeType;
	}
	public void setParent(TreeParent parent) {
		this.parent = parent;
	}
	public TreeParent getParent() {
		return parent;
	}
	public String toString() {
		return getName();
	}
	
	private TreePropertyProvider propProvider;
	
	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		if (adapter == IPropertySource.class) {
			if (propProvider == null) {
				propProvider = new TreePropertyProvider(this);
			}
			return propProvider;
		}
	    return null;
	}
	
	public ResourceProperties getResourceProps() {
		return props;
	}
	public void setResourceProps(ResourceProperties props) {
		this.props = props;
	}
	public void setGuvnorRepository(GuvnorRepository theRep) {
		this.theRep = theRep;
	}
	public GuvnorRepository getGuvnorRepository() {
		return theRep;
	}
	public String getFullPath() {
		if (props.getBase().trim().length() > 0) {
			if (props.getBase().endsWith("/")) {
				return props.getBase() + getName();
			} else {
				return props.getBase() + "/" + getName();
			}
		} else {
			return getName();
		}
	}
}
