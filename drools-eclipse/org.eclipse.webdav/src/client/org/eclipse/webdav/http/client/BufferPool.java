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

import java.util.Vector;
import org.eclipse.webdav.internal.kernel.utils.Assert;

/**
 * A <code>BufferPool</code> holds on to a collection of buffers (byte
 * arrays) so they can be reused without having to allocate and release
 * memory.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public class BufferPool {
	/**
	 * The size of each buffer in the pool.
	 */
	private static int BUFFER_SIZE = 8 * 1024;

	/**
	 * The maximum number of buffers in the pool.
	 */
	private static int MAX_BUFFERS = 5;

	/**
	 * The buffer pool.
	 */
	private Vector pool = new Vector(MAX_BUFFERS);

	/**
	 * Returns a buffer (byte array) of size <code>BUFFER_SIZE</code> from the
	 * pool. When the buffer is no longer needed, it should be put back in the
	 * pool for future use by calling <code>putBuffer</code>.
	 *
	 * @return a buffer
	 * @see #putBuffer(byte[])
	 */
	public synchronized byte[] getBuffer() {
		if (pool.isEmpty())
			return new byte[BUFFER_SIZE];
		byte[] buffer = (byte[]) pool.lastElement();
		pool.removeElementAt(pool.size() - 1);
		return buffer;
	}

	/**
	 * Puts the given buffer into the pool for future use. The size of the
	 * buffer must be <code>BUFFER_SIZE</code>.
	 *
	 * @param buffer the buffer to be put back into the buffer pool
	 * @see #getBuffer()
	 */
	public synchronized void putBuffer(byte[] buffer) {
		Assert.isNotNull(buffer);
		Assert.isTrue(buffer.length == BUFFER_SIZE);
		if (pool.size() < MAX_BUFFERS)
			pool.addElement(buffer);
	}
}
