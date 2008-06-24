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
 * This is the superclass of all WebDAV protocol exceptions
 * It contains a status code that provides information, and a
 * descriptive message.
 */
public class WebDAVException extends DAVException {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;
	private int statusCode = 0;
	protected Object data = null;

	/** 
	 * Construct a WebDAVException
	 *
	 * @param statusCode the HTTP/1.1 or WebDAV status code
	 * @param statusMessage a message describing the exception of status code
	 */
	public WebDAVException(int statusCode, String statusMessage) {
		super(statusMessage);
		this.statusCode = statusCode;
	}

	/** 
	 * Construct a WebDAVException
	 *
	 * @param statusCode the HTTP/1.1 or WebDAV status code
	 * @param statusMessage a message describing the exception of status code
	 */
	public WebDAVException(int statusCode, String statusMessage, Object data) {
		super(statusMessage);
		this.statusCode = statusCode;
		this.data = data;
	}

	public Object getData() {
		return data;
	}

	/** 
	 * Get the status code that provides additional information about the
	 * exception. These status codes are defined by the HTTP/1.1 and WebDAV
	 * specifications.
	 *
	 * @return the HTTP/1.1 or WebDAV status code
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/** 
	 * Render this WebDAVException as a string including its status code.
	 *
	 * @return the string includes the status code and message
	 */
	public String toString() {
		return "WebDAVException(" + statusCode + ": " + getMessage() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}
}
