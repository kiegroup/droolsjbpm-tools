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
