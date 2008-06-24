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

public class SystemException extends DAVException {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;
	protected Exception wrappedException;

	/**
	 * SystemException default constructor.
	 */
	public SystemException() {
		super();
	}

	public SystemException(Exception e) {
		super(e.getMessage());
		wrappedException = e;
	}

	public SystemException(String s) {
		super(s);
	}

	public Exception getWrappedException() {
		return wrappedException;
	}

	public void setWrappedException(Exception e) {
		wrappedException = e;
	}
}
