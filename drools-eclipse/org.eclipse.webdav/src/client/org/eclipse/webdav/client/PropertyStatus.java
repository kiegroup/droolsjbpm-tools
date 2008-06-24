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
package org.eclipse.webdav.client;

import org.eclipse.webdav.dom.*;
import org.eclipse.webdav.internal.kernel.utils.Assert;
import org.w3c.dom.Element;

/**
 * A <code>PropertyStatus</code> holds the WebDAV server's response from
 * a client's request to retrieve a particular property.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 */
public class PropertyStatus {
	private Element property;
	private int statusCode;
	private String statusMessage;

	/**
	 * Create a new property status from the given property and the
	 * specified status.
	 *
	 * @param property      the requested property
	 * @param statusCode    the resulting status code
	 * @param statusMessage the resulting status message
	 */
	public PropertyStatus(Element property, int statusCode, String statusMessage) {
		Assert.isNotNull(property);
		Assert.isNotNull(statusMessage);
		this.property = property;
		this.statusCode = statusCode;
		this.statusMessage = statusMessage;
	}

	/**
	 * Return the requested property as an <code>Element</code>.  The
	 * element will not have any children if the property could not be
	 * retrieved as indicated by the status.
	 */
	public Element getProperty() {
		return property;
	}

	/**
	 * Return the requested property's name.
	 */
	public QualifiedName getPropertyName() throws MalformedElementException {
		return ElementEditor.getQualifiedName(property);
	}

	/**
	 * Return the status code that resulted from retrieving (or attempting
	 * to retrieve) this property status' property.
	 */
	public int getStatusCode() {
		return statusCode;
	}

	/**
	 * Return the status message that resulted from retrieving (or
	 * attempting to retrieve) this property status' property.
	 */
	public String getStatusMessage() {
		return statusMessage;
	}
}
