package org.eclipse.webdav.internal.kernel;

import javax.xml.parsers.*;
import org.w3c.dom.Document;

public class DocumentFactory implements IDocumentFactory {

    DocumentBuilder builder = null;

    public Document newDocument() {
        if (builder == null) {
            try {
                builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            } catch (ParserConfigurationException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (FactoryConfigurationError e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return builder.newDocument();
    }
}
