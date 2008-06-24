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
 * An element editor for the WebDAV prop element. See RFC2518 section
 * 12.11 for the element's definition.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 *
 * @see PropStat
 * @see PropertyUpdate
 * @see PropFind
 * @see Property
 */
public class Prop extends ElementEditor {
	/**
	 * Creates a new editor on the given WebDAV prop element. The element
	 * is assumed to be well formed.
	 *
	 * @param root a prop element
	 * @throws        MalformedElementException if there is reason to
	 *                believe that the element is not well formed
	 */
	public Prop(Element root) throws MalformedElementException {
		super(root, "prop"); //$NON-NLS-1$
	}

	/**
	 * Creates a clone of the given element and adds it to this prop. The
	 * element must not be <code>null</code>.
	 *
	 * @param element any element
	 * @throws        MalformedElementException if there is reason to
	 *                believe that the given element is not well formed
	 */
	public void addProperty(Element element) throws MalformedElementException {
		Assert.isNotNull(element);
		extractNode(root, element);
	}

	/**
	 * Creates a new element with the given name and adds it to this prop.
	 * The name must not be <code>null</code> and its qualifier and local
	 * name must not be <code>null</code> and must not be the empty string.
	 *
	 * @param name the <code>QualifiedName</code> of the property to add
	 */
	public void addPropertyName(QualifiedName name) {
		Assert.isNotNull(name);
		String nsName = name.getQualifier();
		Assert.isTrue(!"".equals(nsName)); //$NON-NLS-1$
		String localName = name.getLocalName();
		Assert.isNotNull(localName);
		Assert.isTrue(!localName.equals("")); //$NON-NLS-1$
		Document document = root.getOwnerDocument();
		Element element = document.createElement(localName);
		declareNS(element, null, nsName);
		root.appendChild(element);
	}

	/**
	 * Returns an <code>Enumeration</code> over this prop's property
	 * <code>Element</code>s.
	 *
	 * @returns an <code>Enumeration</code> of </code>Element</code>s
	 * @throws  MalformedElementException if there is reason to believe that
	 *          this editor's underlying element is not well formed
	 */
	public Enumeration getProperties() throws MalformedElementException {
		Node firstChild = getChildElement(root, true);
		final Node firstElement = firstChild;
		Enumeration e = new Enumeration() {
			Node currentElement = firstElement;

			public boolean hasMoreElements() {
				return currentElement != null;
			}

			public Object nextElement() {
				if (!hasMoreElements())
					throw new NoSuchElementException();
				Node nextElement = currentElement;
				currentElement = getNextSibling((Element) currentElement);
				return nextElement;
			}
		};
		return e;
	}

	/**
	 * Returns an <code>Enumeration</code> over this prop's property
	 * <code>QualifiedName</code>s.
	 *
	 * @returns an <code>Enumeration</code> of </code>QualifiedName</code>s
	 * @throws  MalformedElementException if there is reason to believe that
	 *          this editor's underlying element is not well formed
	 */
	public Enumeration getPropertyNames() throws MalformedElementException {
		Node firstChild = getChildElement(root, true);
		final Node firstElement = firstChild;
		Enumeration e = new Enumeration() {
			Node currentElement = firstElement;

			public boolean hasMoreElements() {
				return currentElement != null;
			}

			public Object nextElement() {
				if (!hasMoreElements())
					throw new NoSuchElementException();
				String nsName = null;
				try {
					nsName = getNSName((Element) currentElement);
				} catch (MalformedElementException ex) {
					Assert.isTrue(false, Policy.bind("assert.internalError")); //$NON-NLS-1$
				}
				String localName = getNSLocalName((Element) currentElement);
				QualifiedNameImpl name = new QualifiedNameImpl(nsName, localName);
				currentElement = getNextSibling((Element) currentElement);
				return name;
			}
		};
		return e;
	}
}
