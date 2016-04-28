/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.eclipse.runtime;

public abstract class AbstractRuntime implements IRuntime {

	protected String version;
	protected String name;
	protected String path;
	protected boolean isDefault;
	protected String[] jars;

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
	
	public String getId() {
		return createRuntimeId(getProduct(), getVersion());
	}

    public static String createRuntimeId(String product, String version) {
    	return product + "_" + version;
    }

    public static String getProductFromId(String runtimeId) {
    	int i = runtimeId.lastIndexOf("_");
    	if (i>0) {
    		return runtimeId.substring(0,i);
    	}
    	return null;
    }

    public static String getVersionFromId(String runtimeId) {
    	int i = runtimeId.lastIndexOf("_");
    	if (i>0) {
    		return runtimeId.substring(i+1);
    	}
    	return null;
    }
    
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IRuntime) {
			IRuntime that = ((IRuntime)obj);
			if (this.getProduct().equals(that.getProduct()) &&
					this.getVersion()!=null && that.getVersion()!=null) {
				String thisVersion[] = this.getVersion().split("\\.");
				String thatVersion[] = that.getVersion().split("\\.");
				if (thisVersion.length>2 && thatVersion.length>2) {
					if (thisVersion[0].equals(thatVersion[0]) &&
							thisVersion[1].equals(thatVersion[1])) {
						// major and minor versions match.
						// if update version number of either is "x" it's a match,
						// otherwise the update version numbers must match
						if ("x".equalsIgnoreCase(thisVersion[2]) || "x".equalsIgnoreCase(thatVersion[2]))
							return true;
						return thisVersion[2].equals(thatVersion[2]);
					}
				}
			}
		}
		return super.equals(obj);
	}

	@Override
	public int compareTo(IRuntime that) {
		int i = getProduct().compareTo(that.getProduct());
		if (i==0) {
			i = getVersion()==null ? -1 : getVersion().compareTo(that.getVersion());
		}
		return i;
	}
}
