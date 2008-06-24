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

import java.util.Enumeration;
import org.w3c.dom.Element;

/**
 * An element editor for the WebDAV conflict-report-response element.
 * See INTERNET-DRAFT draft-ietf-deltav-versioning-03.1 section 12.1.2
 * for the element's definition.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 *
 * @see Conflict
 */
public class ConflictReportResponse extends ElementEditor {
	/**
	 * Creates a new editor on the given WebDAV conflict-report-response
	 * element. The element is assumed to be well formed.
	 *
	 * @param root a conflict-report-response element
	 * @throws        MalformedElementException if there is reason to
	 *                believe that the element is not well formed
	 */
	public ConflictReportResponse(Element root) throws MalformedElementException {
		super(root, "conflict-report-response"); //$NON-NLS-1$
	}

	public Conflict addConflict() {
		return null;
	}

	public Enumeration getConflicts() throws MalformedElementException {
		return null;
	}
}
