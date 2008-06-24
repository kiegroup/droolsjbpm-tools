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
 * An element editor for the WebDAV multistatus element. See RFC2518
 * section 12.9 for the element's definition.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 *
 * @see ResponseBody
 */
public class MultiStatus extends ElementEditor {
	/**
	 * An ordered collection of the element names of the multistatus
	 * element's children.
	 */
	protected static final String[] childNames = new String[] {"response", "responsedescription"}; //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Creates a new editor on the given WebDAV multistatus element. The
	 * element is assumed to be well formed.
	 *
	 * @param root a multistatus element
	 * @throws        MalformedElementException if there is reason to
	 *                believe that the element is not well formed
	 */
	public MultiStatus(Element root) throws MalformedElementException {
		super(root, "multistatus"); //$NON-NLS-1$
	}

	/**
	 * Creates and adds a response element to this multistatus and returns
	 * an editor on it.
	 *
	 * @return an editor on a response element
	 */
	public ResponseBody addResponse() {
		Element response = addChild(root, "response", childNames, true); //$NON-NLS-1$
		try {
			return new ResponseBody(response);
		} catch (MalformedElementException e) {
			Assert.isTrue(false, Policy.bind("assert.internalError")); //$NON-NLS-1$
			return null; // Never reached.
		}
	}

	/**
	 * Creates a new WebDAV multistatus element and sets it as the root of
	 * the given document.  Returns an editor on the new multistatus element.
	 * <p>
	 * The document must not be <code>null</code>, and must not already have
	 * a root element.</p>
	 *
	 * @param document the document that will become the root of a new
	 *                 multistatus element
	 * @return         an element editor on a multistatus element
	 */
	public static MultiStatus create(Document document) {
		Assert.isNotNull(document);
		Assert.isTrue(document.getOwnerDocument() == null);
		Element element = create(document, "multistatus"); //$NON-NLS-1$
		try {
			return new MultiStatus(element);
		} catch (MalformedElementException e) {
			Assert.isTrue(false, Policy.bind("assert.internalError")); //$NON-NLS-1$
			return null; // Never reached.
		}
	}

	/**
	 * Returns this multistatus' response description, or <code>null</code>
	 * if it has none.
	 *
	 * @return this multistatus' response description, or <code>null</code>
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed
	 */
	public String getResponseDescription() throws MalformedElementException {
		return getChildText(root, "responsedescription", false); //$NON-NLS-1$
	}

	/**
	 * Returns an <code>Enumeration</code> over this multistatus' responses.
	 *
	 * @return an <code>Enumeration</code> of <code>ResponseBody</code>s
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed
	 */
	public Enumeration getResponses() throws MalformedElementException {

		final Element firstResponse = getFirstChild(root, "response"); //$NON-NLS-1$
		ensureNotNull(Policy.bind("ensure.missingResponseElmt"), firstResponse); //$NON-NLS-1$

		Enumeration e = new Enumeration() {
			Element currentResponse = firstResponse;

			public boolean hasMoreElements() {

				return currentResponse != null;
			}

			public Object nextElement() {

				if (!hasMoreElements())
					throw new NoSuchElementException();

				ResponseBody responseBody = null;
				try {
					responseBody = new ResponseBody(currentResponse);
				} catch (MalformedElementException ex) {
					Assert.isTrue(false, Policy.bind("assert.internalError")); //$NON-NLS-1$
				}

				currentResponse = getTwin(currentResponse, true);

				return responseBody;
			}
		};

		return e;
	}

	/**
	 * Sets this multistatus' response description to the given value. If
	 * the value is <code>null</code> and a response description has already
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
}
