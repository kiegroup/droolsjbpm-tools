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

/** Represents exceptions that require further action by the user agent
 * in order to fulfill the request.
 * <p>
 * Status codes:
 * <ul>
 *    <li>300 Multiple Choices</li>
 *    <li>301 Moved Permanently</li>
 *    <li>302 Moved Temporarily</li>
 *    <li>303 See Other</li>
 *    <li>304 Not Modified</li>
 *    <li>305 Use Proxy</li>
 * </ul>
 * </p>
 */
public class RedirectionException extends WebDAVException {
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	/** 
	 * Construct a RedirectionException with a status code and simple message.
	 * 
	 * @param statusCode the WebDAV status code corresponding to the exception
	 * @param statusMessage a message describing the status code in the context of the exception
	 */
	public RedirectionException(int statusCode, String statusMessage) {
		super(statusCode, statusMessage);
	}

	/** 
	 * Construct a RedirectionException with a status code and simple message.
	 *
	 * @param statusCode the WebDAV status code corresponding to the exception
	 * @param statusMessage a message describing the status code in the context of the exception
	 */
	public RedirectionException(int statusCode, String statusMessage, Object data) {
		super(statusCode, statusMessage, data);
	}
}
