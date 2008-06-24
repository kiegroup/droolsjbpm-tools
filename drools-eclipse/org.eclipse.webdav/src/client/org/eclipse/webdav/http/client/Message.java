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
import org.eclipse.webdav.client.Policy;
import org.eclipse.webdav.client.WebDAVFactory;
import org.eclipse.webdav.internal.kernel.utils.Assert;

/**
 * A common superclass for HTTP messages.  There are two kinds of HTTP
 * message; requests and responses.  They both have in common a context
 * and an input stream.  This class factors out these similarities.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public abstract class Message {
	protected static BufferPool bufferPool = new BufferPool();
	protected InputStream is;
	protected boolean inputRead = false;
	protected boolean hasInputStream = false;
	protected IContext context;

	/**
	 * Creates a message.
	 *
	 * @param context the message header, or <code>null</code> for an empty
	 * header
	 * @param is an input stream containing the message's body, or
	 * <code>null</code> for an empty body
	 */
	public Message(IContext context, InputStream is) {
		this.context = context;
		if (context == null)
			this.context = WebDAVFactory.contextFactory.newContext();
		this.is = is;
		if (is == null)
			this.is = new ByteArrayInputStream(new byte[0]);
	}

	/**
	 * Closes this message to free up any system resources.  All messages
	 * must be closed before finalization.
	 *
	 * @exception IOException if there is an I/O error
	 */
	public void close() throws IOException {
		is.close();
	}

	/**
	 * Returns the content length of this message's body, or -1 if the
	 * content length is unknown.
	 *
	 * @return the content length of this message's body
	 */
	public long getContentLength() {
		long contentLength = context.getContentLength();
		if (contentLength != -1)
			return contentLength;
		if (is instanceof ByteArrayInputStream)
			return ((ByteArrayInputStream) is).available();
		return -1;
	}

	/**
	 * Returns this message's context.
	 *
	 * @return this message's context
	 */
	public IContext getContext() {
		return context;
	}

	/**
	 * Returns this message's input stream.
	 *
	 * @return this message's input stream
	 */
	public InputStream getInputStream() {
		hasInputStream = true;
		return is;
	}

	public String toString() {
		return context.toString();
	}

	/**
	 * Writes this messages body to the given output stream. This method may
	 * only be called once during the lifetime of this message.
	 *
	 * @param os an output stream
	 * @exception IOException if there is an I/O error
	 */
	public void write(OutputStream os) throws IOException {
		Assert.isTrue(!inputRead);
		Assert.isTrue(!hasInputStream);

		int bytesRead = 0;
		int totalBytesRead = 0;
		byte[] buffer = bufferPool.getBuffer();
		long contentLength = getContentLength();

		try {
			while (bytesRead != -1 && (contentLength == -1 || contentLength > totalBytesRead)) {
				if (contentLength == -1) {
					bytesRead = is.read(buffer);
				} else {
					bytesRead = is.read(buffer, 0, (int) Math.min(buffer.length, contentLength - totalBytesRead));
				}
				if (bytesRead == -1) {
					if (contentLength >= 0) {
						throw new IOException(Policy.bind("exception.unexpectedEndStream")); //$NON-NLS-1$
					}
				} else {
					totalBytesRead += bytesRead;
					os.write(buffer, 0, bytesRead);
				}
			}
		} finally {
			bufferPool.putBuffer(buffer);
			inputRead = true;
		}
	}
}
