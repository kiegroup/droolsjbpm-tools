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
 * An element editor for the WebDAV baseline control element. See
 * the latest Delta-V protocol document for the element's definition.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public class BaselineControl extends ElementEditor {
	/**
	 * An ordered collection of the element names of the resourceid
	 * element's children.
	 */
	protected static final String[] childNames = new String[] {"href"}; //$NON-NLS-1$

	/**
	 * Creates a new editor on the given WebDAV baseline control element. The
	 * element is assumed to be well formed.
	 *
	 * @param root a baseline control element
	 * @throws        MalformedElementException if there is reason to
	 *                believe that the element is not well formed
	 */
	public BaselineControl(Element root) throws MalformedElementException {
		super(root, "baseline-control"); //$NON-NLS-1$
	}

	/**
	 * Answer the href passed in the body of the baseline control element.
	 */
	public String getHref() throws MalformedElementException {
		String href = getChildText(root, "href", true); //$NON-NLS-1$
		ensureNotNull(Policy.bind("ensure.missingHrefElmt"), href); //$NON-NLS-1$
		return decodeHref(href);
	}

	/**
	 * Sets this baseline control elment description to the given href.
	 *
	 * @param href the href of the baseline.
	 */
	public void setHref(String href) {
		Assert.isNotNull(href);
		setChild(root, "href", encodeHref(href), childNames, true); //$NON-NLS-1$
	}
}
