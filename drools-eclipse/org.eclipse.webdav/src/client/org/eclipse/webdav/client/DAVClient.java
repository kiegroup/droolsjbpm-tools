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
package org.eclipse.webdav.client;

import java.net.MalformedURLException;
import org.eclipse.webdav.*;
import org.eclipse.webdav.internal.kernel.utils.Assert;

/**
 * <code>Dav Client</code>s implement the <code>IServer</code> interface
 * and represent a client's connection to a server.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public abstract class DAVClient implements IServer {

	protected WebDAVFactory davFactory = null;

	/**
	 * Creates a new DAV client from a clone of the given DAV client.
	 *
	 * @param davClient the DAV client to clone
	 */
	public DAVClient(DAVClient davClient) {
		davFactory = davClient.davFactory;
	}

	/**
	 * Creates a new DAV client.
	 *
	 * @param davFactory
	 */
	public DAVClient(WebDAVFactory davFactory) {
		Assert.isNotNull(davFactory);
		this.davFactory = davFactory;
	}

	/**
	 * @see Object#clone()
	 */
	protected abstract Object clone();

	public WebDAVFactory getDAVFactory() {
		return davFactory;
	}

	/**
	 * Returns a new context that is based on the given context.
	 *
	 * @param userContext
	 * @param locator
	 *
	 * @return a new context that is based on the given context
	 */
	protected IContext newContext(IContext userContext, ILocator locator) throws MalformedURLException {
		Assert.isNotNull(userContext);
		Assert.isNotNull(locator);
		IContext context = davFactory.newContext(userContext);
		if (locator.getLabel() != null)
			context.setLabel(locator.getLabel());
		return context;
	}

	/**
	 * Shut down the client for future use, and release any system resources associated with the
	 * instance.  Callers should not expect the client to succeed with further API calls once the
	 * client has been closed.
	 */
	public void close() {
		// Default is to do nothing.
	}
}
