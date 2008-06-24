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
 * An element editor for the WebDAV propstat element. See RFC2518
 * section 12.9.1.1 for the element's definition.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 *
 * @see org.eclipse.webdav.http.client.Response
 * @see Prop
 */
public class PropStat extends ElementEditor {
	/**
	 * An ordered collection of the element names of the propstat
	 * element's children.
	 */
	protected static final String[] childNames = new String[] {"prop", "status", "responsedescription"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	/**
	 * Creates a new editor on the given WebDAV propstat element. The
	 * element is assumed to be well formed.
	 *
	 * @param root a propstat element
	 * @throws        MalformedElementException if there is reason to
	 *                believe that the element is not well formed
	 */
	public PropStat(Element root) throws MalformedElementException {
		super(root, "propstat"); //$NON-NLS-1$
	}

	/**
	 * Returns this propstat's prop.
	 *
	 * @return this propstat's prop
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed
	 */
	public Prop getProp() throws MalformedElementException {
		Element prop = getFirstChild(root, "prop"); //$NON-NLS-1$
		ensureNotNull(Policy.bind("ensure.missingPropElmt"), prop); //$NON-NLS-1$
		return new Prop(prop);
	}

	/**
	 * Returns this propstat's response description.
	 *
	 * @return this propstat's response description
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed
	 */
	public String getResponseDescription() throws MalformedElementException {
		return getChildText(root, "responsedescription", false); //$NON-NLS-1$
	}

	/**
	 * Returns this propstat's status.
	 *
	 * @return this propstat's status
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed
	 */
	public String getStatus() throws MalformedElementException {
		String status = getChildText(root, "status", false); //$NON-NLS-1$
		ensureNotNull(Policy.bind("ensure.missingStatusElmt"), status); //$NON-NLS-1$
		return status;
	}

	/**
	 * Returns this propstat's status code.
	 *
	 * @return this propstat's status code
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed
	 */
	public int getStatusCode() throws MalformedElementException {
		return new Status(getStatus()).getStatusCode();
	}

	/**
	 * Creates and sets a new prop on this propstat and returns an editor on
	 * it.
	 *
	 * @return an editor on a new prop element
	 */
	public Prop setProp() {
		Element prop = setChild(root, "prop", childNames, true); //$NON-NLS-1$
		try {
			return new Prop(prop);
		} catch (MalformedElementException e) {
			Assert.isTrue(false, Policy.bind("assert.internalError")); //$NON-NLS-1$
			return null; // Never reached.
		}
	}

	/**
	 * Sets this propstat's response description to the given value. If the
	 * value is <code>null</code> and a response description has already
	 * been set, it is removed.
	 *
	 * @param value a response description, or <code>null</code>
	 */
	public void setResponseDescription(String value) {
		if (value == null) {
			Element child = getLastChild(root, "responsedescription"); //$NON-NLS-1$
			if (child != null)
				root.removeChild(child);
		} else
			setChild(root, "responsedescription", value, childNames, false); //$NON-NLS-1$
	}

	/**
	 * Sets the status on this propstat to the given status. The status
	 * must not be <code>null</code>.
	 *
	 * @param status the status for this propstat
	 */
	public void setStatus(String status) {
		Assert.isNotNull(status);
		setChild(root, "status", status, childNames, true); //$NON-NLS-1$
	}
}
