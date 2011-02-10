/*
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

package org.eclipse.webdav.client;

import java.io.IOException;
import org.eclipse.webdav.ILocator;
import org.eclipse.webdav.IResponse;
import org.eclipse.webdav.dom.Mkworkspace;
import org.eclipse.webdav.internal.kernel.DAVException;
import org.eclipse.webdav.internal.kernel.SystemException;
import org.w3c.dom.Document;

/**
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public class WorkspaceHandle extends CollectionHandle {

	public WorkspaceHandle(DAVClient davClient, ILocator locator) {
		super(davClient, locator);
	}

	/**
	 * Check out this resource. Returns a resource handle on the checked out
	 * version selector, or the working resource if a version is checked out.
	 */
	public AbstractResourceHandle checkOut() throws DAVException {
		ILocator locator = protectedCheckOut();
		return new WorkspaceHandle(davClient, locator);
	}

	/**
	 * Create a new workspace in the location described by this handle.
	 * <p>
	 * A new workspace is created using a MKWORKSPACE method call.</p>
	 *
	 * @throws DAVException if a problem occured creating the workspace
	 * on the WebDAV server.
	 */
	public void create() throws DAVException {
		Document document = newDocument();
		Mkworkspace.create(document);
		IResponse response = null;
		try {
			response = davClient.mkworkspace(locator, newContext(), document);
			examineResponse(response);
		} catch (IOException e) {
			throw new SystemException(e);
		} finally {
			closeResponse(response);
		}
	}
}
