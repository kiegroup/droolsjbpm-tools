/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
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
