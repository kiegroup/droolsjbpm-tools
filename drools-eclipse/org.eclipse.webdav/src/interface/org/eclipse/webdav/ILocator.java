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
package org.eclipse.webdav;

/**
 * Locators encapsulate a resource URL, whether it is
 * a stable URL (such as a version URL) or a dynamic URL,
 * and optional label selector.
 * <p>
 * The locator 'knows' if the resource URL is stable or not.
 * Note that a stable URL cannot also have a label selector.</p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public interface ILocator {
	/**
	 * Returns the label selector value of this locator, 
	 * or <code>null</code> if there is no label selector.
	 * <p>
	 * For certain methods (e.g. GET, PROPFIND), if the
	 * request-URL identifies a version-controlled resource,
	 * a label can be specified in a Label request header
	 * to cause the method to be applied to the version
	 * selected by that label from the version history of
	 * that version-controlled resource.</p>
	 * <p>
	 * Note that a stable URL must not have a locator label.</p>
	 *
	 * @return the label as a <code>String</code> or <code>null
	 * </code> if there is no label specified.
	 */
	public String getLabel();

	/**
	 * Returns a resource URL.
	 * <p>
	 * If there is no label specified, the URL properly identifies the
	 * resource.  However, if there is a label, the resource URL
	 * identiifes a version-controlled resource whose history contains
	 * the version whose label is the target of this locator.</p>
	 * <p>
	 * In typical usage, and with no label header, this URL may be the
	 * HTTP request URI or the destination header URI.</p>
	 *
	 * @return the String representation of the resource URL.
	 */
	public String getResourceURL();

	/**
	 * Returns whether the resource URL of this locator is stable.
	 * <p>
	 * A stable URL is a server-generated URL that cannot be moved
	 * and do not mecessarily conform to the DAV namespace.  For
	 * example, version URLs are server generated and do not have
	 * to appear in any DAV collection.  Version URLs and version
	 * history URLs are stable URLs.</p>
	 *
	 * @return true if the resource URL is stable, and false otherwise.
	 */
	public boolean isStable();
}
