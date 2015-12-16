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

package org.eclipse.webdav.client;

import java.io.FilterInputStream;
import java.io.IOException;
import org.eclipse.webdav.IContext;
import org.eclipse.webdav.IResponse;

/**
 * An <code>InputStream</code> on a response from a WebDAV server.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public class ResponseInputStream extends FilterInputStream {
    private IResponse response;

    /**
     * Creates a new input stream on the given response.
     *
     * @param response a response from a WebDAV server
     */
    public ResponseInputStream(IResponse response) {
        super(response.getInputStream());
        this.response = response;
    }

    /**
     * Closes the response and frees all system resources associated with
     * this input stream.
     */
    public void close() throws IOException {
        response.close();
    }

    /**
     * Returns the http header of the response from the WebDAV server on
     * which this input stream is based. The context contains information
     * that may be useful such as the content length and content type of
     * this input stream.
     */
    public IContext getContext() {
        return response.getContext();
    }
}
