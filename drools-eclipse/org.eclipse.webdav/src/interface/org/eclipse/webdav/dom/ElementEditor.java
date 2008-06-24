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
import java.util.Vector;
import org.eclipse.webdav.Policy;
import org.eclipse.webdav.internal.utils.URLDecoder;
import org.eclipse.webdav.internal.utils.URLEncoder;
import org.w3c.dom.*;
import org.w3c.dom.CharacterData;

/**
 * Abstract superclass of all element editors.  An element editor is a
 * convenience for manipulating DOM elements.  Specific subclasses declared
 * in this package correspond to the major DOM element types for the WebDAV
 * protocol; e.g., the class <code>ResponseBody</code> corresponds to
 * &lt;DAV:response&gt;.
 * <p>
 * Element editors contain no state of their own; all they they hang onto
 * is a single DOM <code>Element</code>; all information is stored in the DOM
 * itself.  Thus element editors are really just lightweight, throw-away
 * wrappers.</p>
 * <p>
 * Element editors assume that they are working with elements that are "up to
 * spec".  When an element editor encounters an element that is not how it
 * should be, it throws a <code>MalformedElementException</code> (a checked
 * exception).  The editor does not provide any further diagnosis of what's
 * wrong.  Clients catching this exception will generally conclude that they're
 * talking to a non-compliant (or malfunctioning) WebDAV server.</p>
 * <p>
 * Fully-capable element editors can be written using the DOM API.  No access
 * is required to any concrete implementation of the DOM.  The element editors
 * in this package all have this property.</p>
 * <p>
 * This class also provides rudimentary XML namespace support and element
 * navigation and manipulation methods that are useful when writing element
 * editors.</p>
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public abstract class ElementEditor {

	// The root element upon which this editor is based.
	// This field is never <code>null</code>.
	// However, it cannot be assumed that this element conforms
	// to the appropriate specification (the DOM can be changed
	// from underneath its editor).
	protected Element root;

	// The official URI for WebDAV namespace.
	protected static final String DAV_NS = "DAV:"; //$NON-NLS-1$

	// The official prefix for XML namespace declarations,
	// namely "xmlns" (note: no colon).
	protected static final String XML_PREFIX = "xmlns"; //$NON-NLS-1$

	// The official XML namespace prefix.
	protected static final String XML_NS_PREFIX = "xml"; //$NON-NLS-1$

	// The official XML namespace name.
	protected static final String XML_NS_NAME = "http://www.w3.org/XML/1998/namespace"; //$NON-NLS-1$

	/**
	 * Constructs a new element editor for the given element. The element
	 * must not be <code>null</code>.
	 *
	 * @param root the element to be edited
	 * @throws        MalformedElementException if the element is not null
	 */
	protected ElementEditor(Element root) throws MalformedElementException {
		Assert.isNotNull(root);
		this.root = root;
	}

	/**
	 * Constructs a new element editor for the given element. The element
	 * must not be <code>null</code>.
	 *
	 * @param root the element to be edited
	 * @throws        MalformedElementException if the element is not null
	 */
	protected ElementEditor(Element root, String expectedType) throws MalformedElementException {
		Assert.isNotNull(root);
		Assert.isNotNull(expectedType);
		this.root = root;
		ensureDAVElement(root, expectedType, Policy.bind("ensure.expectingAnElmt", expectedType)); //$NON-NLS-1$
	}

	/**
	 * <p>Creates a WebDAV element with the given name and adds it as a
	 * child of the given parent. Returns the child element.
	 * <p>Children are positioned in the order specified by the given names.
	 * If children with the same name as the child already exist, the child
	 * is inserted after the rightmost child.  If firstToLast is true, the
	 * search for the child's position starts at the parent's first child,
	 * otherwise, the search starts at the parent's last child.
	 * <p>The parent must not be <code>null</code> and must be a WebDAV
	 * element.  The child's name must not be <code>null</code>.  The
	 * parent's valid child names must not be <code>null</code>, and must
	 * contain the name of the child.
	 *
	 * @param parent      the parent to which the child is added
	 * @param name        the name of the child which is created and added
	 *                    to the parent
	 * @param names       the ordered collection of valid child names for
	 *                    the parent
	 * @param firstToLast a boolean specifying the direction to search for
	 *                    the child's position among the parent's children
	 * @return            the child element that is created
	 */
	public static Element addChild(Element parent, String name, String[] names, boolean firstToLast) {
		Assert.isTrue(isDAVElement(parent));
		Assert.isNotNull(name);
		Assert.isNotNull(names);
		String nsPrefix = getNSPrefix(parent);
		String tagName = nsPrefix == null ? name : nsPrefix + ":" + name; //$NON-NLS-1$
		Element child = parent.getOwnerDocument().createElement(tagName);
		addChild(parent, child, names, firstToLast);
		return child;
	}

	/**
	 * <p>Creates a WebDAV element with the given name and adds it as a
	 * child of the given parent. In addition, a text node created from the
	 * given data is created and appended to the child.  Returns the child
	 * element.
	 * <p>Children are positioned in the order specified by the given names.
	 * If children with the same name as the child already exist, the child
	 * is inserted after the rightmost child.  If firstToLast is true, the
	 * search for the child's position starts at the parent's first child,
	 * otherwise, the search starts at the parent's last child.
	 * <p>The parent must not be <code>null</code> and must be a WebDAV
	 * element.  The child's name and data must not be <code>null</code>.
	 * The parent's valid child names must not be <code>null</code>, and
	 * must contain the name of the child.
	 *
	 * @param parent      the parent to which the child is added
	 * @param name        the name of the child which is created and added
	 *                    to the parent
	 * @param data        the data of the text node which is created and
	 *                    added to the child
	 * @param names       the ordered collection of valid child names for
	 *                    the parent
	 * @param firstToLast a boolean specifying the direction to search for
	 *                    the child's position among the parent's children
	 * @return            the child element that is created
	 */
	public static Element addChild(Element parent, String name, String data, String[] names, boolean firstToLast) {
		Assert.isTrue(isDAVElement(parent));
		Assert.isNotNull(name);
		Assert.isNotNull(data);
		Assert.isNotNull(names);
		Element child = addChild(parent, name, names, firstToLast);
		child.appendChild(child.getOwnerDocument().createTextNode(data));
		return child;
	}

	/**
	 * Adds the given child element as a child of the given parent.
	 * <p>
	 * Children are positioned in the order specified by the given names.
	 * If children with the same name as the child already exist, the child
	 * is inserted after the rightmost child.  If firstToLast is true, the
	 * search for the child's position starts at the parent's first child,
	 * otherwise, the search starts at the parent's last child.</p>
	 * <p>
	 * The parent must not be <code>null</code> and must be a WebDAV
	 * element. The child must not be null and its namespace prefix must
	 * resolve to the WebDAV namespace URL in the parent. The parent's valid
	 * child names must not be <code>null</code>, and must contain the name
	 * of the child.</p>
	 *
	 * @param parent      the parent to which the child is added
	 * @param child       the child which is added to the parent
	 * @param names       the ordered collection of valid child names for
	 *                    the parent
	 * @param firstToLast a boolean specifying the direction to search for
	 *                    the child's position among the parent's children
	 */
	public static void addChild(Element parent, Element child, String[] names, boolean firstToLast) {

		Assert.isTrue(isDAVElement(parent));
		Assert.isNotNull(child);
		Assert.isTrue(DAV_NS.equals(resolve(getNSPrefix(child), parent)));
		Assert.isNotNull(names);

		boolean found = false;
		String name = getNSLocalName(child);
		for (int i = 0; !found && i < names.length; ++i)
			found = names[i].equals(name);
		Assert.isTrue(found);

		Node sibling = getChild(parent, name, names, firstToLast);

		if (firstToLast) {
			if (sibling == null)
				parent.appendChild(child);
			else {
				Node lastTwin = null;
				while (isDAVElement(sibling, name)) {
					lastTwin = sibling;
					sibling = getTwin((Element) sibling, firstToLast);
				}
				if (lastTwin == null)
					parent.insertBefore(child, sibling);
				else {
					Node refChild = lastTwin.getNextSibling();
					if (refChild == null)
						parent.appendChild(child);
					else
						parent.insertBefore(child, refChild);
				}
			}
		} else {
			Node refChild = null;
			if (sibling == null) {
				refChild = parent.getFirstChild();
			} else {
				refChild = sibling.getNextSibling();
			}
			if (refChild == null) {
				parent.appendChild(child);
			} else {
				parent.insertBefore(child, refChild);
			}
		}
	}

	/**
	 * Creates a WebDAV element with the given name and appends it as a
	 * child of the given parent. Returns the child element. The parent must
	 * not be <code>null</code> and must be a WebDAV element. The name of
	 * the child must not be <code>null</code>.
	 *
	 * @param parent the parent element to which the child is added
	 * @param name   the name of the child element that is created and added
	 *               as a child of the parent
	 * @return       the child element that is created
	 */
	public static Element appendChild(Element parent, String name) {
		Assert.isTrue(isDAVElement(parent));
		Assert.isNotNull(name);
		String nsPrefix = getNSPrefix(parent);
		String tagName = nsPrefix == null ? name : nsPrefix + ":" + name; //$NON-NLS-1$
		Element child = parent.getOwnerDocument().createElement(tagName);
		parent.appendChild(child);
		return child;
	}

	/**
	 * Creates a WebDAV element with the given name and appends it as a
	 * child of the given parent. In addition, a text node created from the
	 * given data is created and appended to the child. Returns the child
	 * element. The parent must not be <code>null</code> and must be a
	 * WebDAV element. The name of the child must not be <code>null</code>.
	 * The data must not be <code>null</code>.
	 *
	 * @param parent the parent element to which the child is added
	 * @param name   the name of the child element that is created and added
	 *               as a child of the parent
	 * @param data   the data of the text node which is created and added to
	 *               the child
	 * @return       the child element that is created
	 */
	public static Element appendChild(Element parent, String name, String data) {
		Assert.isTrue(isDAVElement(parent));
		Assert.isNotNull(name);
		Assert.isNotNull(data);
		Element child = appendChild(parent, name);
		child.appendChild(child.getOwnerDocument().createTextNode(data));
		return child;
	}

	/**
	 * Returns a clone of the given node.  The given document becomes the
	 * owner document of the clone.
	 *
	 * @param document the owner document of the clone
	 * @param node     the node to clone
	 * @return         a clone of the given node
	 */
	public static Node cloneNode(Document document, Node node) {

		Node nodeClone = null;

		switch (node.getNodeType()) {
			case Node.ELEMENT_NODE :
				{
					nodeClone = document.createElement(((Element) node).getTagName());
					NamedNodeMap namedNodeMap = node.getAttributes();
					for (int i = 0; i < namedNodeMap.getLength(); ++i) {
						Attr attr = (Attr) namedNodeMap.item(i);
						Attr attrClone = document.createAttribute(attr.getName());
						attrClone.setValue(attr.getValue());
						((Element) nodeClone).setAttributeNode(attrClone);
					}
				}
				break;
			case Node.TEXT_NODE :
				nodeClone = document.createTextNode(((CharacterData) node).getData());
				break;
			case Node.CDATA_SECTION_NODE :
				nodeClone = document.createCDATASection(((CharacterData) node).getData());
				break;
			case Node.ENTITY_REFERENCE_NODE :
				nodeClone = document.createEntityReference(node.getNodeName());
				break;
			case Node.PROCESSING_INSTRUCTION_NODE :
				nodeClone = document.createProcessingInstruction(((ProcessingInstruction) node).getTarget(), ((ProcessingInstruction) node).getData());
				break;
			case Node.COMMENT_NODE :
				nodeClone = document.createComment(((CharacterData) node).getData());
				break;
			case Node.DOCUMENT_FRAGMENT_NODE :
				nodeClone = document.createDocumentFragment();
				break;
			case Node.DOCUMENT_NODE :
			case Node.DOCUMENT_TYPE_NODE :
			case Node.NOTATION_NODE :
			case Node.ATTRIBUTE_NODE :
			case Node.ENTITY_NODE :
				Assert.isTrue(false, Policy.bind("assert.notSupported")); //$NON-NLS-1$
				break;
			default :
				Assert.isTrue(false, Policy.bind("assert.unknownNodeType")); //$NON-NLS-1$
		}

		return nodeClone;
	}

	/**
	 * Creates a WebDAV element with the given name and adds it as the root
	 * of the given document. In addition, the WebDAV namespace is declared
	 * on the new element. Returns the new element. The document must not be
	 * <code>null</code> and must not already have a root element. The name
	 * of the element to be created must not be <code>null</code>.
	 *
	 * @param document the document in which the new element is rooted
	 * @param name     the name of the element to be created
	 * @return         the rooted element
	 */
	public static Element create(Document document, String name) {
		Assert.isNotNull(document);
		Assert.isTrue(document.getDocumentElement() == null);
		Assert.isNotNull(name);
		Element element = document.createElement(name);
		declareNS(element, null, DAV_NS);
		document.appendChild(element);
		return element;
	}

	/**
	 * Adds a namespace declaration to the given element. If only the prefix
	 * is <code>null</code>, a default namespace is declared. If the prefix
	 * and the namespaceUrl are <code>null</code> the default namespace is
	 * removed. The element must not be <code>null</code>. If the
	 * namespaceUrl is <code>null</code>, the <code>prefix</code> must also
	 * be <code>null</code>.
	 *
	 * @param element      the element to receive the namespace attribute
	 * @param prefix       the prefix to be used (without a colon); for
	 *                     example, "D", or <code>null</code>
	 * @param namespaceUrl the URL of the namespace; for example, "DAV:", or
	 *                     <code>null</code>
	 */
	public static void declareNS(Element element, String prefix, String namespaceUrl) {
		Assert.isNotNull(element);
		Assert.isTrue(namespaceUrl != null || prefix == null && namespaceUrl == null);
		String name = XML_PREFIX + (prefix == null ? "" : ":" + prefix); //$NON-NLS-1$ //$NON-NLS-2$
		String value = namespaceUrl == null ? "" : namespaceUrl; //$NON-NLS-1$
		element.setAttribute(name, value);
	}

	/**
	 * Decodes the given href from a form that is safe for transport.
	 *
	 * @param href the href to be decoded
	 */
	public static String decodeHref(String href) {
		return URLDecoder.decode(href);
	}

	/**
	 * Encodes the given href to a form that is safe for transport.
	 *
	 * @param href the href to be encoded
	 */
	public static String encodeHref(String href) {
		return URLEncoder.encode(href);
	}

	/**
	 * Ensures that the given value is <code>true</code>.
	 *
	 * @param message the message for the exception that is thrown if the
	 *                given value is <code>false</code>
	 * @param value   a boolean indicating whether or not a malformed
	 *                element exception should be thrown
	 * @throws        MalformedElementException with the given message if
	 *                the given value is <code>false</code>
	 * @deprecated
	 */
	protected static void ensure(String message, boolean value) throws MalformedElementException {
		if (!value)
			throw new MalformedElementException(message);
	}

	/**
	 * Ensures that the given value is <code>true</code>.
	 *
	 * @param message the message for the exception that is thrown if the
	 *                given value is <code>false</code>
	 * @param value   a boolean indicating whether or not a malformed
	 *                element exception should be thrown
	 * @throws        MalformedElementException with the given message if
	 *                the given value is <code>false</code>
	 */
	protected static void ensure(boolean value, String message) throws MalformedElementException {
		if (!value)
			throw new MalformedElementException(message);
	}

	/**
	 * Ensures that the given node is a WebDAV element with the given name,
	 * returning it as an <code>Element</code> if it is.
	 *
	 * @param message the message for the exception that is thrown if the
	 *                node is not a WebDAV element with the given name
	 * @param node    the node, or <code>null</code>
	 * @param name    the expected local name of the WebDAV element
	 * @returns       the node as an <code>Element</code>
	 * @throws        MalformedElementException with the given message if it
	 *                is not a WebDAV element with the given name
	 * @deprecated
	 */
	protected static Element ensureDAVElement(String message, Node node, String name) throws MalformedElementException {
		Assert.isNotNull(name);
		if (node == null || node.getNodeType() != Node.ELEMENT_NODE)
			throw new MalformedElementException(message);
		Element element = (Element) node;
		if (!name.equals(getNSLocalName(element)) || !DAV_NS.equals(getNSName(element)))
			throw new MalformedElementException(message);
		return element;
	}

	/**
	 * Ensures that the given node is a WebDAV element with the given name.
	 *
	 * @param node    the node, or <code>null</code>
	 * @param name    the expected local name of the WebDAV element
	 * @param message the message for the exception that is thrown if the
	 *                node is not a WebDAV element with the given name
	 * @throws        MalformedElementException with the given message if it
	 *                is not a WebDAV element with the given name
	 */
	protected static void ensureDAVElement(Node node, String name, String message) throws MalformedElementException {
		Assert.isNotNull(name);
		if (node == null || node.getNodeType() != Node.ELEMENT_NODE)
			throw new MalformedElementException(message);
		Element element = (Element) node;
		if (!name.equals(getNSLocalName(element)) || !DAV_NS.equals(getNSName(element)))
			throw new MalformedElementException(message);
	}

	/**
	 * Ensures that the given object is not <code>null</code>.
	 *
	 * @param message the message for the exception that is thrown if the
	 *                object is <code>null</code>
	 * @param object  the object, or <code>null</code>
	 * @throws        MalformedElementException if the given object is
	 *                <code>null</code>
	 */
	protected static void ensureNotNull(String message, Object object) throws MalformedElementException {
		if (object == null)
			throw new MalformedElementException(message);
	}

	/**
	 * Ensures that the given object is <code>null</code>.
	 *
	 * @param message the message for the exception that is thrown if the
	 *                object is not <code>null</code>
	 * @param object  the object, or <code>null</code>
	 * @throws        MalformedElementException if the given object is not
	 *                <code>null</code>
	 */
	protected static void ensureNull(String message, Object object) throws MalformedElementException {
		if (object != null)
			throw new MalformedElementException(message);
	}

	/**
	 * Ensures that the given node is a text node, returning it as a
	 * <code>Text</code> node if it is.
	 *
	 * @param message the message for the exception that is thrown if the
	 *                node is not a <code>Text</code> node
	 * @param node    the node, or <code>null</code>
	 * @returns       the node as a <code>Text</code> node
	 * @exception     MalformedElementException if the node is
	 *                <code>null</code> or not a text node
	 */
	protected static Text ensureText(String message, Node node) throws MalformedElementException {
		if (node == null || node.getNodeType() != Node.TEXT_NODE)
			throw new MalformedElementException(message);
		return (Text) node;
	}

	/**
	 * Clones the given element, and its subtrees, and sets it as the root
	 * of the given document. Returns the cloned element. The document must
	 * not have a root and must not be <code>null</code>. The element must
	 * not be <code>null</code>.
	 *
	 * @param document the document that will become the owner and parent
	 *                 of the cloned element
	 * @param element  the element to be cloned
	 * @returns        a clone of the element
	 * @throws         MalformedElementException if an element exists in the
	 *                 subtree to be cloned whose namespace can't be
	 *                 resolved
	 */
	public static Element extractElement(Document document, Element element) throws MalformedElementException {
		Assert.isNotNull(document);
		Assert.isTrue(document.getDocumentElement() == null);
		Assert.isNotNull(element);
		return (Element) extractNode(document, element);
	}

	/**
	 * Clones the given node, and its subtrees, and sets it as the root of
	 * the given parent node. Returns the cloned node. The parent node and
	 * the node to be cloned must not be <code>null</code>.
	 *
	 * @param parent  the node that will become the parent of the cloned
	 *                node
	 * @param node the node to be cloned
	 * @returns       a clone of the element
	 * @throws        MalformedElementException if an element exists in the
	 *                subtree to be cloned whose namespace can't be resolved
	 */
	public static Node extractNode(Node parent, Node node) throws MalformedElementException {

		// Get a handle to the root of the parent node tree.
		Document document;
		if (parent.getNodeType() == Node.DOCUMENT_NODE)
			document = (Document) parent;
		else
			document = parent.getOwnerDocument();

		// Create a clone of the node owned by the document, and add to the parent.
		Node nodeClone = cloneNode(document, node);
		parent.appendChild(nodeClone);

		// If the node is an Element
		if (node.getNodeType() == Node.ELEMENT_NODE) {

			// Figure out its namespace information.
			String nsPrefix = getNSPrefix((Element) node);
			String nsName = getNSName((Element) node);

			// Is this namespace already defined on the clone?  If not declare it.
			String nsNameClone = resolve(nsPrefix, (Element) nodeClone);
			if (nsName != nsNameClone && (nsName == null || !nsName.equals(nsNameClone)))
				declareNS((Element) nodeClone, nsPrefix, nsName);

			// Do the same namespace fix-up for each of the node's attributes.
			NamedNodeMap nodeMap = nodeClone.getAttributes();
			for (int i = 0; i < nodeMap.getLength(); ++i) {
				Attr attr = (Attr) nodeMap.item(i);
				nsPrefix = getNSPrefix(attr.getName());
				if (nsPrefix != null && !nsPrefix.equals(XML_PREFIX)) {
					nsName = resolve(nsPrefix, (Element) node);
					nsNameClone = resolve(nsPrefix, (Element) nodeClone);
					if (nsName != nsNameClone && (nsName == null || !nsName.equals(nsNameClone)))
						declareNS((Element) nodeClone, nsPrefix, nsName);
				}
			}
		}

		// Recursively clone each of the node's children.
		Node child = node.getFirstChild();
		while (child != null) {
			extractNode(nodeClone, child);
			child = child.getNextSibling();
		}

		// Finished cloning this node.
		return nodeClone;
	}

	/**
	 * Returns the first child of the given parent that is a WebDAV element
	 * with one of the given names, or <code>null</code> if no such child
	 * exists.
	 * <p>
	 * If firstToLast is true, the search for the child starts at the parent's
	 * first child, otherwise, the search starts at the parent's last child.
	 * The parent must not be <code>null</code> and must be a WebDAV element.
	 * The names of children to search for must not be <code>null</code>.</p>
	 *
	 * @param parent      the parent of the child to search.
	 * @param names       all possible names of the child to search for.
	 * @param firstToLast a boolean specifying the direction to search for
	 *                    the child among the parent's children.
	 * @return            the specified child of the parent, or
	 *                    <code>null<code> if no such child exists.
	 */
	private static Element getChild(Element parent, String[] names, boolean firstToLast) {

		Assert.isTrue(isDAVElement(parent));
		Assert.isNotNull(names);

		// Get the first candidate.
		Node child = null;
		if (firstToLast)
			child = parent.getFirstChild();
		else
			child = parent.getLastChild();

		// While there are children left to consider.
		while (child != null) {

			// See if the child name matches any being sought.
			for (int i = 0; i < names.length; ++i)
				if (isDAVElement(child, names[i]))
					return (Element) child;

			// Try the next child.
			if (firstToLast)
				child = child.getNextSibling();
			else
				child = child.getPreviousSibling();
		}

		// A matching child was not found.
		return null;
	}

	/**
	 * <p>Returns the child WebDAV element of the given parent that is
	 * nearest in position to a WebDAV element with the given name, or
	 * <code>null</code> if no such child exists.
	 * <p>Children are expected to be in the order specified by the given
	 * names. If firstToLast is true, the search for the child starts at the
	 * parent's first child, otherwise, the search starts at the parent's
	 * last child.
	 * <p>The parent must not be <code>null</code> and must be a WebDAV
	 * element.  The name of the child to search for must not be
	 * <code>null</code>. The parent's valid child names must not be
	 * <code>null</code>, and must contain the name of the child being
	 * searched for.
	 * <p>The returned child is as follows:
	 * <ul>
	 * <li>Searching first to last</li>
	 *   <ul>
	 *   <li>returns <code>null</code> if an element with the given name
	 *       should appear as the last child</li>
	 *   <li>returns the first occurring child if an element with the given
	 *       name is a child</li>
	 *   <li>returns a child if an element with the given name would appear
	 *       before it</li>
	 *   </ul>
	 * <li>Searching last to first</li>
	 *   <ul>
	 *   <li>returns <code>null</code> if an element with the given name
	 *               would appear as the first child</li>
	 *   <li>returns the last occurring child if an element with the given
	 *       name is a child</li>
	 *   <li>returns a child if an element with the given name would appear
	 *       after it</li>
	 *   </ul>
	 * </ul>
	 * @param parent      the parent of the child being searched for
	 * @param name        the name of the child being searched for
	 * @param names       the ordered collection of valid child names for
	 *                    the parent
	 * @param firstToLast a boolean specifying the direction to search for
	 *                    the specified child among the parent's children
	 * @return            a child of the parent, or <code>null</code>
	 */
	public static Element getChild(Element parent, String name, String[] names, boolean firstToLast) {
		Assert.isNotNull(parent);
		Assert.isNotNull(name);
		Assert.isNotNull(names);

		boolean found = false;
		for (int i = 0; !found && i < names.length; ++i) {
			found = names[i].equals(name);
		}
		Assert.isTrue(found);

		int i;
		Node child = null;

		if (firstToLast) {
			i = 0;
			child = parent.getFirstChild();
		} else {
			i = names.length - 1;
			child = parent.getLastChild();
		}

		while (child != null && !names[i].equals(name)) {
			int mark = i;
			while (!isDAVElement(child, names[i]) && !names[i].equals(name)) {
				if (firstToLast) {
					++i;
				} else {
					--i;
				}
			}

			if (!names[i].equals(name)) {
				if (firstToLast) {
					child = child.getNextSibling();
				} else {
					child = child.getPreviousSibling();
				}
			} else if (!isDAVElement(child, names[i])) {
				int pos = i;
				found = false;
				while (!found && (pos >= 0 && pos < names.length)) {
					found = isDAVElement(child, names[pos]);
					if (firstToLast) {
						++pos;
					} else {
						--pos;
					}
				}
				if (!found) {
					i = mark;
					if (firstToLast) {
						child = child.getNextSibling();
					} else {
						child = child.getPreviousSibling();
					}
				}
			}
		}

		return (Element) child;
	}

	/**
	 * Returns the first child element of the given parent element, or
	 * <code>null</code> if no such child exists. If firstToLast is true,
	 * the search for the child starts at the parent's first child,
	 * otherwise, the search starts at the parent's last child. The parent
	 * must not be <code>null</code>.
	 *
	 * @param parent      the parent of the child element to search for
	 * @param firstToLast a boolean specifying the direction to search for
	 *                    the child element among the parent's children
	 * @return            the child element, or <code>null</code> if no such
	 *                    child exists
	 */
	public static Element getChildElement(Element parent, boolean firstToLast) {
		Assert.isNotNull(parent);
		Node child = null;
		if (firstToLast)
			child = parent.getFirstChild();
		else
			child = parent.getLastChild();
		while (child != null && !isElement(child)) {
			if (firstToLast)
				child = child.getNextSibling();
			else
				child = child.getPreviousSibling();
		}
		return (Element) child;
	}

	/**
	 * Returns the data of the first child text node of the first WebDAV
	 * child with the given name of the given parent, or the empty
	 * <code>String</code> if no such child text node exists, or
	 * <code>null</code> if no such child exists. If firstToLast is true, the
	 * search for the child starts at the parent's first child, otherwise,
	 * the search starts at the parent's last child. The parent must not be
	 * <code>null</code> and must be a WebDAV element. The name of the child
	 * must not be <code>null</code>.
	 *
	 * @param parent      the parent of the child with the child text node
	 *                    to search for
	 * @param name        the name of the child with the child text node to
	 *                    search for
	 * @param firstToLast a boolean specifying the direction to search for
	 *                    the specified child among the parent's children
	 * @return            the data of the child's child text node, or the
	 *                    empty <code>String</code> if no such child exists,
	 *                    or <code>null</code> if no such child exists
	 */
	public static String getChildText(Element parent, String name, boolean firstToLast) {
		Assert.isTrue(isDAVElement(parent));
		Assert.isNotNull(name);
		Element child;
		if (firstToLast)
			child = getFirstChild(parent, name);
		else
			child = getLastChild(parent, name);
		if (child != null)
			return getText(child, firstToLast);
		return null;
	}

	/**
	 * Returns the first WebDAV child of the given parent, or
	 * <code>null</code> if no such child exists. The parent must not be
	 * <code>null</code> and must be a WebDAV element.
	 *
	 * @param parent the parent of the child to search for
	 * @return the first WebDAV child of the parent, or null
	 */
	public static Element getDAVChild(Element parent) {
		Assert.isTrue(isDAVElement(parent));
		Node child = parent.getFirstChild();
		while (child != null && !isDAVElement(child))
			child = child.getNextSibling();
		return (Element) child;
	}

	/**
	 * Returns the root element upon which this editor is based. It cannot
	 * be assumed that this element necessarily conforms to the appropriate
	 * specification.
	 *
	 * @return the root element upon which this editor is based
	 */
	public Element getElement() {
		return root;
	}

	/**
	 * Returns the first child of the given parent that is a WebDAV element
	 * with one of the given names, or <code>null</code> if no such child
	 * exists.
	 * <p>
	 * The search for the child starts at the parent's first child.
	 * The parent must not be <code>null</code> and must be a WebDAV element.
	 * The names of children to search for must not be <code>null</code>.</p>
	 *
	 * @param parent      the parent of the child to search.
	 * @param names       all possible names of the child to search for.
	 * @return            the specified child of the parent, or
	 *                    <code>null<code> if no such child exists.
	 */
	public static Element getFirstChild(Element parent, String[] names) {
		return getChild(parent, names, true);
	}

	/**
	 * Returns the first child of the given parent that is a WebDAV element
	 * with the given name, or <code>null</code> if no such child exists.
	 * <p>
	 * The search for the child starts at the parent's first child.
	 * The parent must not be <code>null</code> and must be a DAV: namespace
	 * element. The name of the child to search for must not be
	 * <code>null</code>.
	 *
	 * @param parent      the parent of the child to search.
	 * @param name        the name of the child to search for.
	 * @return            the specified child of the parent, or <code>null
	 *                    </code> if no such child exists.
	 */
	public static Element getFirstChild(Element parent, String name) {
		Assert.isNotNull(name);
		return getChild(parent, new String[] {name}, true);
	}

	/**
	 * Returns the data of the given parent's first text node, or the empty
	 * <code>String</code> if no such text node exists.  The search for the
	 * text node starts at the parent's first child. The parent must not be
	 * <code>null</code>.
	 *
	 * @param parent      the parent of the text node to search for
	 * @return            the data of the text node being searched for, or
	 *                    the empty <code>String</code> if no such text node
	 *                    exists
	 */
	public static String getFirstText(Element parent) {
		Assert.isNotNull(parent);
		Node child = parent.getFirstChild();
		while (child != null && !isText(child))
			child = child.getNextSibling();
		if (child == null)
			return ""; //$NON-NLS-1$
		return ((Text) child).getData();
	}

	/**
	 * Returns the last child of the given parent that is a WebDAV element
	 * with the given name, or <code>null</code> if no such child exists.
	 * <p>
	 * The search starts at the parent's last child.
	 * The parent must not be <code>null</code> and must be a DAV: namespace
	 * element. The name of the child to search for must not be
	 * <code>null</code>.
	 *
	 * @param parent      the parent of the child to search.
	 * @param name        the name of the child to search for.
	 * @return            the specified child of the parent, or <code>null
	 *                    </code> if no such child exists.
	 */
	public static Element getLastChild(Element parent, String name) {
		Assert.isNotNull(name);
		return getChild(parent, new String[] {name}, false);
	}

	/**
	 * Returns the given element's namespace declarations. The element must
	 * not be <code>null</code>.
	 *
	 * @param element the element whose namespace declarations are returned
	 * @return        the element's namespace declarations
	 */
	public static Namespaces getNamespaces(Element element) {
		Assert.isNotNull(element);
		Node parent = element.getParentNode();
		while (parent != null && !isElement(parent))
			parent = parent.getParentNode();
		Namespaces namespaces = null;
		if (parent != null)
			namespaces = getNamespaces((Element) parent);
		return getNamespaces(element, namespaces, false);
	}

	/**
	 * Returns the given element's namespace declarations. The given
	 * namespace declarations should be the element's parent's, or
	 * <code>null</code> if the element has no parent. If
	 * removeDuplicateNSDeclarations is <code>true</code>, duplicate
	 * namespace declarations are removed from the element's attributes. The
	 * element must not be <code>null</code>.
	 *
	 * @param element    the element whose namespace declaration is returned
	 * @param namespaces the namespace declarations for the element's
	 *                   parent's or <code>null</code> if it has no parent
	 * @param removeDuplicateNSDeclarations a boolean indicating whether
	 *                   duplicate namespace declarations should be removed
	 *                   from the element's attributes
	 * @return           the given element's namespace declarations
	 */
	protected static Namespaces getNamespaces(Element element, Namespaces namespaces, boolean removeDuplicateNSDeclarations) {

		// Create a container to hold the new namespace definitions.
		Namespaces newNamespaces = null;
		if (namespaces == null)
			newNamespaces = new Namespaces();
		else
			newNamespaces = new Namespaces(namespaces);

		Vector oldAttributes = new Vector();

		// For each attribute on the given element.
		NamedNodeMap nodeMap = element.getAttributes();
		for (int i = 0; i < nodeMap.getLength(); ++i) {
			Attr attr = (Attr) nodeMap.item(i);

			// Is it a name space declaration?
			String name = attr.getName();
			if (name.startsWith(XML_PREFIX)) {
				String nsName = attr.getValue();

				// Is it setting or clearing the default namespace?
				// (i.e. has no prefix part)
				if (name.length() == XML_PREFIX.length()) {
					if (nsName.equals("")) //$NON-NLS-1$
						newNamespaces.setDefaultNSName(null);
					else
						newNamespaces.setDefaultNSName(nsName);
				} else if (name.charAt(XML_PREFIX.length()) == ':') {
					// It is a namespace declaration.
					String nsPrefix = name.substring(XML_PREFIX.length() + 1);
					if (nsPrefix.length() > 0 && nsName.length() > 0) {
						// Ensure it is in the new namespaces list.
						newNamespaces.putNSName(nsPrefix, nsName);
						boolean prefixExists = newNamespaces.getNSPrefix(nsName) != null;
						if (!prefixExists)
							newNamespaces.putNSPrefix(nsName, nsPrefix);
						// If it is due for removal, rememebr it in the oldAttributes list.
						if (removeDuplicateNSDeclarations && (prefixExists || nsName.equals(newNamespaces.getDefaultNSName())))
							oldAttributes.addElement(attr);
					}
				}
			}
		}

		// Remove all the duplicates on the given element.
		Enumeration e = oldAttributes.elements();
		while (e.hasMoreElements())
			element.removeAttributeNode((Attr) e.nextElement());

		// Answer the new list of namespaces for this element.
		return newNamespaces;
	}

	/**
	 * Returns the next sibling element of the given element, or
	 * <code>null</code> if no such sibling exists. Only the sibling's
	 * next children are searched. The element must not be <code>null</code>.
	 *
	 * @param element     the element whose sibling element is being
	 *                    searched for
	 * @return            the sibling element, or <code>null</code>
	 */
	public static Element getNextSibling(Element element) {
		Assert.isNotNull(element);
		Node sibling = element;
		do {
			sibling = sibling.getNextSibling();
		} while (sibling != null && !isElement(sibling));
		return (Element) sibling;
	}

	/**
	 * Returns the first WebDAV sibling of the given element that has one of
	 * the given names, or <code>null</code> if no such sibling exists.  Only
	 * the sibling's next children (not the previous children) are searched.
	 * The element must not be <code>null</code> and must be a WebDAV element.
	 * The possible names of the sibling to search for must not be
	 * <code>null</code>.
	 *
	 * @param element     the element whose sibling is being searched for
	 * @param names       the possible names of the sibling being search for
	 * @return            the sibling with the specified name, or
	 *                    <code>null</code>
	 */
	public static Element getNextSibling(Element element, String[] names) {
		Assert.isTrue(isDAVElement(element));
		Assert.isNotNull(names);
		Node sibling = element.getNextSibling();
		while (sibling != null) {
			for (int i = 0; i < names.length; ++i)
				if (isDAVElement(sibling, names[i]))
					return (Element) sibling;
			sibling = sibling.getNextSibling();
		}
		return null;
	}

	/**
	 * Returns the next WebDAV sibling of the given element that has the
	 * given name, or <code>null</code> if no such sibling exists.  Only
	 * the sibling's next children are searched. The element must not be
	 * <code>null</code> and must be a WebDAV element.  The name of the
	 * sibling to search for must not be <code>null</code>.
	 *
	 * @param element     the element whose sibling is being searched for
	 * @param name       the name of the sibling being search for
	 * @return            the sibling with the given name, or
	 *                    <code>null</code>
	 */
	public static Element getNextSibling(Element element, String name) {
		return getNextSibling(element, new String[] {name});
	}

	/**
	 * Returns the local part of the given name, or <code>null</code> if its
	 * name has no local part. The name must not be <code>null</code>.
	 * <table>
	 * <caption>Example</caption>
	 * <tr>
	 *   <th>name</th>
	 *   <th>local name</th>
	 * <tr>
	 *   <td>D:foo</td>
	 *   <td>foo</td>
	 * <tr>
	 *   <td>foo</td>
	 *   <td>foo</td>
	 * <tr>
	 *   <td>D:</td>
	 *   <td>null</td>
	 * <tr>
	 *   <td>:foo</td>
	 *   <td>foo</td>
	 * <tr>
	 *   <td>:</td>
	 *   <td>null</td>
	 * </table>
	 *
	 * @param name the name whose local part is returned
	 * @return     the name's local part, or <code>null</code>
	 */
	public static String getNSLocalName(String name) {
		Assert.isNotNull(name);
		int p = name.lastIndexOf(':');
		if (p == -1)
			return name;
		if (p == name.length() - 1)
			return null;
		return name.substring(p + 1);
	}

	/**
	 * Returns the local part of the name of the given element, or
	 * <code>null</code> if its name has no local part. The element must not
	 * be <code>null</code>.
	 * <table>
	 * <caption>Example</caption>
	 * <tr>
	 *   <th>tag name</th>
	 *   <th>local name</th>
	 * <tr>
	 *   <td>D:foo</td>
	 *   <td>foo</td>
	 * <tr>
	 *   <td>foo</td>
	 *   <td>foo</td>
	 * <tr>
	 *   <td>D:</td>
	 *   <td>null</td>
	 * <tr>
	 *   <td>:foo</td>
	 *   <td>foo</td>
	 * <tr>
	 *   <td>:</td>
	 *   <td>null</td>
	 * </table>
	 *
	 * @param element the element whose local name is returned
	 * @return        the element's local name, or <code>null</code>
	 */
	public static String getNSLocalName(Element element) {
		Assert.isNotNull(element);
		return getNSLocalName(element.getTagName());
	}

	/**
	 * Returns the URL of the given element's namespace, or
	 * <code>null</code> if it has no namespace. The element must not be
	 * <code>null</code>.
	 *
	 * @param element the element whose namespace is returned
	 * @return        the namespace of the element, or <code>null</code>
	 * @throws        MalformedElementException if the name of the given
	 *                element has a namespace prefix which could not be
	 *                resolved
	 */
	public static String getNSName(Element element) throws MalformedElementException {
		Assert.isNotNull(element);
		String nsPrefix = getNSPrefix(element);
		String nsName = resolve(nsPrefix, element);
		if (nsPrefix != null && nsName == null)
			throw new MalformedElementException(Policy.bind("exception.namespacePrefixNotResolved", nsPrefix)); //$NON-NLS-1$
		return nsName;
	}

	/**
	 * Returns the namespace prefix part of the given name, or
	 * <code>null</code> if its name has no prefix. The name must not be
	 * <code>null</code>.
	 * <table>
	 * <caption>Example</caption>
	 * <tr>
	 *   <th>name</th>
	 *   <th>namespace prefix</th>
	 * <tr>
	 *   <td>D:foo</td>
	 *   <td>D</td>
	 * <tr>
	 *   <td>foo</td>
	 *   <td>null</td>
	 * <tr>
	 *   <td>D:</td>
	 *   <td>D</td>
	 * <tr>
	 *   <td>:foo</td>
	 *   <td>null</td>
	 * <tr>
	 *   <td>:</td>
	 *   <td>null</td>
	 * </table>
	 *
	 * @param name the name whose namespace prefix is returned
	 * @return     the name's namespace prefix, or <code>null</code>
	 */
	public static String getNSPrefix(String name) {
		Assert.isNotNull(name);
		int p = name.lastIndexOf(':');
		if (p <= 0)
			return null;
		return name.substring(0, p);
	}

	/**
	 * Returns the namespace prefix part of the name of the given element,
	 * or <code>null</code> if its name has no prefix. The element must not
	 * be <code>null</code>.
	 * <table>
	 * <caption>Example</caption>
	 * <tr>
	 *   <th>tag name</th>
	 *   <th>namespace prefix</th>
	 * <tr>
	 *   <td>D:foo</td>
	 *   <td>D</td>
	 * <tr>
	 *   <td>foo</td>
	 *   <td>null</td>
	 * <tr>
	 *   <td>D:</td>
	 *   <td>D</td>
	 * <tr>
	 *   <td>:foo</td>
	 *   <td>null</td>
	 * <tr>
	 *   <td>:</td>
	 *   <td>null</td>
	 * </table>
	 *
	 * @param element the element whose namespace prefix is returned
	 * @return        the element's namespace prefix, or <code>null</code>
	 */
	public static String getNSPrefix(Element element) {
		Assert.isNotNull(element);
		return getNSPrefix(element.getTagName());
	}

	/**
	 * Returns a qualified name that is formed from the given element's
	 * namespace name and namespace local name. The qualified name's
	 * qualifier is the element's namespace name and the qualified name's
	 * local name is the element's local name. The element must not be
	 * <code>null</code>.
	 *
	 * @param     element the element whose qualified name is returned
	 * @return            the qualified name that is formed from the given
	 *                    element's namespace name and namespace local name
	 * @exception MalformedElementException if the name of the element has a
	 *            namespace prefix which could not be resolved, or the
	 *            element does not have a local name
	 */
	public static QualifiedName getQualifiedName(Element element) throws MalformedElementException {
		Assert.isNotNull(element);
		String nsName = getNSName(element);
		String nsLocalName = getNSLocalName(element);
		if (nsLocalName == null)
			throw new MalformedElementException(Policy.bind("exception.noLocalNameForElmt")); //$NON-NLS-1$
		return new QualifiedNameImpl(nsName, nsLocalName);
	}

	/**
	 * Returns the first WebDAV sibling of the given element that has the
	 * given name, or <code>null</code> if no such sibling exists. If
	 * firstToLast is true, only the sibling's next children are searched,
	 * otherwise, only the siblings previous children are searched. The
	 * element must not be <code>null</code> and must be a WebDAV element.
	 * The name of the sibling to search for must not be <code>null</code>.
	 *
	 * @param element the element whose sibling is being searched for
	 * @param name the name of the sibling being search for
	 * @param firstToLast a boolean specifying the direction to search for
	 *                    the sibling among the element's siblings
	 * @return            the sibling with the given name, or
	 *                    <code>null</code>
	 * @deprecated
	 */
	public static Element getSibling(Element element, String name, boolean firstToLast) {
		Assert.isTrue(isDAVElement(element));
		Assert.isNotNull(name);
		Node sibling = element;
		do {
			if (firstToLast)
				sibling = sibling.getNextSibling();
			else
				sibling = sibling.getPreviousSibling();
		} while (sibling != null && !isDAVElement(sibling, name));
		return (Element) sibling;
	}

	/**
	 * Returns the data of the given parent's first text node, or the empty
	 * <code>String</code> if no such text node exists. If firstToLast is
	 * true, the search for the text node starts at the parent's first
	 * child, otherwise, the search starts at the parent's last child. The
	 * parent must not be <code>null</code>.
	 *
	 * @param parent      the parent of the text node to search for
	 * @param firstToLast a boolean specifying the direction to search for
	 *                    the text node among the parent's children
	 * @return            the data of the text node being searched for, or
	 *                    the empty <code>String</code> if no such text node
	 *                    exists
	 * @deprecated
	 */
	public static String getText(Element parent, boolean firstToLast) {
		Assert.isNotNull(parent);
		Node child = null;
		if (firstToLast)
			child = parent.getFirstChild();
		else
			child = parent.getLastChild();
		while (child != null && !isText(child)) {
			if (firstToLast)
				child = child.getNextSibling();
			else
				child = child.getPreviousSibling();
		}
		if (child != null)
			return ((Text) child).getData();
		return ""; //$NON-NLS-1$
	}

	/**
	 * Returns the first WebDAV sibling of the given element that has the
	 * same name as the element, or <code>null</code> if no such sibling
	 * exists. If firstToLast is true, only the sibling's next children are
	 * searched, otherwise, only the siblings previous children are
	 * searched. The element must not be <code>null</code> and must be a
	 * WebDAV element.
	 *
	 * @param element     the element whose sibling is being searched for
	 * @param firstToLast a boolean specifying the direction to search for
	 *                    the sibling among the element's siblings
	 * @return            the sibling with the same name as the given
	 *                    element, or <code>null</code> if no such element
	 *                    exists
	 */
	public static Element getTwin(Element element, boolean firstToLast) {
		Assert.isTrue(isDAVElement(element));
		String name = getNSLocalName(element);
		return getSibling(element, name, firstToLast);
	}

	public static boolean hasChild(Element parent, QualifiedName childName) throws MalformedElementException {
		// Get the first candidate.
		Node child = parent.getFirstChild();
		// While there are children left to consider.
		while (child != null) {
			if (child instanceof Element) {
				QualifiedName name = getQualifiedName((Element) child);
				if (name.equals(childName))
					return true;
			}
			// Try the next child.
			child = child.getNextSibling();
		}
		// A matching child was not found.
		return false;
	}

	/**
	 * Creates a WebDAV element with the given name and inserts it before
	 * the given sibling. Returns the new sibling. The sibling must not be
	 * <code>null</code> and must be a WebDAV element. The name of the
	 * new sibling must not be <code>null</code>.
	 *
	 * @param sibling the existing sibling element
	 * @param name    the name of the new sibling element that is created
	 *                and inserted before the existing sibling element
	 * @return        the new sibling element that is created
	 */
	public static Element insertBefore(Element sibling, String name) {
		Assert.isTrue(isDAVElement(sibling));
		Assert.isNotNull(name);
		String nsPrefix = getNSPrefix(sibling);
		String tagName = nsPrefix == null ? name : nsPrefix + ":" + name; //$NON-NLS-1$
		Element newSibling = sibling.getOwnerDocument().createElement(tagName);
		sibling.getParentNode().insertBefore(newSibling, sibling);
		return newSibling;
	}

	/**
	 * Creates a WebDAV element with the given name and inserts it before
	 * the given sibling. In addition, a text node created from the given
	 * data and appended to the new sibling. Returns the new sibling. The
	 * sibling must not be <code>null</code> and must be a WebDAV element.
	 * The name of the new sibling must not be <code>null</code>. The data
	 * must not be <code>null</code>.
	 *
	 * @param sibling the existing sibling element
	 * @param name    the name of the sibling element that is created and
	 *                inserted before the existing sibling element
	 * @param data    the data of the text node which is created and
	 *                appended to the new sibling
	 * @return        the new sibling element that is created
	 */
	public static Element insertBefore(Element sibling, String name, String data) {
		Assert.isTrue(isDAVElement(sibling));
		Assert.isNotNull(name);
		Assert.isNotNull(data);
		Element child = insertBefore(sibling, name);
		child.appendChild(child.getOwnerDocument().createTextNode(data));
		return child;
	}

	/**
	 * Returns a boolean indicating whether or not the given node is a
	 * WebDAV element. The node may be <code>null</code> in which case
	 * <code>false</code> is returned.
	 *
	 * @param node a node, or <code>null</code>
	 * @return     a boolean indicating whether or not the given node is a
	 *             WebDAV element
	 */
	public static boolean isDAVElement(Node node) {
		if (node == null || node.getNodeType() != Node.ELEMENT_NODE)
			return false;
		try {
			if (!DAV_NS.equals(getNSName((Element) node)))
				return false;
		} catch (MalformedElementException e) {
			return false;
		}
		return true;
	}

	/**
	 * Returns a boolean indicating whether or not the given node is a
	 * WebDAV element with the given name. The node may be <code>null</code>
	 * in which case <code>false</code> is returned. The name must not be
	 * <code>null</code>.
	 *
	 * @param node a node, or <code>null</code>
	 * @return     a boolean indicating whether or not the given node is a
	 *             WebDAV element with the given name
	 */
	public static boolean isDAVElement(Node node, String name) {
		Assert.isNotNull(name);
		if (node == null || node.getNodeType() != Node.ELEMENT_NODE)
			return false;
		try {
			Element element = (Element) node;
			if (!name.equals(getNSLocalName(element)) || !DAV_NS.equals(getNSName(element))) {
				return false;
			}
		} catch (MalformedElementException e) {
			return false;
		}
		return true;
	}

	/**
	 * Returns a boolean indicating whether or not the given node is an
	 * element. The node may be <code>null</code> in which case
	 * <code>false</code> is returned.
	 *
	 * @param node a node, or <code>null</code>
	 * @return     a boolean indicating whether or not the given node is an
	 *             element
	 */
	public static boolean isElement(Node node) {
		if (node == null || node.getNodeType() != Node.ELEMENT_NODE)
			return false;
		return true;
	}

	/**
	 * Returns a boolean indicating whether or not the given node is a text
	 * node. The node may be <code>null</code> in which case
	 * <code>false</code> is returned.
	 *
	 * @param node a node, or <code>null</code>
	 * @return     a boolean indicating whether or not the given node is a
	 *             text node
	 */
	public static boolean isText(Node node) {
		return (node != null) && (node.getNodeType() == Node.TEXT_NODE);
	}

	public static void makeNSStandalone(Element element) {
		Assert.isTrue(false, Policy.bind("assert.notImplemented")); //$NON-NLS-1$
	}

	/**
	 * Removes redundant namespace declarations from this element and all
	 * its children to maximum depth. The element must not be
	 * <code>null</code>.
	 *
	 * @param element the element whose namespace declarations are to be
	 *                reduced
	 * @return        the element, or a new element with an equivalent
	 *                qualified name but whose tag name has changed
	 * @throws        MalformedElementException if the name of the given
	 *                element, or one of its attributes, has a namespace
	 *                prefix which could not be resolved
	 */
	public static Element reduceNS(Element element) throws MalformedElementException {
		return (Element) reduceNS(element, null);
	}

	/**
	 * Removes redundant namespace declarations from the given node and all
	 * its children to maximum depth. The node must not be
	 * <code>null</code>.
	 *
	 * @param node the node whose namespace declarations are to be reduced
	 * @return     the node, or a new node (element) with an equivalent
	 *             qualified name but whose tag name has changed
	 * @throws     MalformedElementException if the name of the given node
	 *             (element), or one of its attributes, has a namespace
	 *             prefix which could not be resolved
	 */
	public static Node reduceNS(Node node, Namespaces parentNamespaces) throws MalformedElementException {
		Namespaces namespaces = parentNamespaces;

		if (node.getNodeType() == Node.ELEMENT_NODE) {
			Element element = (Element) node;
			namespaces = getNamespaces(element, parentNamespaces, false);

			String nsPrefix = getNSPrefix(element);
			String nsLocalName = getNSLocalName(element);

			if (nsPrefix != null) {
				String nsName = namespaces.getNSName(nsPrefix);
				ensureNotNull(Policy.bind("ensure.missingNamespaceForPrefix", nsPrefix), nsName); //$NON-NLS-1$

				String tagName = null;
				if (nsName.equals(namespaces.getDefaultNSName())) {
					tagName = nsLocalName;
				} else {
					tagName = namespaces.getNSPrefix(nsName) + ":" + nsLocalName; //$NON-NLS-1$
				}

				if (!tagName.equals(element.getTagName())) {
					Document document = element.getOwnerDocument();
					Element newElement = document.createElement(tagName);
					NamedNodeMap nodeMap = element.getAttributes();
					for (int i = 0; i < nodeMap.getLength(); ++i) {
						Attr attr = (Attr) nodeMap.item(i);
						newElement.setAttribute(attr.getName(), attr.getValue());
					}
					Node child = element.getFirstChild();
					while (child != null) {
						element.removeChild(child);
						newElement.appendChild(child);
						child = element.getFirstChild();
					}
					element.getParentNode().replaceChild(newElement, element);
					element = newElement;
				}
			}

			Vector oldAttributes = new Vector();
			Vector newAttributes = new Vector();
			NamedNodeMap nodeMap = element.getAttributes();
			for (int i = 0; i < nodeMap.getLength(); ++i) {
				Attr attr = (Attr) nodeMap.item(i);
				String name = attr.getName();
				String value = attr.getValue();
				String newName = name;
				nsPrefix = getNSPrefix(name);
				nsLocalName = getNSLocalName(name);

				if (nsPrefix != null && !nsPrefix.equals(XML_PREFIX)) {
					String nsName = namespaces.getNSName(nsPrefix);
					ensureNotNull(Policy.bind("ensure.missingNamespaceForPrefix", nsPrefix), nsName); //$NON-NLS-1$
					String newNSPrefix = namespaces.getNSPrefix(nsName);
					if (!newNSPrefix.equals(nsPrefix)) {
						newName = newNSPrefix + ":" + nsLocalName; //$NON-NLS-1$
					}
				}

				boolean newAttribute = true;
				if (parentNamespaces != null) {
					if (nsPrefix == null && XML_PREFIX.equals(nsLocalName)) {
						if (value.equals(parentNamespaces.getDefaultNSName())) {
							newAttribute = false;
						}
					}
					if (nsPrefix != null && XML_PREFIX.equals(nsPrefix)) {
						if (parentNamespaces.getNSPrefix(value) != null) {
							newAttribute = false;
						}
					}
				}

				oldAttributes.addElement(attr);
				if (newAttribute) {
					newAttributes.addElement(new String[] {newName, value});
				}
			}

			Enumeration oldAttrs = oldAttributes.elements();
			while (oldAttrs.hasMoreElements()) {
				element.removeAttributeNode((Attr) oldAttrs.nextElement());
			}

			Enumeration newAttrs = newAttributes.elements();
			while (newAttrs.hasMoreElements()) {
				String[] newAttr = (String[]) newAttrs.nextElement();
				element.setAttribute(newAttr[0], newAttr[1]);
			}

			node = element;
		}

		Node child = node.getFirstChild();
		while (child != null) {
			child = reduceNS(child, namespaces);
			child = child.getNextSibling();
		}

		return node;
	}

	/**
	 * Resolves the given namespace prefix in the namespace of the given
	 * element. If the given prefix is <code>null</code>, the default
	 * namespace is resolved. Returns the URL of the namespace, or
	 * <code>null</code> if the prefix could not be resolved.
	 *
	 * @param prefix  the namespace prefix to be resolved, or
	 *                <code>null<code> for the default namespace
	 * @param element the element supplying the namespace
	 * @return        the URL of the namespace, or <code>null</code> if the
	 *                prefix could not be resolved
	 */
	public static String resolve(String prefix, Element element) {
		Assert.isNotNull(element);

		/* The prefix xml is by definition bound to the namespace name
		 * <code>XML_NS_NAME</code>.
		 */
		if (XML_NS_PREFIX.equals(prefix)) {
			return XML_NS_NAME;
		}

		/* Search from given element up parent chain to root (document)
		 * looking for a XML namespace declaration (represented as
		 * an element attribute with a name beginning in 
		 * XML_PREFIX ("xmlns")).
		 */
		Node current = element;
		do {
			NamedNodeMap attrs = current.getAttributes();
			int n = attrs.getLength();
			for (int i = 0; i < n; i++) {
				Attr attr = (Attr) attrs.item(i);
				String name = attr.getName();
				if (name.startsWith(XML_PREFIX)) {
					if (name.length() == XML_PREFIX.length()) {
						// no prefix e.g., xmlns="foo:"
						if (prefix == null) {
							String nsName = attr.getValue();
							if (nsName.equals("")) { //$NON-NLS-1$
								return null;
							}
							return nsName;
						}
					} else {
						if (prefix != null && name.equals(XML_PREFIX + ":" + prefix)) { //$NON-NLS-1$
							return attr.getValue();
						}
					}
				}
			}
			do {
				current = current.getParentNode();
			} while (current != null && current.getNodeType() != Node.ELEMENT_NODE);
		} while (current != null);
		return null;
	}

	/**
	 * <p>Creates a WebDAV element with the given name and sets it as a
	 * child of the given parent. Returns the child element.
	 * <p>Children are positioned in the order specified by the given names.
	 * If a child with the same name as the child already exist, the child
	 * is replaced. If firstToLast is true, the search for the child's
	 * position starts at the parent's first child, otherwise, the search
	 * starts at the parent's last child.
	 * <p>The parent must not be <code>null</code> and must be a WebDAV
	 * element.  The child's name must not be <code>null</code>.  The
	 * parent's valid child names must not be <code>null</code>, and must
	 * contain the name of the child.
	 *
	 * @param parent      the parent to which the child is added
	 * @param name        the name of the child which is created and added
	 *                    to the parent
	 * @param names       the ordered collection of valid child names for
	 *                    the parent
	 * @param firstToLast a boolean specifying the direction to search for
	 *                    the child's position among the parent's children
	 * @return            the child element that is created
	 */
	public static Element setChild(Element parent, String name, String[] names, boolean firstToLast) {
		Assert.isTrue(isDAVElement(parent));
		Assert.isNotNull(name);
		Assert.isNotNull(names);
		String nsPrefix = getNSPrefix(parent);
		String tagName = nsPrefix == null ? name : nsPrefix + ":" + name; //$NON-NLS-1$
		Element child = parent.getOwnerDocument().createElement(tagName);
		setChild(parent, child, names, firstToLast);
		return child;
	}

	/**
	 * <p>Creates a WebDAV element with the given name and sets it as a
	 * child of the given parent. In addition, a text node created from the
	 * given data is created and appended to the child.  Returns the child
	 * element.
	 * <p>Children are positioned in the order specified by the given names.
	 * If a child with the same name already exists, it is replaced. If
	 * firstToLast is true, the search for the child's position starts at the
	 * parent's first child, otherwise, the search starts at the parent's
	 * last child.
	 * <p>The parent must not be <code>null</code> and must be a WebDAV
	 * element.  The child's name and data must not be <code>null</code>.
	 * The parent's valid child names must not be <code>null</code>, and
	 * must contain the name of the child.
	 *
	 * @param parent      the parent to which the child is added
	 * @param name        the name of the child which is created and added
	 *                    to the parent
	 * @param data        the data of the text node which is created and
	 *                    added to the child
	 * @param names       the ordered collection of valid child names for
	 *                    the parent
	 * @param firstToLast a boolean specifying the direction to search for
	 *                    the child's position among the parent's children
	 * @return            the child element that is created
	 */
	public static Element setChild(Element parent, String name, String data, String[] names, boolean firstToLast) {
		Assert.isTrue(isDAVElement(parent));
		Assert.isNotNull(name);
		Assert.isNotNull(data);
		Assert.isNotNull(names);
		Element child = setChild(parent, name, names, firstToLast);
		child.appendChild(parent.getOwnerDocument().createTextNode(data));
		return child;
	}

	/**
	 * <p>Sets the given child element as a child of the given parent.
	 * <p>Children are positioned in the order specified by the given names.
	 * If a child with the same name already exists, it is replaced. If
	 * firstToLast is true, the search for the child's position starts at
	 * the parent's first child, otherwise, the search starts at the
	 * parent's last child.
	 * <p>The parent must not be <code>null</code> and must be a WebDAV
	 * element. The child must not be null and its namespace prefix must
	 * resolve to the WebDAV namespace URL in the parent. The parent's valid
	 * child names must not be <code>null</code>, and must contain the name
	 * of the child.
	 *
	 * @param parent      the parent to which the child is added
	 * @param child       the child which is added to the parent
	 * @param names       the ordered collection of valid child names for
	 *                    the parent
	 * @param firstToLast a boolean specifying the direction to search for
	 *                    the child's position among the parent's children
	 */
	public static void setChild(Element parent, Element child, String[] names, boolean firstToLast) {
		Assert.isTrue(isDAVElement(parent));
		Assert.isNotNull(child);
		Assert.isTrue(DAV_NS.equals(resolve(getNSPrefix(child), parent)));
		Assert.isNotNull(names);

		boolean found = false;
		String name = getNSLocalName(child);
		for (int i = 0; !found && i < names.length; ++i) {
			found = names[i].equals(name);
		}
		Assert.isTrue(found);

		Node sibling = getChild(parent, name, names, firstToLast);

		if (isDAVElement(sibling, name)) {
			parent.replaceChild(child, sibling);
		} else if (firstToLast) {
			if (sibling == null) {
				parent.appendChild(child);
			} else {
				parent.insertBefore(child, sibling);
			}
		} else {
			Node refChild = null;
			if (sibling == null) {
				refChild = parent.getFirstChild();
			} else {
				refChild = sibling.getNextSibling();
			}
			if (refChild == null) {
				parent.appendChild(child);
			} else {
				parent.insertBefore(child, refChild);
			}
		}
	}
}
