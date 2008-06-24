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
package org.eclipse.webdav.http.client;

import java.io.*;
import java.net.URL;
import org.eclipse.webdav.IContext;
import org.eclipse.webdav.internal.kernel.utils.Assert;

/**
 * An HTTP request message.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public class Request extends Message {
	private String method;
	private URL resourceUrl;
	private IRequestBodyWriter requestBodyWriter;

	/**
	 * Creates a request.
	 *
	 * @param method the type of request, for example: "PUT"
	 * @param resourceUrl the URL of the target resource
	 * @param context the request's header, or <code>null</code> for an empty
	 * header
	 */
	public Request(String method, URL resourceUrl, IContext context) {
		super(context, new RequestInputStream(new byte[0]));
		Assert.isNotNull(method);
		Assert.isNotNull(resourceUrl);
		this.method = method;
		this.resourceUrl = resourceUrl;
	}

	/**
	 * Creates a request. For efficiency, the given input stream should be a
	 * <code>RequestInputStream</code> or a <code>ByteArrayInputStream</code>.
	 *
	 * @param method the type of request, for example: "PUT"
	 * @param resourceUrl the URL of the target resource
	 * @param context the request's header, or <code>null</code> for an empty
	 * header
	 * @param is an input stream containing the message's body, or
	 * <code>null</code> for an empty body
	 * @exception IOException if there is an I/O error
	 * @see RequestInputStream
	 */
	public Request(String method, URL resourceUrl, IContext context, InputStream is) throws IOException {
		super(context, is == null ? new RequestInputStream(new byte[0]) : is instanceof RequestInputStream ? is : is instanceof ByteArrayInputStream ? new RequestInputStream((ByteArrayInputStream) is) : new RequestInputStream(is, -1));
		Assert.isNotNull(method);
		Assert.isNotNull(resourceUrl);
		this.method = method;
		this.resourceUrl = resourceUrl;
	}

	/**
	 * Creates a request.
	 *
	 * @param method the type of request, for example: "PUT"
	 * @param resourceUrl the URL of the target resource
	 * @param context the message header, or <code>null</code> for an empty
	 * header
	 * @param requestBodyWriter for obtaining the message's body
	 */
	public Request(String method, URL resourceUrl, IContext context, IRequestBodyWriter requestBodyWriter) {
		super(context, new RequestInputStream(new byte[0]));
		Assert.isNotNull(method);
		Assert.isNotNull(resourceUrl);
		Assert.isNotNull(requestBodyWriter);
		this.method = method;
		this.resourceUrl = resourceUrl;
		this.requestBodyWriter = requestBodyWriter;
	}

	/**
	 * Returns the content length of this message's body, or -1 if the
	 * content length is unknown.
	 *
	 * @return the content length of this message's body
	 */
	public long getContentLength() {
		long contentLength = super.getContentLength();
		if (contentLength != -1)
			return contentLength;
		if (requestBodyWriter == null)
			return ((RequestInputStream) is).length();
		return -1;
	}

	/**
	 * Returns the type of this request, for example: "PUT".
	 *
	 * @return the type of this request, for example: "PUT"
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * Returns this request's request body writer, or <code>null</code> if
	 * this request does not have one.
	 *
	 * @return this request's request body writer, or <code>null</code>
	 */
	public IRequestBodyWriter getRequestBodyWriter() {
		return requestBodyWriter;
	}

	/**
	 * Returns the URL of this request's target resource.
	 *
	 * @return the URL of this request's target resource
	 */
	public URL getResourceUrl() {
		return resourceUrl;
	}

	void setResourceUrl(URL resourceUrl) {
		this.resourceUrl = resourceUrl;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(method);
		buffer.append(' ');
		buffer.append(resourceUrl);
		buffer.append('\n');
		buffer.append(super.toString());
		return buffer.toString();
	}

	/**
	 * Writes this request's body to the given output stream. This method may
	 * be called more than once during the lifetime of this request.
	 *
	 * @param os an output stream
	 * @exception IOException if there is an I/O error
	 */
	public void write(OutputStream os) throws IOException {
		if (requestBodyWriter == null) {
			if (inputRead) {
				is.reset();
				inputRead = false;
			}
			super.write(os);
		} else {
			requestBodyWriter.writeRequestBody(os);
		}
	}
}
