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

/** 
 * When thrown, this class signals that the property name
 * string provided was not legal based on the definition
 * provided in the documentation of the PropertyName(String)
 * constructor.
 */
public class InvalidPropertyNameException extends Exception {
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Construct an InvalidPropertyNameException object.
	 */
	public InvalidPropertyNameException() {
		super("InvalidPropertyName"); //$NON-NLS-1$
	}

	/**
	 * InvalidPropertyNameException constructor comment.
	 * @param statusMessage a message describing the exception of status code
	 */
	public InvalidPropertyNameException(String statusMessage) {
		super(statusMessage);
	}
}
