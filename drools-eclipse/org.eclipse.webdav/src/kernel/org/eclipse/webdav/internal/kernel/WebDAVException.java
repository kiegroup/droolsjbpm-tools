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

package org.eclipse.webdav.internal.kernel;

/**
 * This is the superclass of all WebDAV protocol exceptions
 * It contains a status code that provides information, and a
 * descriptive message.
 */
public class WebDAVException extends DAVException {

    /**
     * Comment for <code>serialVersionUID</code>
     */
    private static final long serialVersionUID = 510l;
    private int statusCode = 0;
    protected Object data = null;

    /**
     * Construct a WebDAVException
     *
     * @param statusCode the HTTP/1.1 or WebDAV status code
     * @param statusMessage a message describing the exception of status code
     */
    public WebDAVException(int statusCode, String statusMessage) {
        super(statusMessage);
        this.statusCode = statusCode;
    }

    /**
     * Construct a WebDAVException
     *
     * @param statusCode the HTTP/1.1 or WebDAV status code
     * @param statusMessage a message describing the exception of status code
     */
    public WebDAVException(int statusCode, String statusMessage, Object data) {
        super(statusMessage);
        this.statusCode = statusCode;
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    /**
     * Get the status code that provides additional information about the
     * exception. These status codes are defined by the HTTP/1.1 and WebDAV
     * specifications.
     *
     * @return the HTTP/1.1 or WebDAV status code
     */
    public int getStatusCode() {
        return statusCode;
    }

    /**
     * Render this WebDAVException as a string including its status code.
     *
     * @return the string includes the status code and message
     */
    public String toString() {
        return "WebDAVException(" + statusCode + ": " + getMessage() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
