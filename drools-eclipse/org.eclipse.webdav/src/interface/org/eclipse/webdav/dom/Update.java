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
 * An element editor for the WebDAV update element.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public class Update extends ElementEditor {

	// An ordered collection of the element names of the update
	// element's children.
	public static String[] childNames = new String[] {"label-name", "version"}; //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Creates a new editor on the given WebDAV set target element. The
	 * element is assumed to be well formed.
	 *
	 * @param root an activelock element
	 * @throws        MalformedElementException if there is reason to
	 *                believe that the element is not well formed
	 */
	public Update(Element root) throws MalformedElementException {
		super(root, "set-target"); //$NON-NLS-1$
	}

	/**
	 * Creates a new WebDAV update element and sets it as the root of
	 * the given document.  Returns an editor on the new element.
	 * <p>
	 * The document must not be <code>null</code>, and must not already have
	 * a root element.</p>
	 *
	 * @param document the document that will become the root of a new
	 *                 update element.
	 * @return         an element editor on an update element.
	 */
	public static Update createLabel(Document document, String label) {
		Assert.isNotNull(document);
		Assert.isTrue(document.getOwnerDocument() == null);
		Assert.isNotNull(label);
		Element element = create(document, "update"); //$NON-NLS-1$
		try {
			Update editor = new Update(element);
			editor.setLabelName(label);
			return editor;
		} catch (MalformedElementException e) {
			Assert.isTrue(false, Policy.bind("assert.internalError")); //$NON-NLS-1$
			return null; // Never reached.
		}
	}

	/**
	 * Creates a new WebDAV update element and sets it as the root of
	 * the given document.  Returns an editor on the new element.
	 * <p>
	 * The document must not be <code>null</code>, and must not already have
	 * a root element.</p>
	 *
	 * @param document the document that will become the root of a new
	 *                 update element
	 * @return         an element editor on a set-target element
	 */
	public static Update createVersion(Document document, String href) {
		Assert.isNotNull(document);
		Assert.isTrue(document.getOwnerDocument() == null);
		Assert.isNotNull(href);
		Element element = create(document, "update"); //$NON-NLS-1$
		try {
			Update editor = new Update(element);
			editor.setVersion(href);
			return editor;
		} catch (MalformedElementException e) {
			Assert.isTrue(false, Policy.bind("assert.internalError")); //$NON-NLS-1$
			return null; //  Never reached.
		}
	}

	public String getLabel() throws MalformedElementException {
		String label = getChildText(root, "label", true); //$NON-NLS-1$
		ensureNotNull(Policy.bind("ensure.missingLabelElmt"), label); //$NON-NLS-1$
		return label;
	}

	/**
	 * Returns this response's first DAV:version child element.
	 *
	 * @return this response's first version href.
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed
	 */
	public String getVersion() throws MalformedElementException {
		Element version = getFirstChild(root, "version"); //$NON-NLS-1$
		ensureNotNull(Policy.bind("ensure.missingVersionElmt"), version); //$NON-NLS-1$
		String href = getChildText(version, "href", true); //$NON-NLS-1$
		ensureNotNull(Policy.bind("ensure.missingHrefElmt"), href); //$NON-NLS-1$
		return decodeHref(href);
	}

	public boolean isVersion() throws MalformedElementException {
		Element child = getFirstChild(root, childNames);
		ensureNotNull(Policy.bind("ensure.missingTargetDesc"), child); //$NON-NLS-1$
		return getNSLocalName(child).equals("version"); //$NON-NLS-1$
	}

	/**
	 * Sets the DAV:label child element.
	 *
	 * @param label the string label to the version.
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed
	 */
	public void setLabelName(String label) throws MalformedElementException {
		Element child = getFirstChild(root, childNames);
		// If there is a version child already there remove it.
		if (isDAVElement(child, "version")) //$NON-NLS-1$
			root.removeChild(child);
		// Add/update the label-name element.
		setChild(child, "label-name", label, new String[] {"label-name"}, true); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * Sets the DAV:version child element.
	 *
	 * @param href the string href to the version.
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed
	 */
	public void setVersion(String href) throws MalformedElementException {
		Element child = getFirstChild(root, childNames);
		// If there is a label-name child remove it.
		if (isDAVElement(child, "label-name")) //$NON-NLS-1$
			root.removeChild(child);
		// Add/update a version element with the href of the version target.
		Element newChild = setChild(root, "version", new String[] {"version"}, true); //$NON-NLS-1$ //$NON-NLS-2$
		setChild(newChild, "href", encodeHref(href), new String[] {"href"}, true); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
