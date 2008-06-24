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

import java.util.Enumeration;
import java.util.NoSuchElementException;
import org.eclipse.webdav.Policy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An element editor for the WebDAV propertybehavior element. See
 * RFC2518 section 12.12 for the element's definition.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public class PropertyBehavior extends ElementEditor {
	/**
	 * An ordered collection of the element names of the
	 * propertybehavior element's children.
	 */
	protected static final String[] childNames = new String[] {"omit", "keepalive"}; //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * An ordered collection of the element names of the
	 * propertybehavior element's children in the "omit" form
	 */
	public static String[] fgNamesOmit = new String[] {"omit"}; //$NON-NLS-1$

	/**
	 * An ordered collection of the element names of the
	 * propertybehavior element's children in the "keep alive" form
	 */
	public static String[] fgNamesKeepAlive = new String[] {"keepalive"}; //$NON-NLS-1$

	/**
	 * Creates a new editor on the given WebDAV propertybehavior element.
	 * The element is assumed to be well formed.
	 *
	 * @param root a propertybehavior element
	 * @throws        MalformedElementException if there is reason to
	 *                believe that the element is not well formed
	 */
	public PropertyBehavior(Element root) throws MalformedElementException {
		super(root, "propertybehavior"); //$NON-NLS-1$
	}

	/**
	 * Adds the given property href to this propertybehavior's list of live
	 * properties. The property href must not be <code>null</code> and the
	 * form of this property behavior must not already be omit or
	 * keepAllAlive.
	 *
	 * @param propertyHref the property href to add
	 */
	public void addProperty(String propertyHref) {
		Assert.isNotNull(propertyHref);
		Assert.isTrue(getFirstChild(root, "omit") == null); //$NON-NLS-1$
		Element keepalive = getFirstChild(root, "keepalive"); //$NON-NLS-1$
		if (keepalive == null)
			keepalive = addChild(root, "keepalive", fgNamesKeepAlive, true); //$NON-NLS-1$
		else
			Assert.isTrue(!"*".equals(getFirstText(keepalive))); //$NON-NLS-1$
		addChild(keepalive, "href", //$NON-NLS-1$
				encodeHref(propertyHref), new String[] {"href"}, //$NON-NLS-1$
				false);
	}

	/**
	 * Creates a new WebDAV propertybehavior element and sets it as the root
	 * of the given document. Returns an editor on the new propertybehavior
	 * element. The document must not be <code>null</code>, and must not
	 * already have a root element.
	 *
	 * @param document the document that will become the root of a new
	 *                 propertybehavior element
	 * @return         an element editor on a propertybehavior element
	 */
	public static PropertyBehavior create(Document document) {
		Assert.isNotNull(document);
		Assert.isTrue(document.getOwnerDocument() == null);
		Element element = create(document, "propertybehavior"); //$NON-NLS-1$
		PropertyBehavior result = null;
		try {
			result = new PropertyBehavior(element);
		} catch (MalformedElementException e) {
			Assert.isTrue(false, Policy.bind("assert.internalError")); //$NON-NLS-1$
		}
		return result;
	}

	/**
	 * Returns an <code>Enumeration</code> over this propertybehavior's
	 * property hrefs. The methods <code>isMerge()</code> and
	 * <code>isKeepAllAlive</code> return false if this propertybehavior is
	 * in the "keep some alive" form.
	 *
	 * @return an <code>Enumeration</code> of <code>String</code>s
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed, or this
	 *         propertybehavior is not in the "keep some alive" form
	 * @see    #isKeepAllAlive()
	 * @see    #isOmit()
	 */
	public Enumeration getProperties() throws MalformedElementException {

		Element keepalive = getFirstChild(root, "keepalive"); //$NON-NLS-1$
		ensureNotNull(Policy.bind("ensure.missingKeealiveElmt"), keepalive); //$NON-NLS-1$
		ensure(!"*".equals(getFirstText(keepalive)), //$NON-NLS-1$
				Policy.bind("ensure.wrongForm")); //$NON-NLS-1$

		final Element firstHref = getFirstChild(keepalive, "href"); //$NON-NLS-1$
		ensureNotNull(Policy.bind("ensure.missingHrefElmt"), firstHref); //$NON-NLS-1$

		Enumeration e = new Enumeration() {
			Element currentHref = firstHref;

			public boolean hasMoreElements() {
				return currentHref != null;
			}

			public Object nextElement() {
				if (!hasMoreElements())
					throw new NoSuchElementException();
				String href = getFirstText(currentHref);
				currentHref = getTwin(currentHref, true);
				return decodeHref(href);
			}
		};

		return e;
	}

	/**
	 * Returns <code>true</code> if this propertybehavior is in the
	 * "keep all alive" form, otherwise, returns <code>false</code>.
	 *
	 * @return a boolean indicating whether this propertybehavior is in the
	 *         "keep all alive" form or not
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed
	 */
	public boolean isKeepAllAlive() throws MalformedElementException {
		Element child = getFirstChild(root, childNames);
		ensureNotNull(Policy.bind("ensure.expectingOmitOrKeepaliveElmt"), child); //$NON-NLS-1$
		boolean isKeepAllAlive = false;
		if (isDAVElement(child, "keepalive")) { //$NON-NLS-1$
			isKeepAllAlive = "*".equals(getFirstText(child)); //$NON-NLS-1$
			ensureNull(Policy.bind("ensure.conflictingHrefElmt"), getFirstChild(child, "href")); //$NON-NLS-1$ //$NON-NLS-2$
		}
		child = getNextSibling(child, childNames);
		ensureNull(Policy.bind("ensure.conflictingOmitOrKeepaliveElmt"), child); //$NON-NLS-1$
		return isKeepAllAlive;
	}

	/**
	 * Returns <code>true</code> if this propertybehavior is in the
	 * "omit" form, otherwise, returns <code>false</code>.
	 *
	 * @return a boolean indicating whether this propertybehavior is in the
	 *         "omit" form or not
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed
	 */
	public boolean isOmit() throws MalformedElementException {
		Element child = getFirstChild(root, childNames);
		ensureNotNull(Policy.bind("ensure.expectingOmitOrKeepaliveElmt"), child); //$NON-NLS-1$
		boolean isOmit = isDAVElement(child, "omit"); //$NON-NLS-1$
		child = getNextSibling(child, childNames);
		ensureNull(Policy.bind("ensure.conflictingOmitOrKeepaliveElmt"), child); //$NON-NLS-1$
		return isOmit;
	}

	/**
	 * Sets whether this propertybehavior is in the "keep all alive" form or
	 * not.
	 *
	 * @param isKeepAllAlive a boolean indicating whether this
	 *                       propertybehavior will be in the "keep all
	 *                       alive" form
	 */
	public void setIsKeepAllAlive(boolean isKeepAllAlive) {
		Element child = getFirstChild(root, childNames);
		boolean isAlreadyKeepAllAlive = false;
		if (isDAVElement(child, "keepalive")) //$NON-NLS-1$
			isAlreadyKeepAllAlive = "*".equals(getFirstText(child)); //$NON-NLS-1$
		if (isKeepAllAlive) {
			if (!isAlreadyKeepAllAlive) {
				if (child != null)
					root.removeChild(child);
				appendChild(root, "keepalive", "*"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		} else if (isAlreadyKeepAllAlive)
			root.removeChild(child);
	}

	/**
	 * Sets whether this propertybehavior is in the "omit" form or not.
	 *
	 * @param isOmit a boolean indicating whether this propertybehavior will
	 *               be in the "omit" form
	 */
	public void setIsOmit(boolean isOmit) {
		Element child = getFirstChild(root, childNames);
		boolean isAlreadyOmit = isDAVElement(child, "omit"); //$NON-NLS-1$
		if (isOmit) {
			if (!isAlreadyOmit) {
				if (child != null)
					root.removeChild(child);
				appendChild(root, "omit"); //$NON-NLS-1$
			}
		} else if (isAlreadyOmit)
			root.removeChild(child);
	}
}
