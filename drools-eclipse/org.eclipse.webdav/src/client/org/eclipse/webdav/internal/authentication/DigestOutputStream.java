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
package org.eclipse.webdav.internal.authentication;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * <P>An <code>OutputStream</code> that filters its data through a
 * <code>MessageDigest</code> to compute a hash value over the data.
 */
public class DigestOutputStream extends FilterOutputStream {

	private static class NullOutputStream extends OutputStream {
		public void write(int b) throws IOException {
			// do nothing
		}
	}

	private MessageDigest messageDigest = null;
	private byte[] digest = null;

	/**
	 * Creates a new <code>DigestOutputStream</code> that filters its data
	 * through a <code>MessageDigest</code> that hashes its input using the
	 * given algorithm.
	 *
	 * @param out       the data is also written to the given output stream
	 * @param algorithm a hashing algorithm, for example: "MD5"
	 */
	public DigestOutputStream(OutputStream out, String algorithm) throws NoSuchAlgorithmException {

		super(out);
		messageDigest = MessageDigest.getInstance(algorithm);
	}

	/**
	 * Creates a new <code>DigestOutputStream</code> that filters its data
	 * through a <code>MessageDigest</code> that hashes its input using the
	 * given algorithm.
	 *
	 * @param algorithm a hashing algorithm, for example: "MD5"
	 */
	public DigestOutputStream(String algorithm) throws NoSuchAlgorithmException {
		this(new NullOutputStream(), algorithm);
	}

	/**
	 * Completes the hash computation and resets the digest.
	 *
	 * @returns the hash value
	 */
	public byte[] digest() {
		if (digest == null)
			digest = messageDigest.digest();
		return digest;
	}

	/**
	 * @see OutputStream#write(byte[], int, int)
	 */
	public void write(byte b[], int off, int len) throws IOException {
		out.write(b, off, len);
		messageDigest.update(b, off, len);
	}

	/**
	 * @see OutputStream#write(int)
	 */
	public void write(int b) throws IOException {
		out.write(b);
		messageDigest.update((byte) b);
	}
}
