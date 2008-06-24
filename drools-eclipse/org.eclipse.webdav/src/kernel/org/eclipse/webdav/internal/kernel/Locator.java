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
import org.eclipse.webdav.internal.kernel.utils.Assert;

/**
 * A <code>LocatorImpl</code> uniquely identifies a DAV
 * a resource.
 * <p>
 * This class implements the <code>Locator</code>
 * interface.</p>
 *
 * @see Locator
 */
public class Locator implements ILocator {

	protected String resourceURL;
	protected String label;
	protected boolean isStable;

	public Locator(String resourceURL, String label) {
		super();
		Assert.isLegal(resourceURL != null);
		this.resourceURL = resourceURL;
		this.label = label;
		this.isStable = false;
	}

	/**
	 * Return a boolean value indicating whether or not this locator
	 * and the given object are equal.
	 *
	 * @param obj the object to compare against
	 * @return equality indicator
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof ILocator))
			return false;
		ILocator locator = (ILocator) obj;
		if (!resourceURL.equals(locator.getResourceURL()))
			return false;
		if (label == null)
			return locator.getLabel() == null;
		return label.equals(locator.getLabel());
	}

	public int hashCode() {
		return resourceURL.hashCode();
	}

	public String getLabel() {
		return label;
	}

	public String getResourceURL() {
		return resourceURL;
	}

	public boolean isStable() {
		return isStable;
	}

	public void markStable() {
		// Stable URLs cannot not have a label.
		Assert.isTrue(label == null);
		isStable = true;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[Locator "); //$NON-NLS-1$
		buffer.append(resourceURL);
		if (label != null) {
			buffer.append("\n  label: "); //$NON-NLS-1$
			buffer.append(label);
		}
		if (isStable)
			buffer.append("\n  stable"); //$NON-NLS-1$
		buffer.append("]"); //$NON-NLS-1$
		return buffer.toString();
	}
}
