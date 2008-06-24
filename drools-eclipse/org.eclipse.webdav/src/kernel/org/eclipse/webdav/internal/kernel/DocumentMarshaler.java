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
package org.eclipse.webdav.internal.kernel;

import java.io.*;
import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class DocumentMarshaler implements IDocumentMarshaler {

	public Document parse(Reader reader) throws IOException {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder parser = factory.newDocumentBuilder();
			return parser.parse(new InputSource(reader));
		} catch (ParserConfigurationException e) {
			throw new IOException(e.getMessage());
		} catch (SAXException e) {
			throw new IOException(e.getMessage());
		}
	}

	public void print(Document document, Writer writer, String encoding) throws IOException {
		Transformer transformer = null;
		try {
			transformer = TransformerFactory.newInstance().newTransformer();
		} catch (TransformerConfigurationException e) {
			throw new IOException(e.getMessageAndLocation());
		} catch (TransformerFactoryConfigurationError e) {
			throw new IOException(e.getMessage());
		}
		DOMSource source = new DOMSource(document);
		StreamResult result = new StreamResult(writer);
		try {
			transformer.transform(source, result);
		} catch (TransformerException e) {
			throw new IOException(e.getMessageAndLocation());
		}
	}
}
