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
import org.w3c.dom.Document;

public interface IDocumentMarshaler {

	public Document parse(Reader reader) throws IOException;

	// Write out the give document to the writer.
	// The writer's encoding scheme is given.
	public void print(Document document, Writer writer, String encoding) throws IOException;
}
