/**
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

package org.eclipse.webdav.dom;

import java.util.Properties;

/**
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 */
public class Namespaces {
	private String fDefaultNSName = null;
	private Properties fNSNames = null;
	private Properties fNSPrefixes = null;

	public Namespaces() {
		fNSNames = new Properties();
		fNSPrefixes = new Properties();
	}

	public Namespaces(Namespaces namespaces) {
		fDefaultNSName = namespaces.fDefaultNSName;
		fNSNames = new Properties(namespaces.fNSNames);
		fNSPrefixes = new Properties(namespaces.fNSPrefixes);
	}

	public String getDefaultNSName() {
		return fDefaultNSName;
	}

	public String getNSName(String nsPrefix) {
		return fNSNames.getProperty(nsPrefix);
	}

	public String getNSPrefix(String nsName) {
		return fNSPrefixes.getProperty(nsName);
	}

	public void putNSName(String nsPrefix, String nsName) {
		fNSNames.put(nsPrefix, nsName);
	}

	public void putNSPrefix(String nsName, String nsPrefix) {
		fNSPrefixes.put(nsName, nsPrefix);
	}

	public void setDefaultNSName(String nsName) {
		fDefaultNSName = nsName;
	}
}
