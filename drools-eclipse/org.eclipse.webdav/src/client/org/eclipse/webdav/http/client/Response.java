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
import org.eclipse.webdav.IContext;
import org.eclipse.webdav.IResponse;
import org.eclipse.webdav.internal.kernel.DocumentMarshaler;
import org.eclipse.webdav.internal.kernel.IDocumentMarshaler;
import org.eclipse.webdav.internal.kernel.utils.Assert;
import org.w3c.dom.Document;

/**
 * An HTTP response message.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public class Response extends Message implements IResponse, IStatusCodes {
	protected int statusCode;
	protected String statusMessage;
	protected boolean hasDocumentBody;
	protected Document document;

	/**
	 * Creates a response.
	 *
	 * @param context the response's header, or <code>null</code> for an
	 * empty header
	 * @param inputStream an input stream containing the response's body, or
	 * <code>null</code> for an empty body
	 * @param statusCode the response's status code
	 * @param statusMessage the response's status message
	 */
	public Response(int statusCode, String statusMessage, IContext context, InputStream inputStream) {
		super(context, inputStream);

		Assert.isTrue(statusCode >= 100 && statusCode < 600);
		Assert.isNotNull(statusMessage);

		this.statusCode = statusCode;
		this.statusMessage = statusMessage;
		this.hasDocumentBody = false;

		// Now determine if the response has a document (XML) body
		// or is raw bytes.
		ContentType contentType = getContentType();
		if (contentType != null) {
			hasDocumentBody = "xml".equals(contentType.getSubtype()); //$NON-NLS-1$
		}
	}

	/**
	 * Returns the content length of this message's body, or -1 if the
	 * content length is unknown.
	 *
	 * @return the content length of this message's body
	 */
	public long getContentLength() {

		// Get the declared content length.
		long contentLength = context.getContentLength();

		// If it is defined send the answer.
		if (contentLength != -1)
			return contentLength;

		// Certain messages are defined as having zero length
		// message bodies.
		int statusCode = getStatusCode();
		if (statusCode == IResponse.SC_NO_CONTENT || statusCode == IResponse.SC_NOT_MODIFIED || statusCode >= 100 && statusCode < 200)
			return 0;

		// We don't know how long the body is.
		return -1;
	}

	/**
	 * Returns the content type of this response's body, or <code>null</code>
	 * if the content type is unknown.
	 *
	 * @return the content type of this response's body
	 */
	public ContentType getContentType() {
		String contentTypeString = context.getContentType();
		if (contentTypeString == null)
			return null;
		ContentType contentType = null;
		try {
			contentType = new ContentType(contentTypeString);
		} catch (IllegalArgumentException e) {
			// ignore or log?
		}
		return contentType;
	}

	/**
	 * Returns this response's body as a DOM <code>Document</code>. This
	 * response must have a document body.
	 *
	 * @exception IOException if there is an I/O error
	 */
	public Document getDocumentBody() throws IOException {
		Assert.isTrue(hasDocumentBody);
		Assert.isTrue(!hasInputStream);
		// Lazily parse the message body.
		if (document == null) {
			String characterEncoding = null;
			ContentType contentType = getContentType();
			if (contentType != null) {
				characterEncoding = contentType.getValue("charset"); //$NON-NLS-1$
			}
			if (characterEncoding == null) {
				characterEncoding = "ASCII"; //$NON-NLS-1$
			}
			IDocumentMarshaler marshaler = new DocumentMarshaler();
			document = marshaler.parse(new InputStreamReader(is, characterEncoding));
		}
		return document;
	}

	/**
	 * Returns this response's status code.
	 *
	 * @return this response's status code
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * Returns this response's status message.
	 *
	 * @return this response's status message
	 */
	public String getStatusMessage() {
		return statusMessage;
	}

	/**
	 * Returns a boolean indicating whether this response has a body that can
	 * be marshaled to become a DOM <code>Document</code>.
	 *
	 * @return a boolean indicating whether this response has a body that can
	 * be marshaled to become a DOM <code>Document</code>
	 * @see Document
	 */
	public boolean hasDocumentBody() {
		return hasDocumentBody;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(statusCode);
		buffer.append(' ');
		buffer.append(statusMessage);
		buffer.append('\n');
		buffer.append(super.toString());
		return buffer.toString();
	}
}
