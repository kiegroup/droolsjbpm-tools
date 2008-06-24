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
 * An element editor for the WebDAV propertyupdate element. See RFC2518
 * section 12.13 for the element's definition.
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
public class PropertyUpdate extends ElementEditor {
	/**
	 * An editor for a generic element that is a child of a WebDAV prop
	 * element, which, in turn, is a child of a WebDAV set or remove
	 * element.
	 */
	public class Directive extends ElementEditor {

		/**
		 * Creates a new editor on the given element.
		 *
		 * @param element a generic element
		 * @throws        MalformedElementException if there is reason
		 *                to believe that this editor's underlying
		 *                element is not well formed
		 */
		public Directive(Element element) throws MalformedElementException {
			super(element);
			Node parent = element.getParentNode();
			ensureDAVElement(parent, "prop", Policy.bind("ensure.expectingPropElmt")); //$NON-NLS-1$ //$NON-NLS-2$
			Node grandparent = parent.getParentNode();
			ensure(isDAVElement(grandparent, "remove") || isDAVElement(grandparent, "set"), //$NON-NLS-1$ //$NON-NLS-2$
					Policy.bind("ensure.expectingRemoveOrSetElmt")); //$NON-NLS-1$
		}

		/**
		 * Returns this editor's underlying element, which may be any
		 * generic element.
		 *
		 * @return this editor's underlying element
		 */
		public Element getProperty() throws MalformedElementException {
			return root;
		}

		/**
		 * Returns <code>true</code> if this directive's property
		 * decends from a WebDAV remove element and <code>false</code>
		 * if it decends from a WebDAV set element.
		 *
		 * @return a boolean indicating whether this directive is a
		 *           remove or not
		 * @throws MalformedElementException if there is reason to
		 *         believe that this editor's underlying element is not
		 *         well formed
		 */
		public boolean isRemove() throws MalformedElementException {
			Node parent = root.getParentNode();
			ensureDAVElement(parent, "prop", Policy.bind("ensure.expectingPropElmt")); //$NON-NLS-1$ //$NON-NLS-2$
			Node grandparent = parent.getParentNode();
			return isDAVElement(grandparent, "remove"); //$NON-NLS-1$
		}

		/**
		 * Returns <code>true</code> if this directive's property
		 * decends from a WebDAV set element and <code>false</code>
		 * if it decends from a WebDAV remove element.
		 *
		 * @return a boolean indicating whether this directive is a
		 *           set or not
		 * @throws MalformedElementException if there is reason to
		 *         believe that this editor's underlying element is not
		 *         well formed
		 */
		public boolean isSet() throws MalformedElementException {
			Node parent = root.getParentNode();
			ensureDAVElement(parent, "prop", Policy.bind("ensure.expectingPropElmt")); //$NON-NLS-1$ //$NON-NLS-2$
			Node grandparent = parent.getParentNode();
			return isDAVElement(grandparent, "set"); //$NON-NLS-1$
		}
	}

	/**
	 * Creates a new editor on the given WebDAV propertyupdate element. The
	 * element is assumed to be well formed.
	 *
	 * @param root a propertyupdate element
	 * @throws        MalformedElementException if there is reason to
	 *                believe that the element is not well formed
	 */
	public PropertyUpdate(Element root) throws MalformedElementException {
		super(root, "propertyupdate"); //$NON-NLS-1$
	}

	/**
	 * Adds a remove to the given propertyupdate and returns an editor on
	 * its prop.
	 *
	 * @return an editor on a new prop
	 */
	public Prop addRemove() {
		Element remove = appendChild(root, "remove"); //$NON-NLS-1$
		Element prop = appendChild(remove, "prop"); //$NON-NLS-1$
		Prop result = null;
		try {
			result = new Prop(prop);
		} catch (MalformedElementException e) {
			Assert.isTrue(false, Policy.bind("assert.internalError")); //$NON-NLS-1$
		}
		return result;
	}

	/**
	 * Adds a set to the given propertyupdate and returns an editor on its
	 * prop.
	 *
	 * @return an editor on a new prop
	 */
	public Prop addSet() {
		Element set = appendChild(root, "set"); //$NON-NLS-1$
		Element prop = appendChild(set, "prop"); //$NON-NLS-1$
		try {
			return new Prop(prop);
		} catch (MalformedElementException e) {
			Assert.isTrue(false, Policy.bind("assert.internalError")); //$NON-NLS-1$
			return null; // Never reached.
		}
	}

	/**
	 * Creates a new WebDAV propertyupdate element and sets it as the root
	 * of the given document. Returns an editor on the new propertyupdate
	 * element. The document must not be <code>null</code>, and must not
	 * already have a root element.
	 *
	 * @param document the document that will become the root of a new
	 *                 propertyupdate element
	 * @return         an element editor on a propertyupdate element
	 */
	public static PropertyUpdate create(Document document) {
		Assert.isNotNull(document);
		Assert.isTrue(document.getDocumentElement() == null);
		Element element = create(document, "propertyupdate"); //$NON-NLS-1$
		PropertyUpdate result = null;
		try {
			result = new PropertyUpdate(element);
		} catch (MalformedElementException e) {
			Assert.isTrue(false, Policy.bind("assert.internalError")); //$NON-NLS-1$
		}
		return result;
	}

	/**
	 * Returns an <code>Enumeration</code> over this propertyupdate's set
	 * and remove property elements.
	 *
	 * @return an <code>Enumeration</code> of
	 *         <code>PropertyUpdate.Directive</code>s
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed
	 */
	public Enumeration getSetsAndRemoves() throws MalformedElementException {

		Node setOrRemove = getFirstChild(root, new String[] {"remove", "set"}); //$NON-NLS-1$ //$NON-NLS-2$
		ensureNotNull(Policy.bind("ensure.missingRemoveOrSetElmt"), setOrRemove); //$NON-NLS-1$
		Node property = null;

		while (setOrRemove != null && property == null) {
			Node prop = getFirstChild((Element) setOrRemove, "prop"); //$NON-NLS-1$
			ensureNotNull(Policy.bind("ensure.missingPropElmt"), prop); //$NON-NLS-1$
			property = getChildElement((Element) prop, true);

			if (property == null)
				setOrRemove = getNextSibling((Element) setOrRemove, new String[] {"remove", "set"}); //$NON-NLS-1$ //$NON-NLS-2$
		}

		final Node a = setOrRemove;
		final Node c = property;

		Enumeration e = new Enumeration() {
			Node currentSetOrRemove = a;
			Node currentProperty = c;

			public boolean hasMoreElements() {

				return currentProperty != null;
			}

			public Object nextElement() {

				if (!hasMoreElements())
					throw new NoSuchElementException();
				Directive result = null;
				try {
					result = new Directive((Element) currentProperty);
				} catch (MalformedElementException ex) {
					Assert.isTrue(false, Policy.bind("assert.internalError")); //$NON-NLS-1$
				}
				currentProperty = getNextSibling((Element) currentProperty);
				while (currentSetOrRemove != null && currentProperty == null) {
					currentSetOrRemove = getNextSibling((Element) currentSetOrRemove, new String[] {"remove", "set"}); //$NON-NLS-1$ //$NON-NLS-2$
					if (currentSetOrRemove != null) {
						Node prop = getFirstChild((Element) currentSetOrRemove, "prop"); //$NON-NLS-1$
						if (prop != null)
							currentProperty = getChildElement((Element) prop, true);
					}
				}
				return result;
			}
		};

		return e;
	}
}
