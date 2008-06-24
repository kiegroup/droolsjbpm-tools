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

import org.eclipse.webdav.internal.kernel.utils.Assert;

/**
 * This class implements a BASE64 character encoder as specified in
 * RFC1521.
 */
public final class Base64Encoder {
	/**
	 * Returns the BASE64 encoded <code>String</code> of the given data.
	 *
	 * @param data the bytes to be BASE64 encoded
	 * @return     the BASE64 encoded <code>String</code> of the given data
	 */
	public static String encode(byte[] data) {
		Assert.isNotNull(data);

		StringBuffer buf = new StringBuffer();

		byte b = 0;
		int bits = 2;

		for (int i = 0; i < data.length; ++i) {
			b = (byte) ((b | (data[i] >> bits)) & 0x003f);
			buf.append(encode(b));
			b = (byte) ((data[i] << 6 - bits) & 0x003f);
			bits += 2;
			if (bits == 8) {
				buf.append(encode((byte) (b & 0x003f)));
				b = 0;
				bits = 2;
			}
		}

		if (bits == 4) {
			buf.append(encode(b));
			buf.append("=="); //$NON-NLS-1$
		} else if (bits == 6) {
			buf.append(encode(b));
			buf.append('=');
		}

		return buf.toString();
	}

	private static char encode(byte b) {
		if (b >= 0 && b <= 25)
			return (char) (b + 65);
		if (b >= 26 && b <= 51)
			return (char) (b + 71);
		if (b >= 52 && b <= 61)
			return (char) (b - 4);
		if (b == 62)
			return '+';
		if (b == 63)
			return '/';
		return '=';
	}
}
