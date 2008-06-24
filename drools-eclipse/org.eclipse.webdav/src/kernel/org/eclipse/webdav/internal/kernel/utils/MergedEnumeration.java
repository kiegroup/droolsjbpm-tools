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
package org.eclipse.webdav.internal.kernel.utils;

import java.util.Enumeration;

public class MergedEnumeration extends EnumerationFilter {

	protected Enumeration first;
	protected Enumeration second;

	/**
	 * MergedEnumeration constructor comment.
	 */
	public MergedEnumeration(Enumeration first, Enumeration second) {
		super();
		this.first = first;
		this.second = second;
	}

	public boolean hasMoreElements() {
		return (first.hasMoreElements() || second.hasMoreElements());
	}

	public Object nextElement() {
		if (first.hasMoreElements())
			return first.nextElement();
		return second.nextElement();
	}
}
