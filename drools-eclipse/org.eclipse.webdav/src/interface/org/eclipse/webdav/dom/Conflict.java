/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.eclipse.webdav.dom;

import java.util.Enumeration;
import org.w3c.dom.Element;

/**
 * An element editor for the WebDAV conflict element. See INTERNET-DRAFT
 * draft-ietf-deltav-versioning-03.1 section 12.1.2 for the element's
 * definition.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 *
 * @see ConflictReportResponse
 */
public class Conflict extends ElementEditor {
	/**
	 * Creates a new editor on the given WebDAV conflict element. The
	 * element is assumed to be well formed.
	 *
	 * @param root a conflict element
	 * @throws        MalformedElementException if there is reason to
	 *                believe that the element is not well formed
	 */
	public Conflict(Element root) throws MalformedElementException {
		super(root, "conflict"); //$NON-NLS-1$
	}

	public void addContributor(String contributor) {
		// do nothing
	}

	public String getCommonAncestor() throws MalformedElementException {
		return null;
	}

	public Enumeration getContributors() throws MalformedElementException {
		return null;
	}

	public String getHref() throws MalformedElementException {
		return null;
	}

	public void setCommonAncestor(String commonAncestor) {
		// do nothing
	}

	public void setContributor(String contributor) {
		// do nothing
	}

	public void setHref(String href) {
		// do nothing
	}
}
