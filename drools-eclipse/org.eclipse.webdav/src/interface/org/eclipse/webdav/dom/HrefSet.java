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
 * An element editor for sets of hrefs, for example, the WebDAV
 * predecessor-set element.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public class HrefSet extends ElementEditor {
	/**
	 * Creates a new editor on the given href set element with the given
	 * name. The element is assumed to be well formed.
	 *
	 * @param root    an href element
	 * @param name    the name of the element that this editor is expected
	 *                to manipulate.
	 * @throws        MalformedElementException if there is reason to
	 *                believe that the element is not well formed
	 */
	public HrefSet(Element root, QualifiedName name) throws MalformedElementException {
		super(root, name.getLocalName());
		ensure(DAV_NS.equals(name.getQualifier()), Policy.bind("ensure.mustHaveDAVQualifier")); //$NON-NLS-1$
	}

	/**
	 * Adds the given href to the end of the set of hrefs. If the href
	 * already exists it is not added.
	 *
	 * @param href    the href to add to the end of the set of hrefs
	 */
	public void addHref(String href) {
		String encodedHref = encodeHref(href);
		if (isDuplicate(encodedHref))
			return;
		appendChild(root, "href", encodedHref); //$NON-NLS-1$
	}

	/**
	 * Creates a new href set element with the given name and sets it as
	 * the root of the given document. Returns an editor on the new href set
	 * element. The document must not be <code>null</code>, and must not
	 * already have a root element.
	 *
	 * @param document the document that will become the root of a new
	 *                 href set element
	 * @return         an element editor on a href set element
	 */
	public static HrefSet create(Document document, QualifiedName name) {
		Assert.isNotNull(document);
		Assert.isTrue(document.getOwnerDocument() == null);
		Assert.isNotNull(name);
		Assert.isTrue(DAV_NS.equals(name.getQualifier()));
		Element element = create(document, name.getLocalName());
		try {
			return new HrefSet(element, name);
		} catch (MalformedElementException e) {
			Assert.isTrue(false, Policy.bind("assert.internalError")); //$NON-NLS-1$
			return null; // Never reached.
		}
	}

	/**
	 * Returns an <code>Enumeration</code> over the set of hrefs.
	 *
	 * @return an <code>Enumeration</code> of href <code>String</code>s
	 */
	public Enumeration getHrefs() {
		final Element firstHref = getFirstChild(root, "href"); //$NON-NLS-1$
		Enumeration e = new Enumeration() {
			Element currentHref = firstHref;

			public boolean hasMoreElements() {
				return currentHref != null;
			}

			public Object nextElement() {
				if (!hasMoreElements())
					throw new NoSuchElementException();
				String href = getFirstText(currentHref);
				currentHref = getNextSibling(currentHref, "href"); //$NON-NLS-1$
				return decodeHref(href);
			}
		};
		return e;
	}

	/**
	 * Inserts the given newHref after the given refHref in the set of
	 * hrefs. If newHref already exists it is not inserted.
	 *
	 * @param refHref the existing href
	 * @param newHref the new href to be inserted after the existing href
	 */
	public void insertHrefAfter(String refHref, String newHref) {
		String refHrefEncoded = encodeHref(refHref);
		String newHrefEncoded = encodeHref(newHref);
		if (isDuplicate(newHrefEncoded))
			return;
		Element child = getFirstChild(root, "href"); //$NON-NLS-1$
		while (child != null) {
			if (refHrefEncoded.equals(getFirstText(child))) {
				Element nextSibling = getNextSibling(child, "href"); //$NON-NLS-1$
				if (nextSibling == null)
					appendChild(root, "href", newHrefEncoded); //$NON-NLS-1$
				else
					insertBefore(nextSibling, "href", newHrefEncoded); //$NON-NLS-1$
				return;
			}
			child = getNextSibling(child, "href"); //$NON-NLS-1$
		}
		Assert.isTrue(false, Policy.bind("assert.noHrefRef")); //$NON-NLS-1$
	}

	/**
	 * Inserts the given newHref before the given refHref in the set of
	 * hrefs. If newHref already exists it is not inserted.
	 *
	 * @param refHref the existing href
	 * @param newHref the new href to be inserted before the existing href
	 */
	public void insertHrefBefore(String newHref, String refHref) {
		String refHrefEncoded = encodeHref(refHref);
		String newHrefEncoded = encodeHref(newHref);
		if (isDuplicate(newHrefEncoded))
			return;
		Element child = getFirstChild(root, "href"); //$NON-NLS-1$
		while (child != null) {
			if (refHrefEncoded.equals(getFirstText(child))) {
				insertBefore(child, "href", newHrefEncoded); //$NON-NLS-1$
				return;
			}
			child = getNextSibling(child, "href"); //$NON-NLS-1$
		}
		Assert.isTrue(false, Policy.bind("assert.noHrefRef")); //$NON-NLS-1$
	}

	protected boolean isDuplicate(String encodedHref) {
		Element child = getFirstChild(root, "href"); //$NON-NLS-1$
		while (child != null) {
			if (encodedHref.equals(getFirstText(child)))
				return true;
			child = getNextSibling(child, "href"); //$NON-NLS-1$
		}
		return false;
	}

	/**
	 * Remove the given href from the set of hrefs.
	 *
	 * @param href    the href to remove from the set of hrefs
	 */
	public void removeHref(String href) {
		String encodedHref = encodeHref(href);
		Element child = getFirstChild(root, "href"); //$NON-NLS-1$
		while (child != null) {
			if (encodedHref.equals(getFirstText(child))) {
				root.removeChild(child);
				return;
			}
			child = getNextSibling(child, "href"); //$NON-NLS-1$
		}
	}
}
