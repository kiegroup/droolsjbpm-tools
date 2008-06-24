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
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An element editor for the WebDAV propfind element. See RFC2518
 * section 12.14 for the element's definition.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 *
 * @see Prop
 */
public class PropFind extends ElementEditor {
	/**
	 * An ordered collection of the element names of the propfind
	 * element's children.
	 */
	protected static final String[] childNames = new String[] {"allprop", "propname", "prop"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	/**
	 * Creates a new editor on the given WebDAV propfind element. The
	 * element is assumed to be well formed.
	 *
	 * @param root a propfind element
	 * @throws        MalformedElementException if there is reason to
	 *                believe that the element is not well formed
	 */
	public PropFind(Element root) throws MalformedElementException {
		super(root, "propfind"); //$NON-NLS-1$
	}

	/**
	 * Creates a new WebDAV propfind element and sets it as the root of the
	 * given document. Returns an editor on the new propfind element. The
	 * document must not be <code>null</code>, and must not already have a
	 * root element.
	 *
	 * @param document the document that will become the root of a new
	 *                 propfind element
	 * @return         an element editor on a propfind element
	 */
	public static PropFind create(Document document) {
		Assert.isNotNull(document);
		Assert.isTrue(document.getDocumentElement() == null);
		Element element = create(document, "propfind"); //$NON-NLS-1$
		try {
			return new PropFind(element);
		} catch (MalformedElementException e) {
			Assert.isTrue(false, Policy.bind("assert.internalError")); //$NON-NLS-1$
			return null;
		}
	}

	/**
	 * Returns this propfind's prop.
	 *
	 * @return this propfind's prop
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed, or if
	 *         this propfind is not in the "prop" form
	 */
	public Prop getProp() throws MalformedElementException {
		Element prop = getFirstChild(root, "prop"); //$NON-NLS-1$
		ensureNotNull(Policy.bind("ensure.missingPropElmt"), prop); //$NON-NLS-1$
		return new Prop(prop);
	}

	/**
	 * Returns <code>true</code> iff this propfind is in the "all prop"
	 * form.
	 *
	 * @return a boolean indicating whether this propfind is in the "all
	 *         prop" form
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed
	 */
	public boolean isAllProp() throws MalformedElementException {
		Element child = getFirstChild(root, childNames);
		ensureNotNull(Policy.bind("ensure.missingAllpropOrPropnameOrPropElmt"), child); //$NON-NLS-1$
		boolean isAllProp = isDAVElement(child, "allprop"); //$NON-NLS-1$
		child = getNextSibling(child, childNames);
		ensureNull(Policy.bind("ensure.conflictingAllpropOrPropnameOrPropElmt"), child); //$NON-NLS-1$
		return isAllProp;
	}

	/**
	 * Returns <code>true</code> iff this propfind is in the "prop" form.
	 *
	 * @return a boolean indicating whether this propfind is in the "prop"
	 *         form
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed
	 */
	public boolean isProp() throws MalformedElementException {
		Element child = getFirstChild(root, childNames);
		ensureNotNull(Policy.bind("ensure.missingAllpropOrPropnameOrPropElmt"), child); //$NON-NLS-1$
		boolean isProp = isDAVElement(child, "prop"); //$NON-NLS-1$
		child = getNextSibling(child, childNames);
		ensureNull(Policy.bind("ensure.conflictingAllpropOrPropnameOrPropElmt"), child); //$NON-NLS-1$
		return isProp;
	}

	/**
	 * Returns <code>true</code> iff this propfind is in the "prop name"
	 * form.
	 *
	 * @return a boolean indicating whether this propfind is in the
	 *         "propname" form
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed
	 */
	public boolean isPropName() throws MalformedElementException {
		Element child = getFirstChild(root, childNames);
		ensureNotNull(Policy.bind("ensure.missingAllpropOrPropnameOrPropElmt"), child); //$NON-NLS-1$
		boolean isPropName = isDAVElement(child, "propname"); //$NON-NLS-1$
		child = getNextSibling(child, childNames);
		ensureNull(Policy.bind("ensure.conflictingAllpropOrPropnameOrPropElmt"), child); //$NON-NLS-1$
		return isPropName;
	}

	/**
	 * Sets whether this propfind is in the "all prop" form.
	 *
	 * @param isAllProp boolean indicating whether this propfind will be in the
	 *        "all prop" form
	 */
	public void setIsAllProp(boolean isAllProp) {
		Element child = getFirstChild(root, childNames);
		boolean isAlreadyAllProp = isDAVElement(child, "allprop"); //$NON-NLS-1$
		if (isAllProp) {
			if (!isAlreadyAllProp) {
				if (child != null)
					root.removeChild(child);
				appendChild(root, "allprop"); //$NON-NLS-1$
			}
		} else if (isAlreadyAllProp)
			root.removeChild(child);
	}

	/**
	 * Sets whether this propfind is in the "prop name" form.
	 *
	 * @param isPropName boolean indicating whether this propfind will be in the
	 *        "prop name" form
	 */
	public void setIsPropName(boolean isPropName) {
		Element child = getFirstChild(root, childNames);
		boolean isAlreadyPropName = isDAVElement(child, "propname"); //$NON-NLS-1$
		if (isPropName) {
			if (!isAlreadyPropName) {
				if (child != null)
					root.removeChild(child);
				appendChild(root, "propname"); //$NON-NLS-1$
			}
		} else if (isAlreadyPropName)
			root.removeChild(child);
	}

	/**
	 * Creates and sets a new prop on this propfind and returns an editor on
	 * it. This propfind must not already be in the "all prop" or "prop
	 * name" form.
	 *
	 * @return an editor on a new prop element
	 */
	public Prop setProp() {
		Assert.isTrue(getFirstChild(root, new String[] {"allprop", "propname"}) == null); //$NON-NLS-1$ //$NON-NLS-2$
		Element prop = setChild(root, "prop", new String[] {"prop"}, true); //$NON-NLS-1$ //$NON-NLS-2$
		try {
			return new Prop(prop);
		} catch (MalformedElementException e) {
			Assert.isTrue(false, Policy.bind("assert.internalError")); //$NON-NLS-1$
			return null; // Never reached.
		}
	}
}
