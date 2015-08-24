/*
 * Copyright 2010 JBoss Inc
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
import java.util.List;
import java.util.Map.Entry;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.Path;

public class DefaultRuntimeRecognizer implements IRuntimeRecognizer {

	// TODO: determine actual product ID from discovered jars
	String version = "";
	String product = "";
	
    public String[] recognizeJars(String path) {
        List<String> list = new ArrayList<String>();
        if (path != null) {
            File file = (new Path(path)).toFile();
            addJarNames(file, list);
        }
        return list.toArray(new String[list.size()]);
    }

    private void addJarNames(File file, List<String> list) {
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            if (files[i].isDirectory() && "lib".equals(files[i].getName())) {
                addJarNames(files[i], list);
            } else if (files[i].getPath().endsWith(".jar")) {
                list.add(files[i].getAbsolutePath());
                File jarFile = files[i];
                if (jarFile.getName().startsWith("drools-core")) {
                	// get the runtime version from drools-core.jar
                	JarFile jar = null;
                	try {
    	        		jar = new java.util.jar.JarFile(jarFile);
    	        		for (Entry<Object, Object> a : jar.getManifest().getMainAttributes().entrySet()) {
    	        			if ("Bundle-Version".equals(a.getKey().toString())) {
    	        				// only keep major.minor.patch numbers
    	        				String v[] = ((String) a.getValue()).split("\\.");
    	        				if (v.length>0)
    	        					version = v[0];
    	        				if (v.length>1)
    	        					version += "." + v[1];
    	        				if (v.length>2)
    	        					version += "." + v[2];
    	        				product = "drools";
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
        }
    }

	@Override
	public String getVersion() {
		return version;
	}
	
	@Override
	public String getProduct() {
		return product;
	}
}
