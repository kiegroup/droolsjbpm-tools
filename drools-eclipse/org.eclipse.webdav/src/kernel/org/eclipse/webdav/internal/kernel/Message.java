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

import org.eclipse.webdav.IContext;

/**
 * The <code>Message</code> class represents a basic message
 * that has a context and a body.
 */
public class Message {

	protected IContext context = new ContextFactory().newContext();

	// The message body. Can be either an Element, an InputStream
	protected Object body;

	/**
	 * Default constructor for the class.
	 */
	public Message() {
		super();
	}

	/**
	 * Return the message body.
	 */
	public Object getBody() {
		return body;
	}

	/**
	 * Return the message context.
	 *
	 * @return the message context.
	 * @see Context
	 */
	public IContext getContext() {
		return context;
	}
}
