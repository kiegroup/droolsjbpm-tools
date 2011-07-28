package org.eclipse.webdav.internal.kernel;

import java.io.*;
import org.w3c.dom.Document;

public interface IDocumentMarshaler {

    public Document parse(Reader reader) throws IOException;

    // Write out the give document to the writer.
    // The writer's encoding scheme is given.
    public void print(Document document, Writer writer, String encoding) throws IOException;
}
