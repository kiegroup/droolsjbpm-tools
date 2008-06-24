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
