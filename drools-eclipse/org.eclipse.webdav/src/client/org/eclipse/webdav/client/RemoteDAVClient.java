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
import java.net.URL;
import org.eclipse.webdav.*;
import org.eclipse.webdav.http.client.HttpClient;
import org.eclipse.webdav.http.client.Request;
import org.eclipse.webdav.internal.kernel.utils.Assert;
import org.eclipse.webdav.internal.utils.URLEncoder;
import org.w3c.dom.Document;

/**
 * The <code>ServerProxy</code> class implements the <code>IServer</code>
 * interface and represents a client's local proxy to a remote server.
 * This object is used to talk with the server.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public class RemoteDAVClient extends DAVClient {

	protected HttpClient httpClient = null;

	/**
	 * Creates a new remote DAV client from a clone of the given remote
	 * DAV client.
	 *
	 * @param davClient the DAV client to clone
	 */
	public RemoteDAVClient(RemoteDAVClient davClient) {
		super(davClient);
	}

	/**
	 * Creates a new remote DAV client.
	 *
	 * @param webDAVFactory
	 */
	public RemoteDAVClient(WebDAVFactory webDAVFactory, HttpClient httpClient) {
		super(webDAVFactory);
		this.httpClient = httpClient;
	}

	/**
	 * @see IServer#baselineControl(ILocator, IContext, Document)
	 */
	public IResponse baselineControl(ILocator locator, IContext userContext, Document document) throws IOException {
		Assert.isNotNull(locator);
		Assert.isNotNull(userContext);
		IContext context = newContext(userContext, locator);
		Request request = newRequest(locator, context, document, "BASELINE-CONTROL"); //$NON-NLS-1$
		return httpClient.invoke(request);
	}

	/**
	 * @see IServer#bind(ILocator, ILocator, IContext)
	 */
	public IResponse bind(ILocator source, ILocator destination, IContext userContext) throws IOException {
		Assert.isNotNull(source);
		Assert.isNotNull(destination);
		Assert.isNotNull(userContext);
		IContext context = newContext(userContext, source);
		context.setDestination(URLEncoder.encode(destination.getResourceURL()));
		Request request = newRequest(source, context, "BIND"); //$NON-NLS-1$
		return httpClient.invoke(request);
	}

	/**
	 * @see IServer#checkin(ILocator, IContext, Document)
	 */
	public IResponse checkin(ILocator locator, IContext userContext, Document document) throws IOException {
		Assert.isNotNull(locator);
		Assert.isNotNull(userContext);
		IContext context = newContext(userContext, locator);
		Request request = newRequest(locator, context, document, "CHECKIN"); //$NON-NLS-1$
		return httpClient.invoke(request);
	}

	/**
	 * @see IServer#checkout(ILocator, IContext, Document)
	 */
	public IResponse checkout(ILocator locator, IContext userContext, Document body) throws IOException {
		Assert.isNotNull(locator);
		Assert.isNotNull(userContext);
		IContext context = newContext(userContext, locator);
		Request request = newRequest(locator, context, body, "CHECKOUT"); //$NON-NLS-1$
		return httpClient.invoke(request);
	}

	/**
	 * @see Object#clone()
	 */
	protected Object clone() {
		return new RemoteDAVClient(this);
	}

	/**
	 * Close down the client for futire API calls.  Once a client has been clsed then callers
	 * should not expect further API cals to be sucessful.
	 */
	public void close() {
		httpClient.close();
		super.close();
	}

	/**
	 * @see IServer#copy(ILocator, ILocator, IContext, Document)
	 */
	public IResponse copy(ILocator source, ILocator destination, IContext userContext, Document document) throws IOException {
		Assert.isNotNull(source);
		Assert.isNotNull(destination);
		Assert.isNotNull(userContext);
		IContext context = newContext(userContext, source);
		context.setDestination(URLEncoder.encode(destination.getResourceURL()));
		Request request = newRequest(source, context, document, "COPY"); //$NON-NLS-1$
		return httpClient.invoke(request);
	}

	/**
	 * @see IServer#delete(ILocator, IContext)
	 */
	public IResponse delete(ILocator locator, IContext userContext) throws IOException {
		Assert.isNotNull(locator);
		Assert.isNotNull(userContext);
		IContext context = newContext(userContext, locator);
		Request request = newRequest(locator, context, "DELETE"); //$NON-NLS-1$
		return httpClient.invoke(request);
	}

	/**
	 * @see IServer#get(ILocator, IContext)
	 */
	public IResponse get(ILocator locator, IContext userContext) throws IOException {
		Assert.isNotNull(locator);
		Assert.isNotNull(userContext);
		IContext context = newContext(userContext, locator);
		Request request = newRequest(locator, context, "GET"); //$NON-NLS-1$
		return httpClient.invoke(request);
	}

	/**
	 * Returns this DAV clients HTTP client.
	 */
	public HttpClient getHttpClient() {
		return httpClient;
	}

	/**
	 * @see IServer#head(ILocator, IContext)
	 */
	public IResponse head(ILocator locator, IContext userContext) throws IOException {
		Assert.isNotNull(locator);
		Assert.isNotNull(userContext);
		IContext context = newContext(userContext, locator);
		Request request = newRequest(locator, context, "HEAD"); //$NON-NLS-1$
		return httpClient.invoke(request);
	}

	/**
	 * @see IServer#label(ILocator, IContext, Document)
	 */
	public IResponse label(ILocator locator, IContext userContext, Document document) throws IOException {
		Assert.isNotNull(locator);
		Assert.isNotNull(userContext);
		Assert.isNotNull(document);
		IContext context = newContext(userContext, locator);
		Request request = newRequest(locator, context, document, "LABEL"); //$NON-NLS-1$
		return httpClient.invoke(request);
	}

	/**
	 * @see IServer#lock(ILocator, IContext, Document)
	 */
	public IResponse lock(ILocator locator, IContext userContext, Document document) throws IOException {
		Assert.isNotNull(locator);
		Assert.isNotNull(userContext);
		IContext context = newContext(userContext, locator);
		Request request = newRequest(locator, context, document, "LOCK"); //$NON-NLS-1$
		return httpClient.invoke(request);
	}

	/**
	 * @see IServer#merge(ILocator, IContext, Document)
	 */
	public IResponse merge(ILocator locator, IContext userContext, Document document) throws IOException {
		Assert.isNotNull(locator);
		Assert.isNotNull(userContext);
		Assert.isNotNull(document);
		IContext context = newContext(userContext, locator);
		Request request = newRequest(locator, context, document, "MERGE"); //$NON-NLS-1$
		return httpClient.invoke(request);
	}

	/**
	 * @see IServer#mkactivity(ILocator, IContext, Document)
	 */
	public IResponse mkactivity(ILocator locator, IContext userContext, Document document) throws IOException {
		Assert.isNotNull(locator);
		Assert.isNotNull(userContext);
		IContext context = newContext(userContext, locator);
		Request request = newRequest(locator, context, document, "MKACTIVITY"); //$NON-NLS-1$
		return httpClient.invoke(request);
	}

	/**
	 * @see IServer#mkcol(ILocator, IContext, Document)
	 */
	public IResponse mkcol(ILocator locator, IContext userContext, Document document) throws IOException {
		Assert.isNotNull(locator);
		Assert.isNotNull(userContext);
		IContext context = newContext(userContext, locator);
		Request request = newRequest(locator, context, document, "MKCOL"); //$NON-NLS-1$
		return httpClient.invoke(request);
	}

	/**
	 * @see IServer#mkworkspace(ILocator, IContext, Document)
	 */
	public IResponse mkworkspace(ILocator locator, IContext userContext, Document document) throws IOException {
		Assert.isNotNull(locator);
		Assert.isNotNull(userContext);
		IContext context = newContext(userContext, locator);
		Request request = newRequest(locator, context, document, "MKWORKSPACE"); //$NON-NLS-1$
		return httpClient.invoke(request);
	}

	/**
	 * @see IServer#move(ILocator, ILocator, IContext, Document)
	 */
	public IResponse move(ILocator source, ILocator destination, IContext userContext, Document document) throws IOException {
		Assert.isNotNull(source);
		Assert.isNotNull(destination);
		Assert.isNotNull(userContext);
		IContext context = newContext(userContext, source);
		context.setDestination(URLEncoder.encode(destination.getResourceURL()));
		Request request = newRequest(source, context, document, "MOVE"); //$NON-NLS-1$
		return httpClient.invoke(request);
	}

	private Request newRequest(ILocator locator, IContext context, InputStream is, String methodName) throws IOException {
		return new Request(methodName, URLEncoder.encode(new URL(locator.getResourceURL())), context, is);
	}

	private Request newRequest(ILocator locator, IContext context, String methodName) throws IOException {
		return new Request(methodName, URLEncoder.encode(new URL(locator.getResourceURL())), context);
	}

	private Request newRequest(ILocator locator, IContext context, Document document, String methodName) throws IOException {
		context.setContentType("text/xml; charset=\"UTF8\""); //$NON-NLS-1$
		if (document == null)
			return new Request(methodName, URLEncoder.encode(new URL(locator.getResourceURL())), context);
		RequestBodyWriter writer = new RequestBodyWriter(document, "UTF8"); //$NON-NLS-1$
		return new Request(methodName, URLEncoder.encode(new URL(locator.getResourceURL())), context, writer);
	}

	/**
	 * @see IServer#options(ILocator, IContext)
	 */
	public IResponse options(ILocator locator, IContext userContext) throws IOException {
		Assert.isNotNull(locator);
		Assert.isNotNull(userContext);
		IContext context = newContext(userContext, locator);
		Request request = newRequest(locator, context, "OPTIONS"); //$NON-NLS-1$
		return httpClient.invoke(request);
	}

	/**
	 * @see IServer#post(ILocator, IContext, InputStream)
	 */
	public IResponse post(ILocator locator, IContext userContext, InputStream is) throws IOException {
		Assert.isNotNull(locator);
		Assert.isNotNull(userContext);
		Assert.isNotNull(is);
		IContext context = newContext(userContext, locator);
		Request request = newRequest(locator, context, is, "POST"); //$NON-NLS-1$
		return httpClient.invoke(request);
	}

	/**
	 * @see IServer#propfind(ILocator, IContext, Document)
	 */
	public IResponse propfind(ILocator locator, IContext userContext, Document document) throws IOException {
		Assert.isNotNull(locator);
		Assert.isNotNull(userContext);
		IContext context = newContext(userContext, locator);
		Request request = newRequest(locator, context, document, "PROPFIND"); //$NON-NLS-1$
		return httpClient.invoke(request);
	}

	/**
	 * @see IServer#proppatch(ILocator, IContext, Document)
	 */
	public IResponse proppatch(ILocator locator, IContext userContext, Document document) throws IOException {
		Assert.isNotNull(locator);
		Assert.isNotNull(userContext);
		Assert.isNotNull(document);
		IContext context = newContext(userContext, locator);
		Request request = newRequest(locator, context, document, "PROPPATCH"); //$NON-NLS-1$
		return httpClient.invoke(request);
	}

	/**
	 * @see IServer#put(ILocator, IContext, InputStream)
	 */
	public IResponse put(ILocator locator, IContext userContext, InputStream is) throws IOException {
		Assert.isNotNull(locator);
		Assert.isNotNull(userContext);
		Assert.isNotNull(is);
		IContext context = newContext(userContext, locator);
		Request request = newRequest(locator, context, is, "PUT"); //$NON-NLS-1$
		return httpClient.invoke(request);
	}

	/**
	 * @see IServer#report(ILocator, IContext, Document)
	 */
	public IResponse report(ILocator locator, IContext userContext, Document document) throws IOException {
		Assert.isNotNull(locator);
		Assert.isNotNull(userContext);
		Assert.isNotNull(document);
		IContext context = newContext(userContext, locator);
		Request request = newRequest(locator, context, document, "REPORT"); //$NON-NLS-1$
		return httpClient.invoke(request);
	}

	/**
	 * @see IServer#trace(ILocator, IContext)
	 */
	public IResponse trace(ILocator locator, IContext userContext) throws IOException {
		Assert.isNotNull(locator);
		Assert.isNotNull(userContext);
		IContext context = newContext(userContext, locator);
		Request request = newRequest(locator, context, "TRACE"); //$NON-NLS-1$
		return httpClient.invoke(request);
	}

	/**
	 * @see IServer#uncheckout(ILocator, IContext)
	 */
	public IResponse uncheckout(ILocator locator, IContext userContext) throws IOException {
		Assert.isNotNull(locator);
		Assert.isNotNull(userContext);
		IContext context = newContext(userContext, locator);
		Request request = newRequest(locator, context, "UNCHECKOUT"); //$NON-NLS-1$
		return httpClient.invoke(request);
	}

	/**
	 * @see IServer#unlock(ILocator, IContext)
	 */
	public IResponse unlock(ILocator locator, IContext userContext) throws IOException {
		Assert.isNotNull(locator);
		Assert.isNotNull(userContext);
		IContext context = newContext(userContext, locator);
		Request request = newRequest(locator, context, "UNLOCK"); //$NON-NLS-1$
		return httpClient.invoke(request);
	}

	/**
	 * @see IServer#update(ILocator, IContext, Document)
	 */
	public IResponse update(ILocator locator, IContext userContext, Document body) throws IOException {
		Assert.isNotNull(locator);
		Assert.isNotNull(userContext);
		IContext context = newContext(userContext, locator);
		Request request = newRequest(locator, context, body, "UPDATE"); //$NON-NLS-1$
		return httpClient.invoke(request);
	}

	/**
	 * @see IServer#versionControl(ILocator, IContext, Document)
	 */
	public IResponse versionControl(ILocator locator, IContext userContext, Document body) throws IOException {
		Assert.isNotNull(locator);
		Assert.isNotNull(userContext);
		IContext context = newContext(userContext, locator);
		Request request = newRequest(locator, context, body, "VERSION-CONTROL"); //$NON-NLS-1$
		return httpClient.invoke(request);
	}
}
