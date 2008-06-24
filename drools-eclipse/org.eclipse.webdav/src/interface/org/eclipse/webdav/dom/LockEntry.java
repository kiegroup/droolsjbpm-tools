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
import org.w3c.dom.Element;

/**
 * An element editor for the WebDAV lockentry element. See RFC2518
 * section 12.5 for the element's definition.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 *
 * @see SupportedLock
 */
public class LockEntry extends ElementEditor {
	/**
	 * An ordered collection of the element names of the lockentry
	 * element's children.
	 */
	protected static final String[] childNames = new String[] {"lockscope", "locktype"}; //$NON-NLS-1$ //$NON-NLS-2$

	/**
	 * Creates a new editor on the given WebDAV lockentry element. The
	 * element is assumed to be well formed.
	 *
	 * @param root a lockentry element
	 * @throws        MalformedElementException if there is reason to
	 *                believe that the element is not well formed
	 */
	public LockEntry(Element root) throws MalformedElementException {
		super(root, "lockentry"); //$NON-NLS-1$
	}

	/**
	 * Returns <code>true</code> if this lockentry is shared and
	 * <code>false</code> if it is exclusive.
	 *
	 * @return a boolean indicating whether this lockentry is shared or
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
	 * Sets whether this lockentry is shared or exclusive. If isShared is
	 * <code>true</code>, the lockentry is set as shared, otherwise, the
	 * lockentry is set as exclusive.
	 *
	 * @param isShared a boolean indicating whether this lockentry will be
	 *                 set to be shared or exclusive
	 */
	public void setIsShared(boolean isShared) {
		Element lockscope = setChild(root, "lockscope", childNames, true); //$NON-NLS-1$
		if (isShared)
			appendChild(lockscope, "shared"); //$NON-NLS-1$
		else
			appendChild(lockscope, "exclusive"); //$NON-NLS-1$
	}
}
