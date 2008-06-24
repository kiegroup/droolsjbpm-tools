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
package org.eclipse.webdav.dom;

import org.eclipse.webdav.Policy;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * The mkworkspace element editor is simple since it is a placeholder
 * for future enhancements and implementation specific arguments.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public class Mkworkspace extends ElementEditor {
	/**
	 * Mkworkspace constructor.
	 *
	 * @param root Element forming the root of the mkworkspace tree.
	 * @throws MalformedElementException if the root element is malformed.
	 */
	public Mkworkspace(Element root) throws MalformedElementException {
		super(root, "mkworkspace"); //$NON-NLS-1$
	}

	/**
	 * Creates a new WebDAV mkworkspace element and sets it as the root of
	 * the given document.  Returns an editor on the new root element.
	 * <p>
	 * The document must not be <code>null</code>, and must not already have
	 * a root element.</p>
	 *
	 * @param document the document that will become the root of a new
	 *                 mkworkspace element
	 * @return         an element editor on a mkworkspace element
	 */
	public static Mkworkspace create(Document document) {
		Assert.isNotNull(document);
		Assert.isTrue(document.getOwnerDocument() == null);
		Element element = create(document, "mkworkspace"); //$NON-NLS-1$
		try {
			return new Mkworkspace(element);
		} catch (MalformedElementException e) {
			Assert.isTrue(false, Policy.bind("assert.internalError")); //$NON-NLS-1$
			return null; // Never reached.
		}
	}
}
