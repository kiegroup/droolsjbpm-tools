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
import org.eclipse.webdav.IContext;
import org.eclipse.webdav.Policy;
import org.eclipse.webdav.internal.kernel.Context;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * An element editor for the WebDAV activelock element. See RFC2518
 * section 12.1 for the element's definition.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 *
 * @see LockDiscovery
 * @see Owner
 */
public class ActiveLock extends ElementEditor {

	// An ordered collection of the element names of the activelock
	// element's children.
	protected static final String[] childNames = new String[] {"lockscope", //$NON-NLS-1$
			"locktype", //$NON-NLS-1$
			"depth", //$NON-NLS-1$
			"owner", //$NON-NLS-1$
			"timeout", //$NON-NLS-1$
			"locktoken"}; //$NON-NLS-1$

	/**
	 * Creates a new editor on the given WebDAV activelock element. The
	 * element is assumed to be well formed.
	 *
	 * @param root an activelock element
	 * @throws        MalformedElementException if there is reason to
	 *                believe that the element is not well formed
	 */
	public ActiveLock(Element root) throws MalformedElementException {
		super(root, "activelock"); //$NON-NLS-1$
	}

	/**
	 * Adds the given href to this activelock's locktoken. The href must not
	 * be <code>null</code>.
	 *
	 * @param href the href to add
	 */
	public void addLockTokenHref(String href) {
		Assert.isNotNull(href);
		Element locktoken = getLastChild(root, "locktoken"); //$NON-NLS-1$
		if (locktoken == null)
			locktoken = setChild(root, "locktoken", childNames, false); //$NON-NLS-1$
		appendChild(locktoken, "href", encodeHref(href)); //$NON-NLS-1$
	}

	/**
	 * Returns the depth of this activelock; for example,
	 * <code>Context.DEPTH_ZERO</code>.
	 *
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed
	 * @see    Context
	 */
	public String getDepth() throws MalformedElementException {
		String depth = getChildText(root, "depth", false); //$NON-NLS-1$
		ensureNotNull(Policy.bind("ensure.missingDepthElmt"), depth); //$NON-NLS-1$
		ensure(depth.equals(IContext.DEPTH_ZERO) || depth.equals(IContext.DEPTH_ONE) || depth.equals(IContext.DEPTH_INFINITY), Policy.bind("ensure.invalidDepth", depth)); //$NON-NLS-1$
		return depth;
	}

	/**
	 * Returns an <code>Enumeration</code> of <code>String</code>s
	 * containing this activelock's lock token hrefs.
	 *
	 * @return an <code>Enumeration</code> of <code>String</code>s
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed
	 */
	public Enumeration getLockTokenHrefs() throws MalformedElementException {
		Element locktoken = getLastChild(root, "locktoken"); //$NON-NLS-1$
		Element firstHref = null;
		if (locktoken != null) {
			firstHref = getFirstChild(locktoken, "href"); //$NON-NLS-1$
			ensureNotNull(Policy.bind("ensure.missingHrefElmt"), firstHref); //$NON-NLS-1$
		}
		final Node node = firstHref;
		Enumeration e = new Enumeration() {
			Node currentHref = node;

			public boolean hasMoreElements() {
				return currentHref != null;
			}

			public Object nextElement() {
				if (!hasMoreElements()) {
					throw new NoSuchElementException();
				}
				String href = getFirstText((Element) currentHref);
				currentHref = getTwin((Element) currentHref, true);
				return decodeHref(href);
			}
		};

		return e;
	}

	/**
	 * Returns this activelock's owner, or <code>null</code> if this
	 * active lock has no owner.
	 *
	 * @return this activelock's owner, or <code>null</code>
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
	 * Returns this activelock's timeout, or <code>null</code> if this
	 * active lock has no timeout.
	 *
	 * @return this activelock's timeout, or <code>null</code>
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed
	 */
	public String getTimeout() throws MalformedElementException {
		return getChildText(root, "timeout", false); //$NON-NLS-1$
	}

	/**
	 * Returns <code>true</code> if this activelock is shared, or
	 * <code>false</code> if it is exclusive.
	 *
	 * @return a boolean indicating whether this activelock is shared or
	 *         exclusive
	 * @throws MalformedElementException if there is reason to believe that
	 *         this editor's underlying element is not well formed
	 */
	public boolean isShared() throws MalformedElementException {

		Element lockscope = getFirstChild(root, "lockscope"); //$NON-NLS-1$
		ensureNotNull(Policy.bind("ensure.missingLockscopeElmt "), lockscope); //$NON-NLS-1$

		String[] names = new String[] {"shared", "exclusive"}; //$NON-NLS-1$ //$NON-NLS-2$
		Element sharedOrExclusive = getFirstChild(lockscope, names);
		ensureNotNull(Policy.bind("ensure.missingSharedOrExclusiveElmt"), sharedOrExclusive); //$NON-NLS-1$

		boolean isShared = isDAVElement(sharedOrExclusive, "shared"); //$NON-NLS-1$
		ensure(getNextSibling(sharedOrExclusive, names) == null, Policy.bind("ensure.conflictingSharedOrExclusiveElmt")); //$NON-NLS-1$

		return isShared;
	}

	/**
	 * Sets the depth of this activelock to the given depth. The depth must
	 * not be null and must be one of:
	 * <ul>
	 * <li><code>Context.DEPTH_ZERO</code>
	 * <li><code>Context.DEPTH_ONE</code>
	 * <li><code>Context.DEPTH_INFINITY</code>
	 * </ul>
	 *
	 * @param depth the depth for this activelock
	 * @see         Context
	 */
	public void setDepth(String depth) {
		Assert.isNotNull(depth);
		Assert.isTrue(depth.equals(IContext.DEPTH_ZERO) || depth.equals(IContext.DEPTH_ONE) || depth.equals(IContext.DEPTH_INFINITY));
		setChild(root, "depth", depth, childNames, false); //$NON-NLS-1$
	}

	/**
	 * Sets whether this activelock is shared or exclusive. If isShared is
	 * <code>true</code>, the activelock is set as shared, otherwise, the
	 * activelock is set as exclusive.
	 *
	 * @param isShared a boolean indicating whether this activelock will be
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
	 * Creates and sets an owner element on this activelock and returns an
	 * editor on it.
	 *
	 * @return an editor on an owner element
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

	/**
	 * Sets the timeout on this activelock to the given timeout. If the
	 * timeout is <code>null</code> the current timeout is removed.
	 *
	 * @param timeout the timeout value for this activelock, or
	 *                <code>null</code> for no timeout
	 */
	public void setTimeout(String timeout) {
		if (timeout == null) {
			Element child = getLastChild(root, "timeout"); //$NON-NLS-1$
			if (child != null)
				root.removeChild(child);
		} else
			setChild(root, "timeout", timeout, childNames, false); //$NON-NLS-1$
	}
}
