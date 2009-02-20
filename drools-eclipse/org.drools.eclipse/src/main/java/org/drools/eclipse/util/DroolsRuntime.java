package org.drools.eclipse.util;

public class DroolsRuntime {
	
	private String name;
	private String path;
	private boolean isDefault;
	private String[] jars;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String path) {
		this.path = path;
	}
	
	public boolean isDefault() {
		return isDefault;
	}
	
	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	public String[] getJars() {
		return jars;
	}

	public void setJars(String[] jars) {
		this.jars = jars;
	}
}