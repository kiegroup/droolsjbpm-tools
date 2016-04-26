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

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IRuntime)
		if (this.getId().equals(((IRuntime)obj).getId()))
			return true;
		return super.equals(obj);
	}
}
