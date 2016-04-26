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

public interface IRuntime extends Comparable<IRuntime> {

	/**
	 * Identifies the version number of this Runtime
	 * @return Runtime version number
	 */
	String getVersion();
	void setVersion(String version);
	/**
	 * Identifies the product name, either "drools", "jbpm" or "kie" or something else?
	 * @return Runtime product name
	 */
	String getProduct();
	// probably not needed:
	void setProduct(String string);
	
	String getId();
	
	/**
	 * Returns a descriptive name of this Runtime. The name, not the ID, uniquely identifies
	 * a configured Runtime; for example two Runtimes may be configured in the user's workspace
	 * and both may have the same ID (version). This is possible if two different servers are
	 * running the same version of Drools/jBPM
	 * @return Runtime name
	 */
	String getName();
	void setName(String name);
	String getPath();
	void setPath(String path);
	boolean isDefault();
	void setDefault(boolean isDefault);
	String[] getJars();
	void setJars(String[] jars);
}
