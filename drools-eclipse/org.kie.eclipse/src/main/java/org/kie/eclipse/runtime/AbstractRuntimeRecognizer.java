/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map.Entry;
import java.util.jar.JarFile;

import org.kie.eclipse.runtime.AbstractRuntime.Version;

public abstract class AbstractRuntimeRecognizer implements IRuntimeRecognizer {

	List<String> fileList = new ArrayList<String>();
	Hashtable<String, List<Version>> products = new Hashtable<String, List<Version>>();
	
	protected void clearFiles() {
		fileList.clear();
		products.clear();
	}
	
	protected void addFile(File file) {
		// add file name to internal list
		fileList.add(file.getAbsolutePath());
		
		// identify the product
		String product = null;
        if (file.getName().startsWith("drools-")) {
        	product = "drools";
        }
        else if (file.getName().startsWith("jbpm-")) {
        	product = "jbpm";
        }
        if (product!=null && file.getName().endsWith(".jar")) {
        	// get the runtime version from manifest
        	JarFile jar = null;
        	try {
        		jar = new java.util.jar.JarFile(file);
        		for (Entry<Object, Object> a : jar.getManifest().getMainAttributes().entrySet()) {
        			if ("Bundle-Version".equals(a.getKey().toString())) {
        				Version version = new Version((String) a.getValue());
        				List<Version> productVersions = products.get(product);
        				if (productVersions!=null) {
	    					if (!productVersions.contains(version)) {
	    						productVersions.add(version);
	    					}
        				}
        				else {
        					productVersions = new ArrayList<Version>();
        					productVersions.add(version);
        					products.put(product,productVersions);
        				}
        			}
        		}
        	}
        	catch (Exception e) {
        	}
    		finally {
				try {
        			if (jar!=null)
        				jar.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
    		}
        }
	}

	protected String[] getFiles() {
        return fileList.toArray(new String[fileList.size()]);
	}
	
	@Override
	public Hashtable<String, List<Version>> getProducts() {
		return products;
	}
}
