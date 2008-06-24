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

/** Represents exceptions that can happen on the Client as the
 * result of a client error.
 * <p>
 * Status codes:
 * <ul>
 *    <li>400 Bad Request</li>
 *    <li>401 Unauthorized</li>
 *    <li>402 Payment Required</li>
 *    <li>403 Forbidden</li>
 *    <li>404 Not Found</li>
 *    <li>405 Mothod Not Allowed</li>
 *    <li>406 Not Acceptable</li>
 *    <li>407 Proxy Authentication Required</li>
 *    <li>408 Request Timeout</li>
 *    <li>409 Conflict</li>
 *    <li>410 Gone</li>
 *    <li>411 Length Required</li>
 *    <li>412 Precondition Failed</li>
 *    <li>413 Request Entity Too Large</li>
 *    <li>414 Request-URI Too Long</li>
 *    <li>415 Unsupported Media Type</li>
 *    <li>422 Unprocessable Entity</li>
 *    <li>423 Locked</li>
 *    <li>424 Method Failure</li>
 *    <li>425 Insufficient Space on Resource</li>
 * </ul>
 * </p>
 */
public class ClientException extends WebDAVException {
	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = 1L;

	/** 
	 * Construct a ClientException with a status code and simple message.
	 *
	 * @param statusCode the WebDAV status code corresponding to the exception
	 * @param statusMessage a message describing the status code in the context of the exception
	 */
	public ClientException(int statusCode, String statusMessage) {
		super(statusCode, statusMessage);
	}

	/** 
	 * Construct a ClientException with a status code and simple message.
	 *
	 * @param statusCode the WebDAV status code corresponding to the exception
	 * @param statusMessage a message describing the status code in the context of the exception
	 */
	public ClientException(int statusCode, String statusMessage, Object data) {
		super(statusCode, statusMessage, data);
	}
}
