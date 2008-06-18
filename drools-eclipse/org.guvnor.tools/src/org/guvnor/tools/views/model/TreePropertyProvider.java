package org.guvnor.tools.views.model;

import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

public class TreePropertyProvider implements IPropertySource {
	
	private TreeObject node;
	
	public TreePropertyProvider(TreeObject node) {
		this.node = node;
	}
	
	public Object getEditableValue() {
		return null;
	}

	public IPropertyDescriptor[] getPropertyDescriptors() { 
		return new IPropertyDescriptor[] {
			new TextPropertyDescriptor("name", "Name"),
			new TextPropertyDescriptor("location", "Location"),
			new TextPropertyDescriptor("type", "Type"),
			new TextPropertyDescriptor("creationdate", "Created"),
			new TextPropertyDescriptor("lastmodified", "Last Modified")
		};
	}

	public Object getPropertyValue(Object id) {
		if (id.equals("name")) {
			return node.getName();
		}
		if (id.equals("location")) {
			return node.getFullPath();
		}
		if (id.equals("type")) {
			if (node.getNodeType() == TreeObject.Type.REPOSITORY) {
				return "repository";
			}
			if (node.getNodeType() == TreeObject.Type.PACKAGE) {
				return "directory";
			}
			if (node.getNodeType() == TreeObject.Type.RESOURCE) {
				return "file";
			}
		}
		if (id.equals("creationdate")) {
			return node.getResourceProps().getCreationDate();
		}
		if (id.equals("lastmodified")) {
			return node.getResourceProps().getLastModifiedDate();
		}
		return "";
	}

	public boolean isPropertySet(Object id) {
		// Guvnor properties are read-only, so do nothing
		return false;
	}

	public void resetPropertyValue(Object id) {
		// Guvnor properties are read-only, so do nothing
	}

	public void setPropertyValue(Object id, Object value) {
		// Guvnor properties are read-only, so do nothing
	}
}
