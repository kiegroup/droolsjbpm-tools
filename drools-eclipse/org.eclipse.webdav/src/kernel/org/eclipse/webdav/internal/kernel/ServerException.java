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

/** Represents exceptions that can happen on the server as the
 * result of a server error.
 * <p>
 * Status codes:
 * <ul>
 *    <li>500 Internal Server Error</li>
 *    <li>501 Not Implemented</li>
 *    <li>502 Bad Gateway</li>
 *    <li>503 Service Unavailable</li>
 *    <li>504 Gateway Timeout</li>
 *    <li>505 HTTP Version Not Supported</li>
 * </ul>
 * </p>
 */
public class ServerException extends WebDAVException {
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	/** 
	 * Construct a ServerException with a status code and simple message.
	 *
	 * @param statusCode the WebDAV status code corresponding to the exception
	 * @param statusMessage a message describing the status code in the context of the exception
	 */
	public ServerException(int statusCode, String statusMessage) {
		super(statusCode, statusMessage);
	}

	/** 
	 * Construct a ServerException with a status code and simple message.
	 * 
	 * @param statusCode the WebDAV status code corresponding to the exception
	 * @param statusMessage a message describing the status code in the context of the exception
	 */
	public ServerException(int statusCode, String statusMessage, Object data) {
		super(statusCode, statusMessage, data);
	}
}
