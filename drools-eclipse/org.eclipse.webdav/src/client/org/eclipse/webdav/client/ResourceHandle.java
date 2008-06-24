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
package org.eclipse.webdav.client;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import org.eclipse.webdav.ILocator;
import org.eclipse.webdav.internal.kernel.*;

/**
 * The <code>ResourceHandle</code> class represents an
 * ordinary resource in the system. It subclasses <code>AbstractResourceHandle</code>
 * and overrides some of its methods in order to provide specific behaviour for
 * this resource type.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public class ResourceHandle extends AbstractResourceHandle {

	/**
	 * Creates a new <code>ResourceHandle</code> from the given
	 * <code>DAVClient</code> and <code>Locator</code>.
	 *
	 * @param davClient
	 * @param locator
	 */
	public ResourceHandle(DAVClient davClient, ILocator locator) {
		super(davClient, locator);
	}

	/**
	 * Check out this resource. Returns a resource handle on the checked out
	 * version selector, or the working resource if a version is checked out.
	 */
	public AbstractResourceHandle checkOut() throws DAVException {
		ILocator locator = protectedCheckOut();
		return new ResourceHandle(davClient, locator);
	}

	/**
	 * Persistently create this resource instance in the repository.
	 * <p>
	 * Note that the usual method for creating an instance of a non-collection
	 * resource would be setContent(InputStream).
	 *
	 * @exception DAVException if there was a problem creating this resource
	 * @see AbstractResourceHandle#setContent(InputStream)
	 */
	public void create() throws DAVException {
		setContent(new ByteArrayInputStream(new byte[0]));
	}

	/**
	 * Check to see if the resource is an activity resource.
	 * <p>
	 * The resource is an activity resource if it has
	 * &lt;DAV:subactivity-set&gt; in the
	 * &lt;DAV:supported-live-properties-set&gt;.</p>
	 *
	 * @return <code>true</code> if the resource is an activity
	 * and <code>false</code> otherwise.
	 * @throws DAVException if a problem occurs determining the state
	 * of the resource.
	 */
	public boolean isActivity() throws DAVException {
		return supportsLiveProperty(DAV_SUBACTIVITY_SET);
	}

	/**
	 * Check to see if the resource is a baseline resource.
	 * <p>
	 * The resource is a baseline resource if it has
	 * &lt;DAV:baseline-collection&gt; in the
	 * &lt;DAV:supported-live-properties-set&gt;.</p>
	 *
	 * @return <code>true</code> if the resource is a baseline
	 * and <code>false</code> otherwise.
	 * @throws DAVException if a problem occurs determining the state
	 * of the resource.
	 */
	public boolean isBaseline() throws DAVException {
		return supportsLiveProperty(DAV_BASELINE_COLLECTION);
	}

	/**
	 * Check to see if the resource is a version-controlled configuration
	 * resource.
	 * <p>
	 * The resource is a version-controlled configuration resource if it has
	 * &lt;DAV:baseline-controlled-collection&gt; in the
	 * &lt;DAV:supported-live-properties-set&gt;.</p>
	 *
	 * @return <code>true</code> if the resource is a version-controlled
	 * configuration and <code>false</code> otherwise.
	 * @throws DAVException if a problem occurs determining the state
	 * of the resource.
	 */
	public boolean isVersionControlledConfiguration() throws DAVException {
		return supportsLiveProperty(DAV_BASELINE_CONTROLLED_COLLECTION);
	}

	/**
	 * Check to see if the resource is a version history resource.
	 * <p>
	 * The resource is a version history resource if it has
	 * &lt;DAV:root-version&gt; in the
	 * &lt;DAV:supported-live-properties-set&gt;.</p>
	 *
	 * @return <code>true</code> if the resource is a version history
	 * and <code>false</code> otherwise.
	 * @throws DAVException if a problem occurs determining the state
	 * of the resource.
	 */
	public boolean isVersionHistory() throws DAVException {
		return supportsLiveProperty(DAV_ROOT_VERSION);
	}
}
