/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
