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
import org.w3c.dom.*;

/**
 * An element editor for the WebDAV response element. See RFC2518
 * section 12.9.1 for the element's definition.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 *
 * @see MultiStatus
 * @see PropStat
 */
public class ResponseBody extends ElementEditor {
	/**
	 * An ordered collection of the element names of the responsebody
	 * element's children.
	 */
	protected static final String[] childNames = new String[] {"href", "href", "status", "propstat", "responsedescription"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$

	/**
	 * An ordered collection of the element names of the responsebody
	 * element's children in the "status" form.
	 */
	public static String[] fgNamesStatus = new String[] {"href", "href", "status", "responsedescription"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

	/**
	 * An ordered collection of the element names of the responsebody
	 * element's children in the "propstat" form.
	 */
	public static String[] fgNamesPropStat = new String[] {"href", "propstat", "responsedescription"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	/**
	 * Creates a new editor on the given WebDAV response element. The
	 * element is assumed to be well formed.
	 *
	 * @param root a response element
	 * @throws        MalformedElementException if there is reason to
	 *                believe that the element is not well formed
	 */
	public ResponseBody(Element root) throws MalformedElementException {
		super(root, "response"); //$NON-NLS-1$
	}

	/**
	 * Gets this response's first propstat with the given status and adds an
	 * element created from the given property name as a property of the
	 * propstat's prop. If such a propstat does not exists, it is created.
	 * The propstat's response description is set to the given response
	 * description, or removed if the response description is
	 * <code>null</code>. Returns the propstat. The property name must not
	 * be <code>null</code> and its qualifier and local part must not be
	 * <code>null</code> and must not be the empty string. The status must
	 * not be <code>null</code>. This response must not already be in the
	 * "status" form.
	 *
	 * @param propertyName        the name of the property to create and add
	 * @param status              the status of the propstat to add the
	 *                            property element to
	 * @param responseDescription the new response description of the
	 *                            propstat, or <code>null</code> to remove
	 *                            the old one
	 * @return                    a propstat with the specified property
	 *                            element, status, and response description
	 */
	public PropStat accumulatePropStat(QualifiedName propertyName, String status, String responseDescription) {

		Assert.isNotNull(propertyName);
		Assert.isNotNull(status);

		Element child = getFirstChild(root, new String[] {"href", "status"}); //$NON-NLS-1$ //$NON-NLS-2$
		Assert.isTrue(child == null || isDAVElement(child, "href") //$NON-NLS-1$
				&& getNextSibling(child, new String[] {"href", "status"}) == null); //$NON-NLS-1$ //$NON-NLS-2$

		String nsName = propertyName.getQualifier();
		Assert.isTrue(!"".equals(nsName)); //$NON-NLS-1$

		String localName = propertyName.getLocalName();
		Assert.isNotNull(localName);
		Assert.isTrue(!localName.equals("")); //$NON-NLS-1$

		Document document = root.getOwnerDocument();
		Element element = document.createElement(localName);
		declareNS(element, null, nsName);

		try {
			return accumulatePropStat(element, status, responseDescription);
		} catch (MalformedElementException e) {
			Assert.isTrue(false, Policy.bind("assert.internalError")); //$NON-NLS-1$
			return null; // Never reached.
		}
	}

	/**
	 * Gets this response's first propstat with the given status and adds a
	 * clone of the given element as a property of its prop. If such a
	 * propstat does not exists, it is created. The propstat's response
	 * description is set to the given response description, or removed if
	 * the response description is <code>null</code>. Returns the propstat.
	 * The element and status must not be <code>null</code>. This response
	 * must not already be in the "status" form.
	 *
	 * @param element             the property element being added
	 * @param status              the status of the propstat to add the
	 *                            property element to
	 * @param responseDescription the new response description of the
	 *                            propstat, or <code>null</code> to remove
	 *                            the old one
	 * @return                    a propstat with the given property
	 *                            element, status, and response description
	 * @throws                    MalformedElementException if there is
	 *                            reason to believe that the given element
	 *                            is not well formed
	 */
	public PropStat accumulatePropStat(Element element, String status, String responseDescription) throws MalformedElementException {

		Assert.isNotNull(element);
		Assert.isNotNull(status);

		Element child = getFirstChild(root, new String[] {"href", "status"}); //$NON-NLS-1$ //$NON-NLS-2$
		Assert.isTrue(child == null || isDAVElement(child, "href") //$NON-NLS-1$
				&& getNextSibling(child, new String[] {"href", "status"}) == null); //$NON-NLS-1$ //$NON-NLS-2$

		boolean found = false;
		Element propstat = getFirstChild(root, "propstat"); //$NON-NLS-1$

		while (!found && propstat != null) {
			String text = getChildText(propstat, "status", false); //$NON-NLS-1$
			if (text != null && text.equals(status))
				found = true;
			else
				propstat = getTwin(propstat, true);

		}

		Element prop = null;

		if (propstat == null) {
			propstat = addChild(root, "propstat", fgNamesPropStat, false); //$NON-NLS-1$
			prop = setChild(propstat, "prop", PropStat.childNames, true); //$NON-NLS-1$
			setChild(propstat, "status", status, PropStat.childNames, false); //$NON-NLS-1$
		} else {
			prop = getFirstChild(propstat, "prop"); //$NON-NLS-1$
			if (prop == null)
				prop = setChild(propstat, "prop", PropStat.childNames, true); //$NON-NLS-1$
		}

		if (responseDescription == null) {
			Element responsedescription = getLastChild(propstat, "responsedescription"); //$NON-NLS-1$
			if (responsedescription != null)
				propstat.removeChild(responsedescription);
		} else
			setChild(propstat, "responsedescription", //$NON-NLS-1$
					responseDescription, PropStat.childNames, false);

		extractNode(prop, element);

		try {
			return new PropStat(propstat);
		} catch (MalformedElementException e) {
			Assert.isTrue(false, Policy.bind("assert.internalError")); //$NON-NLS-1$
			return null; // Never reached.
		}
	}

	/**
	 * Adds the given href to this response. If <code>setHref(String)</code>
	 * hasn't been called and no hrefs have been added, this method sets
	 * the first href and is thus equivalent to
	 * <code>setHref(String)</code>. The href must not be <code>null</code>.
	 * This response must not already be in "propstat" form.
	 *
	 * @param href the href to add
	 */
	public void addHref(String href) {
		Assert.isNotNull(href);
		Assert.isTrue(getLastChild(root, "propstat") == null); //$NON-NLS-1$
		addChild(root, "href", encodeHref(href), fgNamesStatus, false); //$NON-NLS-1$
	}

	/**
	 * Creates and adds a propstat element on this response and returns an
	 * editor on it.
	 *
	 * @return an editor on a propstat element
	 */
	public PropStat addPropStat() {
		Element firstHref = getFirstChild(root, "href"); //$NON-NLS-1$
		Assert.isTrue(firstHref == null || getNextSibling(firstHref, new String[] {"href", "status"}) == null); //$NON-NLS-1$ //$NON-NLS-2$
		Element element = addChild(root, "propstat", fgNamesPropStat, false); //$NON-NLS-1$
		try {
			return new PropStat(element);
		} catch (MalformedElementException e) {
			Assert.isTrue(false, Policy.bind("assert.internalError")); //$NON-NLS-1$
			return null; // Never reached.
		}
	}

	/**
	 * Changes all of this response's propstats with the given old status
	 * to have the given new status. In addition, their response
	 * descriptions are changed to be the given response description or
	 * removed if the given response description is <code>null</code>. The
	 * old status and new status must not be <code>null</null>. This
	 * response must not be in the "status" form.
	 *
	 * @param oldStatus           the old status of the propstat's that are
	 *                            changed
	 * @param newStatus           the new status of the propstat's that are
	 *                            changed
	 * @param responseDescription the new response description of the
	 *                            propstat's that are changed, or
	 *                            <code>null</null>
	 */
	public void changePropStatStatus(String oldStatus, String newStatus, String responseDescription) {
		Assert.isNotNull(oldStatus);
		Assert.isNotNull(newStatus);
		Element firstHref = getFirstChild(root, "href"); //$NON-NLS-1$
		Assert.isTrue(firstHref == null || getNextSibling(firstHref, new String[] {"href", "status"}) == null); //$NON-NLS-1$ //$NON-NLS-2$
		Element propstat = getFirstChild(root, "propstat"); //$NON-NLS-1$
		while (propstat != null) {
			String status = getChildText(propstat, "status", true); //$NON-NLS-1$
			if (oldStatus.equals(status)) {
				setChild(propstat, "status", newStatus, PropStat.childNames, true); //$NON-NLS-1$
				if (responseDescription == null) {
					Element responsedescription = getLastChild(propstat, "responsedescription"); //$NON-NLS-1$
					if (responsedescription != null)
						propstat.removeChild(responsedescription);
				} else
					setChild(propstat, "responsedescription", //$NON-NLS-1$
							responseDescription, PropStat.childNames, false);
			}
			propstat = getTwin(propstat, true);
		}
	}

	/**
	 * Creates a new WebDAV response element and sets it as the root of the
	 * given document. Returns an editor on the new response element. The
	 * document must not be <code>null</code>, and must not already have a
	 * root element.
	 *
	 * @param document the document that will become the root of a new
	 *                 response element
	 * @return         an element editor on a response element
	 */
	public static ResponseBody create(Document document) {
		Assert.isNotNull(document);
		Assert.isTrue(document.getDocumentElement() == null);
		Element element = create(document, "response"); //$NON-NLS-1$
		ResponseBody result = null;
		try {
			result = new ResponseBody(element);
		} catch (MalformedElementException e) {
			Assert.isTrue(false, Policy.bind("assert.internalError")); //$NON-NLS-1$
		}
		return result;
	}

	/**
	 * Returns this response's first href.
	 *
	 * @return this response's first href.
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed
	 */
	public String getHref() throws MalformedElementException {
		String href = getChildText(root, "href", true); //$NON-NLS-1$
		ensureNotNull(Policy.bind("ensure.missingHrefElmt"), href); //$NON-NLS-1$
		return decodeHref(href);
	}

	/**
	 * Returns an <code>Enumeration</code> of this response's hrefs (not
	 * including the first href).
	 *
	 * @return an <code>Enumeration</code> of this response's href
	 *         <code>String<code>s
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed, or if
	 *         this response is not in "status" form
	 */
	public Enumeration getHrefs() throws MalformedElementException {
		final Node firstHref = getFirstChild(root, "href"); //$NON-NLS-1$
		ensureNotNull(Policy.bind("ensure.missingHrefElmt"), firstHref); //$NON-NLS-1$
		ensureNotNull(Policy.bind("ensure.missingStatusElmt"), //$NON-NLS-1$
				getNextSibling((Element) firstHref, "status")); //$NON-NLS-1$
		Enumeration e = new Enumeration() {
			Node currentHref = getTwin((Element) firstHref, true);

			public boolean hasMoreElements() {
				return currentHref != null;
			}

			public Object nextElement() {
				if (!hasMoreElements())
					throw new NoSuchElementException();
				String href = getFirstText((Element) currentHref);
				currentHref = getTwin((Element) currentHref, true);
				return decodeHref(href);
			}
		};
		return e;
	}

	/**
	 * Returns an <code>Enumeration</code> of this response's
	 * <code>Propstat</code>s.
	 *
	 * @return an <code>Enumeration</code> of this response's
	 *            <code>PropStat</code>s
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed, or if
	 *         this response is not in "propstat" form
	 */
	public Enumeration getPropStats() throws MalformedElementException {
		final Element firstPropStat = getFirstChild(root, "propstat"); //$NON-NLS-1$
		ensureNotNull("ensure.missingPropstatElmt", firstPropStat); //$NON-NLS-1$
		Enumeration e = new Enumeration() {
			Element currentPropStat = firstPropStat;

			public boolean hasMoreElements() {
				return currentPropStat != null;
			}

			public Object nextElement() {
				if (!hasMoreElements())
					throw new NoSuchElementException();
				PropStat result = null;
				try {
					result = new PropStat(currentPropStat);
				} catch (MalformedElementException ex) {
					Assert.isTrue(false, Policy.bind("assert.internalError")); //$NON-NLS-1$
				}
				currentPropStat = getTwin(currentPropStat, true);
				return result;
			}
		};
		return e;
	}

	/**
	 * Returns this response's response description, or <code>null</code> if
	 * it has none.
	 *
	 * @return this response's response description, or <code>null</code>
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed
	 */
	public String getResponseDescription() throws MalformedElementException {
		return getChildText(root, "responsedescription", false); //$NON-NLS-1$
	}

	/**
	 * Returns this response's status.
	 *
	 * @return this response's status
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed, or if
	 *         this response is not in "status" form
	 */
	public String getStatus() throws MalformedElementException {
		Element status = getFirstChild(root, "status"); //$NON-NLS-1$
		ensureNotNull(Policy.bind("ensure.missingStatusElmt"), status); //$NON-NLS-1$
		return getFirstText(status);
	}

	/**
	 * Returns this response's status code.
	 *
	 * @return this response's status code
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed, or if
	 *         this response is not in "status" form
	 */
	public int getStatusCode() throws MalformedElementException {
		return new Status(getStatus()).getStatusCode();
	}

	/**
	 * Returns <code>true</code> if this response is in "propstat" form and
	 * <code>false</code> if it is in "status" form.
	 *
	 * @return a boolean indicating whether this response is in "propstat"
	 *         or "status" form
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed
	 */
	public boolean isPropStat() throws MalformedElementException {
		Element child = getFirstChild(root, new String[] {"status", "propstat"}); //$NON-NLS-1$ //$NON-NLS-2$
		ensureNotNull(Policy.bind("ensure.missingStatusOrPropstatElmt"), child); //$NON-NLS-1$
		boolean isPropStat = isDAVElement(child, "propstat"); //$NON-NLS-1$
		if (isPropStat)
			child = getNextSibling(child, "status"); //$NON-NLS-1$
		else
			child = getNextSibling(child, "propstat"); //$NON-NLS-1$
		ensureNull(Policy.bind("ensure.conflictingStatusOrPropstatElmt"), child); //$NON-NLS-1$
		return isPropStat;
	}

	/**
	 * Returns <code>true</code> if this response is in "status" form and
	 * <code>false</code> if it is in "propstat" form.
	 *
	 * @return a boolean indicating whether this response is in "status"
	 *         or "propstat" form
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed
	 */
	public boolean isStatus() throws MalformedElementException {
		return !isPropStat();
	}

	/**
	 * Sets this response's first href to the given href. The href must not
	 * be <code>null</code>.
	 *
	 * @param href the href to set this response's first href to
	 */
	public void setHref(String href) {
		Assert.isNotNull(href);
		setChild(root, "href", href, childNames, true); //$NON-NLS-1$
	}

	/**
	 * Sets this response's response description to the given value. If the
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
	 * Sets this response's response description to the given value. If the
	 * value is <code>null</code> and a response description has already
	 * been set, it is removed.
	 *
	 * @param value a response description, or <code>null</code>
	 */
	public void setResponseDescription(Element value) {
		Element child = getLastChild(root, "responsedescription"); //$NON-NLS-1$
		if (child != null)
			root.removeChild(child);
		if (value == null) {
			child = setChild(root, "responsedescription", childNames, false); //$NON-NLS-1$
			child.appendChild(value);
		}
	}

	/**
	 * Sets the status on this response to the given status. The status
	 * must not be <code>null</code>. This response must not already be in
	 * the "propstat" form.
	 *
	 * @param status the status for this response
	 */
	public void setStatus(String status) {
		Assert.isNotNull(status);
		Assert.isTrue(getLastChild(root, "propstat") == null); //$NON-NLS-1$
		setChild(root, "status", status, fgNamesStatus, true); //$NON-NLS-1$
	}
}
