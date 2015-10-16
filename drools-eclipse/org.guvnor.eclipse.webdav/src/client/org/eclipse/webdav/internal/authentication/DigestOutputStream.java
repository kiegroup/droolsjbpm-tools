/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
