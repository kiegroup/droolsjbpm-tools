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

import java.io.*;
import org.eclipse.webdav.IContext;
import org.eclipse.webdav.IResponse;
import org.eclipse.webdav.internal.kernel.utils.Assert;
import org.w3c.dom.Document;

/**
 * The <code>Response</code> class subclasses <code>Message</code> to
 * add a status field.
 */
public class Response extends Message implements IResponse {

	protected Status status;

	public Response(Status status, IContext context, InputStream body) {
		super();
		this.status = status;
		this.context = context;
		this.body = body;
	}

	public Response(Status status, IContext context, Document body) {
		super();
		this.status = status;
		this.context = context;
		this.body = body;
	}

	public void close() throws IOException {
		if (!hasDocumentBody())
			getInputStream().close();
	}

	public Document getDocumentBody() throws IOException {
		Assert.isTrue(hasDocumentBody(), Policy.bind("error.receiverMissingBody")); //$NON-NLS-1$
		return (Document) body;
	}

	public InputStream getInputStream() {
		Assert.isTrue(!hasDocumentBody(), Policy.bind("error.receiverHasBody")); //$NON-NLS-1$
		return (InputStream) body;
	}

	/**
	 * Return the status code for this response.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Response</code>.</p>
	 *
	 * @return the status code
	 * @see Response#getStatusCode()
	 */
	public int getStatusCode() {
		return status.getCode();
	}

	/**
	 * Return the status message for this response.
	 *
	 * <p>Implements the corresponding API in the interface
	 * <code>Response</code>.</p>
	 *
	 * @return the status message
	 * @see Response#getStatusMessage()
	 */
	public String getStatusMessage() {
		return status.getMessage();
	}

	public boolean hasDocumentBody() {
		return (body instanceof Document);
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(status.toHTTPString());
		buffer.append('\n');
		buffer.append(context.toString());
		buffer.append('\n');
		try {
			if (hasDocumentBody()) {
				IDocumentMarshaler marshaler = new DocumentMarshaler();
				Writer writer = new StringWriter();
				marshaler.print(getDocumentBody(), writer, "UTF-8"); //$NON-NLS-1$
				buffer.append(writer.toString());
			} else
				buffer.append("<<" //$NON-NLS-1$
						+ Policy.bind("label.bytes", String.valueOf(getInputStream().available())) //$NON-NLS-1$
						+ ">>"); //$NON-NLS-1$
		} catch (IOException e) {
			buffer.append("<<" + Policy.bind("exception.dumping", e.getMessage()) + ">>"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		return buffer.toString();
	}
}
