package org.kie.eclipse.runtime;

public abstract class AbstractRuntime implements IRuntime {

	private String version;
	private String name;
	private String path;
	private boolean isDefault;
	private String[] jars;

	@Override
	public String getVersion() {
		return version;
	}

	@Override
	public void setVersion(String version) {
		this.version = version;
	}
	
	@Override
	public String getName() {
		return name;
	}
	
	@Override
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String getPath() {
		return path;
	}
	
	@Override
	public void setPath(String path) {
		this.path = path;
	}
	
	@Override
	public boolean isDefault() {
		return isDefault;
	}
	
	@Override
	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}

	@Override
	public String[] getJars() {
		return jars;
	}

	@Override
	public void setJars(String[] jars) {
		this.jars = jars;
	}
    
    public String toString() {
    	return getName();
    }

	abstract public String getProduct();
	
	public void setProduct(String string) {
	}
}
