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
import java.util.Vector;

public class ExcludingEnumeration extends EnumerationFilter {

	protected Enumeration e;
	protected Vector excludeList;
	protected Object next;

	public ExcludingEnumeration(Enumeration e, Vector excludeList) {
		super();
		this.e = e;
		this.excludeList = excludeList;
		getNextCandidate();
	}

	private void getNextCandidate() {
		while (e.hasMoreElements()) {
			Object candidate = e.nextElement();
			if (excludeList.indexOf(candidate) != -1) {
				next = candidate;
				return;
			}
		}
		next = null;
	}

	/**
	 * @see #hasMoreElements()
	 */
	public boolean hasMoreElements() {
		return (next != null);
	}

	/**
	 * @see #nextElement()
	 */
	public Object nextElement() {
		Object answer = next;
		getNextCandidate();
		return answer;
	}
}
