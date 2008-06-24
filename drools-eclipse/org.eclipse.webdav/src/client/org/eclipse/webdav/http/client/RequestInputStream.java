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
import org.eclipse.webdav.client.Policy;
import org.eclipse.webdav.internal.kernel.utils.Assert;

/**
 * A resettable <code>InputStream</code>.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public class RequestInputStream extends InputStream {
	private long length = -1;
	private long totalBytesRead = 0;
	private InputStream is = null;
	private File file = null;
	private FileOutputStream fos = null;
	private boolean deleteFile = false;

	/**
	 * Creates a <code>RequestInputStream</code> on the given byte array.
	 *
	 * @param b the underlying byte array
	 */
	public RequestInputStream(byte[] b) {
		length = b.length;
		is = new ByteArrayInputStream(b);
	}

	/**
	 * Creates a <code>RequestInputStream</code> on the given
	 * <code>ByteArrayInputStream</code>.
	 *
	 * @param bais the underlying input stream
	 */
	public RequestInputStream(ByteArrayInputStream bais) {
		Assert.isNotNull(bais);
		length = bais.available();
		is = bais;
	}

	/**
	 * Creates a <code>RequestInputStream</code> on the given file.
	 *
	 * @param file the underlying file
	 * @exception IOException if there is a problem opening the file
	 */
	public RequestInputStream(File file) throws IOException {
		Assert.isNotNull(file);
		length = file.length();
		is = new FileInputStream(file);
		this.file = file;
	}

	/**
	 * Creates a <code>RequestInputStream</code> on the given
	 * <code>InputStream</code>. The length of the stream is set to be the
	 * given length. If the length of the stream is unknown, the given length
	 * must be -1.
	 * <p>Note that to enable reset on streams created using this constructor,
	 * the streams content is written to a temporary file while the stream
	 * is read. This results in a loss of performance, so use this
	 * constructor as a last resort.
	 *
	 * @param is the underlying input stream
	 * @param length the length of the stream, or -1 if the length is unknown
	 * @exception IOException if there is a problem creating or opening the
	 * temporary file
	 */
	public RequestInputStream(InputStream is, long length) throws IOException {
		Assert.isNotNull(is);
		Assert.isTrue(length >= -1);
		this.length = length;
		this.is = is;
		file = File.createTempFile("ris", ".tmp"); //$NON-NLS-1$ //$NON-NLS-2$
		fos = new FileOutputStream(file);
		deleteFile = true;
	}

	/**
	 * @see InputStream#close()
	 */
	public void close() throws IOException {
		is.close();
		if (fos != null)
			fos.close();
		if (deleteFile)
			file.delete();
	}

	/**
	 * Returns the length of the stream, or -1 if the length of the stream is
	 * unknown. Note the length of the stream is always known once the stream
	 * has been reset.
	 *
	 * @return the length of the stream
	 */
	public long length() {
		return length;
	}

	/**
	 * @see InputStream#read()
	 */
	public int read() throws IOException {
		int b = is.read();

		if (b == -1) {
			if (length != -1 && totalBytesRead < length) {
				throw new IOException(Policy.bind("exception.unexpectedEndStream")); //$NON-NLS-1$
			}
		} else {
			++totalBytesRead;
			if (fos != null) {
				fos.write(b);
			}
		}

		return b;
	}

	/**
	 * @see InputStream#read(byte[], int, int)
	 */
	public int read(byte b[], int off, int len) throws IOException {
		int bytesRead = is.read(b, off, len);
		if (bytesRead == -1) {
			if (length != -1 && totalBytesRead < length) {
				throw new IOException(Policy.bind("exception.unexpectedEndStream")); //$NON-NLS-1$
			}
		} else {
			totalBytesRead += bytesRead;
			if (fos != null) {
				fos.write(b, off, len);
			}
		}
		return bytesRead;
	}

	/**
	 * Resets the stream to its beginning so it can be read again.
	 *
	 * @exception IOException if there is an I/O error
	 */
	public void reset() throws IOException {
		if (file == null) {
			((ByteArrayInputStream) is).reset();
		} else {
			if (fos != null) {
				while (skip(4096) > 0);
				fos.close();
				fos = null;
				if (length == -1) {
					length = totalBytesRead;
				}
			}
			is.close();
			is = new FileInputStream(file);
		}

		totalBytesRead = 0;
	}
}
