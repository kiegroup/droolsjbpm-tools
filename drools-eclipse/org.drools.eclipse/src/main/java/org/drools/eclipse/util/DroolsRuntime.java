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

package org.drools.eclipse.util;

public class DroolsRuntime {
	public static final String ID_DROOLS_6 = "Drools 6.x";
	public static final String ID_DROOLS_5_1 = "Drools 5.1.x";
	public static final String ID_DROOLS_5 = "Drools 5.0.x";
	public static final String ID_DROOLS_4 = "Drools 4.x";

	private static String[] allIds = {
		ID_DROOLS_4, ID_DROOLS_5, ID_DROOLS_5_1, ID_DROOLS_6
	};
    private String name;
    private String path;
    private boolean isDefault;
    private String[] jars;
    private String id;

    public static String[] getIAllds() {
    	return allIds;
    }
    
    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        if (id==null && name!=null) {
        	// try to guess the ID from the name
        	if (name.contains("6"))
        		id = ID_DROOLS_6;
        	if (name.contains("5.1"))
        		id = ID_DROOLS_5_1;
        	if (name.contains("5.0"))
        		id = ID_DROOLS_5;
        	if (name.contains("4"))
        		id = ID_DROOLS_4;
        }
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
