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
package org.eclipse.webdav.internal.utils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.StringTokenizer;
import org.eclipse.webdav.Policy;
import org.eclipse.webdav.internal.kernel.utils.Assert;

/**
 * Decodes a <code>URL</code> from an <code>ASCII</code> readable
 * <code>URL</code> that is safe for transport.
 *
 * @see URLEncoder
 */
public final class URLDecoder {

	private static final byte[] hexChars = {(byte) '0', //
			(byte) '1', //
			(byte) '2', //
			(byte) '3', //
			(byte) '4', //
			(byte) '5', //
			(byte) '6', //
			(byte) '7', //
			(byte) '8', //
			(byte) '9', //
			(byte) 'a', //
			(byte) 'b', //
			(byte) 'c', //
			(byte) 'd', //
			(byte) 'e', //
			(byte) 'f', //
			(byte) 'A', //
			(byte) 'B', //
			(byte) 'C', //
			(byte) 'D', //
			(byte) 'E', //
			(byte) 'F'};
	private static final byte[] hexCharValues = {(byte) ('0' - '0'), //
			(byte) ('1' - '0'), //
			(byte) ('2' - '0'), //
			(byte) ('3' - '0'), //
			(byte) ('4' - '0'), //
			(byte) ('5' - '0'), //
			(byte) ('6' - '0'), //
			(byte) ('7' - '0'), //
			(byte) ('8' - '0'), //
			(byte) ('9' - '0'), //
			(byte) ('a' - 'a' + 10), //
			(byte) ('b' - 'a' + 10), //
			(byte) ('c' - 'a' + 10), //
			(byte) ('d' - 'a' + 10), //
			(byte) ('e' - 'a' + 10), //
			(byte) ('f' - 'a' + 10), //
			(byte) ('A' - 'A' + 10), //
			(byte) ('B' - 'A' + 10), //
			(byte) ('C' - 'A' + 10), //
			(byte) ('D' - 'A' + 10), //
			(byte) ('E' - 'A' + 10), //
			(byte) ('F' - 'A' + 10)};

	/**
	 * Prevents instances from being created.
	 */
	private URLDecoder() {
		super();
	}

	/**
	 * Decodes the given <code>URL</code> from an <code>ASCII</code>
	 * readable <code>URL</code> that is safe for transport. Returns the
	 * result.
	 *
	 * @return the result of decoding the given <code>URL</code> from an
	 *         <code>ASCII</code> readable <code>URL</code> that is safe for
	 *         transport
	 */
	public static String decode(String url) {
		try {
			return decode(new URL(url)).toString();
		} catch (MalformedURLException e) {
			// ignore or log?
		}

		String file;
		String ref = null;

		int lastSlashIndex = url.lastIndexOf('/');
		int lastHashIndex = url.lastIndexOf('#');
		if ((lastHashIndex - lastSlashIndex > 1) && lastHashIndex < url.length() - 1) {
			file = url.substring(0, lastHashIndex);
			ref = url.substring(lastHashIndex + 1, url.length());
		} else {
			file = url;
		}

		return decode(file, ref);
	}

	/**
	 * Decodes the file and reference parts of a <code>URL</code> from an
	 * <code>ASCII</code> readable <code>URL</code> that is safe for
	 * transport. Returns the result.
	 *
	 * @return the result of decoding the file and reference parts of a
	 *         <code>URL</code> from an <code>ASCII</code> readable
	 *         <code>URL</code> that is safe for transport
	 */
	public static String decode(String file, String ref) {
		StringBuffer buf = new StringBuffer();
		StringTokenizer tokenizer = new StringTokenizer(file, "/", true); //$NON-NLS-1$

		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.nextToken();
			if (token.equals("/")) { //$NON-NLS-1$
				buf.append(token);
			} else {
				buf.append(decodeSegment(token));
			}
		}

		if (ref != null) {
			buf.append('#');
			buf.append(decodeSegment(ref));
		}

		return buf.toString();
	}

	/**
	 * Decodes the given <code>URL</code> from an <code>ASCII</code>
	 * readable <code>URL</code> that is safe for transport. Returns the
	 * result.
	 *
	 * @return the result of decoding the given <code>URL</code> from an
	 *         <code>ASCII</code> readable <code>URL</code> that is safe for
	 *         transport
	 */
	public static URL decode(URL url) {
		String file = url.getFile();
		String ref = url.getRef();
		try {
			return new URL(url.getProtocol(), url.getHost(), url.getPort(), decode(file, ref));
		} catch (MalformedURLException e) {
			Assert.isTrue(false, Policy.bind("assert.internalError")); //$NON-NLS-1$
		}
		return null;
	}

	public static String decodeSegment(String segment) {
		byte[] encodedBytes = segment.getBytes();
		byte[] decodedBytes = new byte[encodedBytes.length];
		int decodedLength = 0;

		for (int i = 0; i < encodedBytes.length; i++) {
			byte b = encodedBytes[i];
			try {
				if (b == '%') {
					byte enc1 = encodedBytes[++i];
					byte enc2 = encodedBytes[++i];
					b = (byte) ((hexToByte(enc1) << 4) + hexToByte(enc2));
				}
				decodedBytes[decodedLength++] = b;
			} catch (ArrayIndexOutOfBoundsException e) {
				Assert.isTrue(false, Policy.bind("assert.decodeSegment")); //$NON-NLS-1$
			} catch (IllegalArgumentException e) {
				Assert.isTrue(false, Policy.bind("assert.decodeSegment")); //$NON-NLS-1$
			}
		}
		try {
			return new String(decodedBytes, 0, decodedLength, "UTF8"); //$NON-NLS-1$
		} catch (UnsupportedEncodingException exception) {
			Assert.isTrue(false, Policy.bind("assert.internalError")); //$NON-NLS-1$
			// avoid compiler error
			return null;
		}
	}

	private final static byte hexToByte(byte ch) {
		for (int i = 0; i < hexChars.length; i++)
			if (hexChars[i] == ch)
				return hexCharValues[i];
		throw new IllegalArgumentException();
	}
}
