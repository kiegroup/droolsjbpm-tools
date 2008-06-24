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

import java.io.IOException;
import java.io.InputStream;
import org.eclipse.webdav.*;
import org.w3c.dom.Document;

/**
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 */
public class LocalDAVClient extends DAVClient {

	private IServer server;

	/**
	 * Creates a new local DAV client from a clone of the given local DAV
	 * client.
	 *
	 * @param localDAVClient the local DAV client to clone
	 */
	public LocalDAVClient(LocalDAVClient localDAVClient) {
		super(localDAVClient);
		server = localDAVClient.server;
	}

	/**
	 * Creates a new local DAV client that talks to the server at the
	 * specified origin. The origin server <code>URL</code> and the server
	 * must not be <code>null</code>.
	 */
	public LocalDAVClient(WebDAVFactory webDAVFactory, IServer server) {
		super(webDAVFactory);
		this.server = server;
	}

	/**
	 * @see IServer#baselineControl(ILocator, IContext, Document)
	 */
	public IResponse baselineControl(ILocator locator, IContext context, Document body) throws IOException {
		return server.baselineControl(locator, newContext(context, locator), body);
	}

	/**
	 * @see IServer#bind(ILocator, ILocator, IContext)
	 */
	public IResponse bind(ILocator source, ILocator destination, IContext context) throws IOException {
		return server.bind(source, destination, newContext(context, source));
	}

	/**
	 * @see IServer#checkin(ILocator, IContext, Document)
	 */
	public IResponse checkin(ILocator locator, IContext context, Document body) throws IOException {
		return server.checkin(locator, newContext(context, locator), body);
	}

	/**
	 * @see IServer#checkout(ILocator, IContext, Document)
	 */
	public IResponse checkout(ILocator locator, IContext context, Document body) throws IOException {
		return server.checkout(locator, newContext(context, locator), body);
	}

	/**
	 * @see Object#clone()
	 */
	protected Object clone() {
		return new LocalDAVClient(this);
	}

	/**
	 * @see IServer#copy(ILocator, ILocator, IContext, Document)
	 */
	public IResponse copy(ILocator source, ILocator destination, IContext context, Document body) throws IOException {
		return server.copy(source, destination, newContext(context, source), body);
	}

	/**
	 * @see IServer#delete(ILocator, IContext)
	 */
	public IResponse delete(ILocator locator, IContext context) throws IOException {
		return server.delete(locator, newContext(context, locator));
	}

	/**
	 * @see IServer#get(ILocator, IContext)
	 */
	public IResponse get(ILocator locator, IContext context) throws IOException {
		return server.get(locator, newContext(context, locator));
	}

	/**
	 * @see IServer#head(ILocator, IContext)
	 */
	public IResponse head(ILocator locator, IContext context) throws IOException {
		return server.head(locator, newContext(context, locator));
	}

	/**
	 * @see IServer#label(ILocator, IContext, Document)
	 */
	public IResponse label(ILocator locator, IContext context, Document body) throws IOException {
		return server.label(locator, newContext(context, locator), body);
	}

	/**
	 * @see IServer#lock(ILocator, IContext, Document)
	 */
	public IResponse lock(ILocator locator, IContext context, Document body) throws IOException {
		return server.lock(locator, newContext(context, locator), body);
	}

	/**
	 * @see IServer#merge(ILocator, IContext, Document)
	 */
	public IResponse merge(ILocator locator, IContext context, Document body) throws IOException {
		return server.merge(locator, newContext(context, locator), body);
	}

	/**
	 * @see IServer#mkactivity(ILocator, IContext, Document)
	 */
	public IResponse mkactivity(ILocator locator, IContext context, Document body) throws IOException {
		return server.mkactivity(locator, newContext(context, locator), body);
	}

	/**
	 * @see IServer#mkcol(ILocator, IContext, Document)
	 */
	public IResponse mkcol(ILocator locator, IContext context, Document element) throws IOException {
		return server.mkcol(locator, newContext(context, locator), element);
	}

	/**
	 * @see IServer#mkworkspace(ILocator, IContext, Document)
	 */
	public IResponse mkworkspace(ILocator locator, IContext context, Document body) throws IOException {
		return server.mkworkspace(locator, newContext(context, locator), body);
	}

	/**
	 * @see IServer#move(ILocator, ILocator, IContext, Document)
	 */
	public IResponse move(ILocator source, ILocator destination, IContext context, Document body) throws IOException {
		return server.move(source, destination, newContext(context, source), body);
	}

	/**
	 * @see IServer#options(ILocator, IContext)
	 */
	public IResponse options(ILocator locator, IContext context) throws IOException {
		return server.options(locator, newContext(context, locator));
	}

	/**
	 * @see IServer#post(ILocator, IContext, InputStream)
	 */
	public IResponse post(ILocator locator, IContext context, InputStream input) throws IOException {
		return server.post(locator, newContext(context, locator), input);
	}

	/**
	 * @see IServer#propfind(ILocator, IContext, Document)
	 */
	public IResponse propfind(ILocator locator, IContext context, Document body) throws IOException {
		return server.propfind(locator, newContext(context, locator), body);
	}

	/**
	 * @see IServer#proppatch(ILocator, IContext, Document)
	 */
	public IResponse proppatch(ILocator locator, IContext context, Document body) throws IOException {
		return server.proppatch(locator, newContext(context, locator), body);
	}

	/**
	 * @see IServer#put(ILocator, IContext, InputStream)
	 */
	public IResponse put(ILocator locator, IContext context, InputStream input) throws IOException {
		return server.put(locator, newContext(context, locator), input);
	}

	/**
	 * @see IServer#report(ILocator, IContext, Document)
	 */
	public IResponse report(ILocator locator, IContext context, Document body) throws IOException {
		return server.report(locator, newContext(context, locator), body);
	}

	/**
	 * @see IServer#trace(ILocator, IContext)
	 */
	public IResponse trace(ILocator locator, IContext context) throws IOException {
		return server.trace(locator, newContext(context, locator));
	}

	/**
	 * @see IServer#uncheckout(ILocator, IContext)
	 */
	public IResponse uncheckout(ILocator locator, IContext context) throws IOException {
		return server.uncheckout(locator, newContext(context, locator));
	}

	/**
	 * @see IServer#unlock(ILocator, IContext)
	 */
	public IResponse unlock(ILocator locator, IContext context) throws IOException {
		return server.unlock(locator, newContext(context, locator));
	}

	/**
	 * @see IServer#update(ILocator, IContext, Document)
	 */
	public IResponse update(ILocator locator, IContext context, Document body) throws IOException {
		return server.update(locator, newContext(context, locator), body);
	}

	/**
	 * @see IServer#versionControl(ILocator, IContext, Document)
	 */
	public IResponse versionControl(ILocator locator, IContext context, Document body) throws IOException {
		return server.versionControl(locator, newContext(context, locator), body);
	}
}
