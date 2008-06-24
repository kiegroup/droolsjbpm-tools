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

import org.eclipse.webdav.dom.QualifiedName;
import org.eclipse.webdav.internal.kernel.utils.Assert;

/**
 * Qualified names are two-part names: qualifier and local name.
 * The qualifier must be in URI form (see RFC2396).  
 * Note however that the qualifier may be <code>null</code> if
 * the default name space is being used.  The empty space is
 * not a valid qualifier.
 */
public class QualifiedNameImpl implements QualifiedName {

	/** Qualifier part (potentially <code>null</code>). */
	protected String qualifier = null;

	/** Local name part. */
	protected String localName = null;

	/**
	 * Creates and returns a new qualified name with the given qualifier
	 * and local name.  The local name must not be the empty string.
	 * The qualifier may be <code>null</code>.   
	 */
	public QualifiedNameImpl(String qualifier, String localName) {
		Assert.isLegal(localName != null && localName.length() != 0);
		this.qualifier = qualifier;
		this.localName = localName;
	}

	/**
	 * Returns <code>true</code> if this qualified name is equivalent to the given object.
	 * <p>
	 * Qualified names are equal if and only if they have the same
	 * qualified parts and local parts.
	 * Qualified names are not equal to objects other than qualified names.
	 *
	 */
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (!(obj instanceof QualifiedName))
			return false;
		// There may or may not be a qualifier.
		QualifiedName qName = (QualifiedName) obj;
		if (qualifier == null && qName.getQualifier() != null)
			return false;
		if (qualifier != null && !qualifier.equals(qName.getQualifier()))
			return false;
		return localName.equals(qName.getLocalName());
	}

	/**
	 * Returns the local part of this name.  
	 */
	public String getLocalName() {
		return localName;
	}

	/**
	 * Returns the qualifier part for this qualifed name, or <code>null</code>
	 * if none.
	 */
	public String getQualifier() {
		return qualifier;
	}

	/**
	 * Returns the hash code for this qualified name.
	 */
	public int hashCode() {
		return (qualifier == null ? 0 : qualifier.hashCode()) + localName.hashCode();
	}

	/**
	 * Converts this qualified name into a string, suitable for 
	 * display (unsuitable for parsing back to a qualified name!).
	 */
	public String toString() {
		return "Namespace: \"" + //$NON-NLS-1$
				(getQualifier() == null ? "null" : getQualifier()) + //$NON-NLS-1$
				"\", Local part: \"" + //$NON-NLS-1$ //$NON-NLS-2$
				getLocalName() + "\""; //$NON-NLS-1$
	}
}
