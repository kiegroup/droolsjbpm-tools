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
