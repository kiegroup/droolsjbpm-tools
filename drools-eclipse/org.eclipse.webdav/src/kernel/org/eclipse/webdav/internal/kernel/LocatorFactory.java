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
package org.eclipse.webdav.internal.kernel;

import org.eclipse.webdav.ILocator;
import org.eclipse.webdav.ILocatorFactory;

public class LocatorFactory implements ILocatorFactory {

	public LocatorFactory() {
		super();
	}

	public ILocator newLocator(String resourceURL) {
		return new Locator(resourceURL, null);
	}

	/**
	 * @deprecated -- not really deprecated, just a warning
	 * 	the second argument used to be the workspace URL but
	 *	is now a version label!
	 * @deprecated
	 */
	public ILocator newLocator(String resourceURL, String label) {
		return new Locator(resourceURL, label);
	}

	public ILocator newStableLocator(String resourceURL) {
		Locator locator = new Locator(resourceURL, null);
		locator.markStable();
		return locator;
	}
}
