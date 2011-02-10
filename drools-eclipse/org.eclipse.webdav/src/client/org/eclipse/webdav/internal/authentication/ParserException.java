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
	private static final long serialVersionUID = 510l;

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
