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

package org.eclipse.webdav.internal.kernel;

import java.io.*;
import org.w3c.dom.Document;

public interface IDocumentMarshaler {

    public Document parse(Reader reader) throws IOException;

    // Write out the give document to the writer.
    // The writer's encoding scheme is given.
    public void print(Document document, Writer writer, String encoding) throws IOException;
}
