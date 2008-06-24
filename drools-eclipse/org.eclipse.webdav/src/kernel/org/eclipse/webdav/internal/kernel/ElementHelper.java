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
package org.eclipse.webdav.internal.kernel;

import java.util.Enumeration;
import org.eclipse.webdav.dom.QualifiedName;
import org.eclipse.webdav.internal.kernel.utils.Assert;
import org.w3c.dom.*;

public class ElementHelper implements WebDAVConstants {

	// This W3C Document is the factory for Nodes
	// produced by this class.
	protected Document nodeFactory;

	protected final static String DAV_PREFIX = "D"; //$NON-NLS-1$
	protected final static String NS_SEPARATOR = ":"; //$NON-NLS-1$
	protected final static String DAV_NS_ATTRIB = "xmlns" + NS_SEPARATOR + DAV_PREFIX; //$NON-NLS-1$

	public ElementHelper() {
		super();
		IDocumentFactory factory = new DocumentFactory();
		this.nodeFactory = factory.newDocument();
	}

	public String extractText(Element element) {
		NodeList childNodes = element.getChildNodes();
		Assert.isTrue(childNodes.getLength() == 1, Policy.bind("error.requireElementWithSingleChild")); //$NON-NLS-1$
		Node child = childNodes.item(0);
		Assert.isTrue(child instanceof Text, Policy.bind("error.extractFromElementWithoutText")); //$NON-NLS-1$
		return ((Text) child).getData();
	}

	public Element newDAVElement(QualifiedName name) {
		Assert.isTrue(name.getQualifier().equals(DAV_URI), Policy.bind("error.davElementsNeedQualifier")); //$NON-NLS-1$
		Element element = nodeFactory.createElement(DAV_PREFIX + NS_SEPARATOR + name.getLocalName());
		element.setAttribute(DAV_NS_ATTRIB, DAV_URI);
		return element;
	}

	/**
	 * Answer a DOM <code>Element</code> that represents a set of <code>String</code>.
	 * <p>
	 * The set is represented by a single <code>Element</code> whose name is
	 * given as the <code>setName</code> argument.  Each member of the set is
	 * a child <code>Element</code> named <code>memberName</code> that has
	 * text taken from the <code>memberEnum</code> an <code>Enumeration</code>
	 * of <code>String</code>.
	 */
	public Element newDAVElementSet(QualifiedName setName, QualifiedName memberName, Enumeration memberEnum) {
		Element setElement = newDAVElement(setName);
		while (memberEnum.hasMoreElements()) {
			String member = (String) memberEnum.nextElement();
			Element memberElement = newDAVTextElement(memberName, member);
			setElement.appendChild(memberElement);
		}
		return setElement;
	}

	public Element newDAVProperty(QualifiedName name, String value) {
		Element element = newDAVElement(name);
		element.appendChild(nodeFactory.createTextNode(value));
		return element;
	}

	public Element newDAVProperty(QualifiedName name, Element value) {
		Element element = newDAVElement(name);
		element.appendChild(value);
		return element;
	}

	public Element newDAVTextElement(QualifiedName name, String value) {
		Element element = newDAVElement(name);
		element.appendChild(nodeFactory.createTextNode(value));
		return element;
	}

	public Element newTextElement(String name, String value) {
		Element element = nodeFactory.createElement(name);
		element.appendChild(nodeFactory.createTextNode(value));
		return element;
	}
}
