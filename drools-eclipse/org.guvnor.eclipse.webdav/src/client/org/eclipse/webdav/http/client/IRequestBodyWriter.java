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

package org.eclipse.webdav.http.client;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import org.eclipse.webdav.IContext;

/**
 * An object writer for <code>Request</code>s whose body is in object
 * form and shouldn't be flattened until an output stream is available.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 *
 * @see Request#Request(String, URL, IContext, IRequestBodyWriter)
 * @see Request#getRequestBodyWriter()
 */
public interface IRequestBodyWriter {
    /**
     * Writes a request body to the given output stream.
     *
     * @throws IOException if there is a problem writing to the stream
     */
    public void writeRequestBody(OutputStream os) throws IOException;
}
