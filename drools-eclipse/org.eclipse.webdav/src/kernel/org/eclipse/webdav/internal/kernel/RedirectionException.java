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
