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
package org.eclipse.webdav.internal.authentication;

/**
 * A <code>ParserException</code> is thrown by the <code>Parser</code>
 * when there is a problem parsing a <code>String</code>.
 *
 * @see Parser
 */
public class ParserException extends Exception {
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new <code>ParserException</code>.
	 */
	public ParserException() {
		super();
	}

	/**
	 * Creates a new <code>ParserException</code> with the given message.
	 *
	 * @param message
	 */
	public ParserException(String message) {
		super(message);
	}
}
