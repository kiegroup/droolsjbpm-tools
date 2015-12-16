/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.eclipse.webdav.dom;

import org.eclipse.webdav.Policy;
import org.eclipse.webdav.http.client.Response;

/**
 * A <code>Status</code> parses an http status string.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public class Status {

    private int fStatusCode;
    private String fStatusMessage;

    /**
     * Creates a new status from the given http status string.
     *
     * @param  status the http status string; for example, "HTTP/1.1 200 OK"
     * @throws MalformedElementException if the given status is malformed
     */
    public Status(String status) throws MalformedElementException {
        status = status.trim();
        int len = status.length();

        int end;
        int start = 0;

        while (start < len && !Character.isWhitespace(status.charAt(start))) {
            ++start;
        }
        while (start < len && Character.isWhitespace(status.charAt(start))) {
            ++start;
        }

        if (start >= len) {
            throw new MalformedElementException(Policy.bind("exception.malformedStatus", status)); //$NON-NLS-1$
        }

        end = start;
        while (end < len && Character.isDigit(status.charAt(end))) {
            ++end;
        }

        if (end == start || end >= len) {
            throw new MalformedElementException(Policy.bind("exception.malformedStatus", status)); //$NON-NLS-1$
        }

        fStatusCode = Integer.parseInt(status.substring(start, end));

        start = end;
        while (start < len && Character.isWhitespace(status.charAt(start))) {
            ++start;
        }

        if (start >= len) {
            throw new MalformedElementException(Policy.bind("exception.malformedStatus", status)); //$NON-NLS-1$
        }

        fStatusMessage = status.substring(start, len);
    }

    /**
     * Returns this status' status code. For example, if the http status
     * string was "HTTP/1.1 200 OK", the status code would be 200.
     *
     * @return this status' status code
     * @see    Response
     */
    public int getStatusCode() {
        return fStatusCode;
    }

    /**
     * Returns this status' status message; for example, if the http status
     * string was "HTTP/1.1 200 OK", the status message would be "OK".
     *
     * @return this status' status message
     */
    public String getStatusMessage() {
        return fStatusMessage;
    }
}
