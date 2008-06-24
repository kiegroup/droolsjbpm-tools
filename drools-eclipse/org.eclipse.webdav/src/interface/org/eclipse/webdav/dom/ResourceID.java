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

import org.eclipse.webdav.Policy;
import org.w3c.dom.Element;

/**
 * An element editor for the WebDAV resourceid element. See INTERNET
 * DRAFT draft-ietf-webdav-binding-protocol-02 section 13.2 for the
 * element's definition.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public class ResourceID extends Property {
	/**
	 * An ordered collection of the element names of the resourceid
	 * element's children.
	 */
	protected static final String[] childNames = new String[] {"href"}; //$NON-NLS-1$

	/**
	 * Creates a new editor on the given WebDAV resourceid element. The
	 * element is assumed to be well formed.
	 *
	 * @param root a resourceid element
	 * @throws        MalformedElementException if there is reason to
	 *                believe that the element is not well formed
	 */
	public ResourceID(Element root) throws MalformedElementException {
		super(root, "resourceid"); //$NON-NLS-1$
	}

	/**
	 * Returns this resourceid's href.
	 *
	 * @return this resourceid's href
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed
	 */
	public String getHref() throws MalformedElementException {
		String href = getChildText(root, "href", true); //$NON-NLS-1$
		ensureNotNull(Policy.bind("ensure.missingHrefElmt"), href); //$NON-NLS-1$
		return decodeHref(href);
	}

	/**
	 * Sets this resourceid's href to the given href.
	 *
	 * @param href the href to set this resourceid's href to
	 */
	public void setHref(String href) {
		Assert.isNotNull(href);
		setChild(root, "href", encodeHref(href), childNames, true); //$NON-NLS-1$
	}
}
