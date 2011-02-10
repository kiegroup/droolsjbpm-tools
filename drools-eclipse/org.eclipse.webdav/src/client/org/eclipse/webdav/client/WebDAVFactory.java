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

import org.eclipse.webdav.*;
import org.eclipse.webdav.dom.QualifiedName;
import org.eclipse.webdav.dom.QualifiedNameImpl;
import org.eclipse.webdav.internal.kernel.*;
import org.w3c.dom.Document;

/**
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 */
public class WebDAVFactory implements ILocatorFactory, IContextFactory, IDocumentFactory {

	public static IContextFactory contextFactory = new ContextFactory();
	public static IDocumentFactory documentFactory = new DocumentFactory();
	public static ILocatorFactory locatorFactory = new LocatorFactory();

	/**
	 * WebDavFactory constructor comment.
	 */
	public WebDAVFactory() {
		super();
	}

	public IContext newContext() {
		return contextFactory.newContext();
	}

	public IContext newContext(IContext baseContext) {
		return contextFactory.newContext(baseContext);
	}

	public Document newDocument() {
		return documentFactory.newDocument();
	}

	public ILocator newLocator(String resourceURL) {
		return locatorFactory.newLocator(resourceURL);
	}

	/**
	 * Answer a new resource locator that identifies a particular
	 * server resource by it's URL and label.
	 *
	 * @param resourceURL the URL of the resource.
	 * @param label the version label of the resource.
	 * @return the Locator to the resource.
	 */
	public ILocator newLocator(String resourceURL, String label) {
		return locatorFactory.newLocator(resourceURL, label);
	}

	public QualifiedName newQualifiedName(String qualifier, String localName) {
		return new QualifiedNameImpl(qualifier, localName);
	}

	public ILocator newStableLocator(String resourceURL) {
		return locatorFactory.newStableLocator(resourceURL);
	}
}
