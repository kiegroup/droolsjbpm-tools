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
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * An element editor for the WebDAV lockdiscovery element. See RFC2518
 * section 13.8 for the element's definition.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 *
 * @see ActiveLock
 */
public class LockDiscovery extends Property {
	/**
	 * An ordered collection of the element names of the lockdiscovery
	 * element's children.
	 */
	protected static final String[] childNames = new String[] {"activelock"}; //$NON-NLS-1$

	/**
	 * Creates a new editor on the given WebDAV lockdiscovery element. The
	 * element is assumed to be well formed.
	 *
	 * @param root a lockdiscovery element
	 * @throws        MalformedElementException if there is reason to
	 *                believe that the element is not well formed
	 */
	public LockDiscovery(Element root) throws MalformedElementException {
		super(root, "lockdiscovery"); //$NON-NLS-1$
	}

	/**
	 * Creates and adds a new activelock on this lockdiscovery and returns
	 * an editor on it.
	 *
	 * @return an editor on an activelock element
	 */
	public ActiveLock addActiveLock() {
		Element activelock = addChild(root, "activelock", childNames, false); //$NON-NLS-1$
		Element locktype = appendChild(activelock, "locktype"); //$NON-NLS-1$
		appendChild(locktype, "write"); //$NON-NLS-1$
		ActiveLock result = null;
		try {
			result = new ActiveLock(activelock);
		} catch (MalformedElementException e) {
			Assert.isTrue(false, Policy.bind("assert.internalError")); //$NON-NLS-1$
		}
		return result;
	}

	/**
	 * Returns an <code>Enumeration</code> over this lockdiscovery's
	 * <code>ActiveLock</code>s.
	 *
	 * @return an <code>Enumeration</code> of <code>ActiveLock</code>s
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed
	 */
	public Enumeration getActiveLocks() throws MalformedElementException {
		final Node firstActiveLock = getFirstChild(root, "activelock"); //$NON-NLS-1$
		Enumeration e = new Enumeration() {
			Node currentActiveLock = firstActiveLock;

			public boolean hasMoreElements() {
				return currentActiveLock != null;
			}

			public Object nextElement() {
				if (!hasMoreElements())
					throw new NoSuchElementException();
				ActiveLock result = null;
				try {
					result = new ActiveLock((Element) currentActiveLock);
				} catch (MalformedElementException ex) {
					Assert.isTrue(false, Policy.bind("assert.internalError")); //$NON-NLS-1$
				}
				currentActiveLock = getTwin((Element) currentActiveLock, true);
				return result;
			}
		};
		return e;
	}
}
