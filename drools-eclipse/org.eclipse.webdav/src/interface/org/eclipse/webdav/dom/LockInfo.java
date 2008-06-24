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
 * An element editor for the WebDAV lockinfo element. See RFC2518
 * section 12.6 for the element's definition.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 *
 * @see Owner
 */
public class LockInfo extends ElementEditor {
	/**
	 * An ordered collection of the element names of the lockinfo
	 * element's children.
	 */
	public static final String[] childNames = new String[] {"lockscope", "locktype", "owner"}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

	/**
	 * Creates a new editor on the given WebDAV lockinfo element. The
	 * element is assumed to be well formed.
	 *
	 * @param root a lockinfo element
	 * @throws        MalformedElementException if there is reason to
	 *                believe that the element is not well formed
	 */
	public LockInfo(Element root) throws MalformedElementException {
		super(root, "lockinfo"); //$NON-NLS-1$
	}

	/**
	 * Creates a new WebDAV lockinfo element and sets it as the root of the
	 * given document. Returns an editor on the new lockinfo element. The
	 * document must not be <code>null</code>, and must not already have a
	 * root element.
	 *
	 * @param document the document that will become the root of a new
	 *                 lockinfo element
	 * @return         an element editor on a lockinfo element
	 */
	public static LockInfo create(Document document) {
		Assert.isNotNull(document);
		Assert.isTrue(document.getOwnerDocument() == null);
		Element element = create(document, "lockinfo"); //$NON-NLS-1$
		Element locktype = appendChild(element, "locktype"); //$NON-NLS-1$
		appendChild(locktype, "write"); //$NON-NLS-1$
		LockInfo result = null;
		try {
			result = new LockInfo(element);
		} catch (MalformedElementException e) {
			Assert.isTrue(false, Policy.bind("assert.internalError")); //$NON-NLS-1$
		}
		return result;
	}

	/**
	 * Returns this lockinfo's owner, or <code>null</code> if this lockinfo
	 * has no owner.
	 *
	 * @return this lockinfo's owner, or <code>null</code>
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed
	 */
	public Owner getOwner() throws MalformedElementException {
		Element owner = getLastChild(root, "owner"); //$NON-NLS-1$
		if (owner == null)
			return null;
		return new Owner(owner);
	}

	/**
	 * Returns <code>true</code> if this lockinfo is shared and
	 * <code>false</code> if it is exclusive.
	 *
	 * @return a boolean indicating whether this lockinfo is shared or
	 *         exclusive
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed
	 */
	public boolean isShared() throws MalformedElementException {
		Element lockscope = getFirstChild(root, "lockscope"); //$NON-NLS-1$
		ensureNotNull(Policy.bind("ensure.missingLockscopeElmt"), lockscope); //$NON-NLS-1$
		String[] names = new String[] {"shared", "exclusive"}; //$NON-NLS-1$ //$NON-NLS-2$
		Element sharedOrExclusive = getFirstChild(lockscope, names);
		ensureNotNull(Policy.bind("ensure.missingSharedOrExclusiveElmt"), sharedOrExclusive); //$NON-NLS-1$
		boolean isShared = isDAVElement(sharedOrExclusive, "shared"); //$NON-NLS-1$
		ensure(getNextSibling(sharedOrExclusive, names) == null, Policy.bind("ensure.conflictingSharedOrExclusiveElmt")); //$NON-NLS-1$
		return isShared;
	}

	/**
	 * Sets whether this lockinfo is shared or exclusive. If isShared is
	 * <code>true</code>, the lockinfo is set as shared, otherwise, the
	 * lockinfo is set as exclusive.
	 *
	 * @param isShared a boolean indicating whether this lockinfo will be
	 *                 set to be shared or exclusive
	 */
	public void setIsShared(boolean isShared) {
		Element lockscope = setChild(root, "lockscope", childNames, true); //$NON-NLS-1$
		if (isShared)
			appendChild(lockscope, "shared"); //$NON-NLS-1$
		else
			appendChild(lockscope, "exclusive"); //$NON-NLS-1$
	}

	/**
	 * Sets the owner on this lockinfo and returns an editor on it.
	 */
	public Owner setOwner() {
		Element owner = setChild(root, "owner", childNames, false); //$NON-NLS-1$
		Owner result = null;
		try {
			result = new Owner(owner);
		} catch (MalformedElementException e) {
			Assert.isTrue(false, Policy.bind("assert.internalError")); //$NON-NLS-1$
		}
		return result;
	}
}
