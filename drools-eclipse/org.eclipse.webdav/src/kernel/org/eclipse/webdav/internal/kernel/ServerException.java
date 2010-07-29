/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
	private static final long serialVersionUID = 510l;

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
