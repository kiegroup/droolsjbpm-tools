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

/**
 * This class provides a function that converts byte arrays to their
 * equivalent hexadecimal string.
 */
public final class HexConverter {
	/**
	 * Converts the given byte array to its equivalent hexadecimal string
	 * and returns the result.
	 */
	public static String toHex(byte[] arr) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < arr.length; ++i) {
			buf.append(Integer.toHexString((arr[i] >> 4) & 0x0f));
			buf.append(Integer.toHexString(arr[i] & 0x0f));
		}
		return buf.toString();
	}

	/**
	 * Converts the given int array to its equivalent hexadecimal string
	 * and returns the result.
	 */
	public static String toHex(int[] arr) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < arr.length; ++i) {
			buf.append(Integer.toHexString((arr[i] >> 28) & 0x0000000f));
			buf.append(Integer.toHexString((arr[i] >> 24) & 0x0000000f));
			buf.append(Integer.toHexString((arr[i] >> 20) & 0x0000000f));
			buf.append(Integer.toHexString((arr[i] >> 16) & 0x0000000f));
			buf.append(Integer.toHexString((arr[i] >> 12) & 0x0000000f));
			buf.append(Integer.toHexString((arr[i] >> 8) & 0x0000000f));
			buf.append(Integer.toHexString((arr[i] >> 4) & 0x0000000f));
			buf.append(Integer.toHexString((arr[i]) & 0x0000000f));
		}
		return buf.toString();
	}
}
