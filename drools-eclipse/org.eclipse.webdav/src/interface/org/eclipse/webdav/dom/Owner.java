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

import org.w3c.dom.Element;

/**
 * An element editor for the WebDAV owner element. See RFC2518 section
 * 12.10 for the element's definition.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 *
 * @see ActiveLock
 * @see LockInfo
 */
public class Owner extends ElementEditor {
	/**
	 * Creates a new editor on the given WebDAV owner element. The element
	 * is assumed to be well formed.
	 *
	 * @param root a owner element
	 * @throws        MalformedElementException if there is reason to
	 *                believe that the element is not well formed
	 */
	public Owner(Element root) throws MalformedElementException {
		super(root, "owner"); //$NON-NLS-1$
	}
}
