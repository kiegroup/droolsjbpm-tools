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
package org.eclipse.webdav.internal.kernel.utils;

import java.util.Enumeration;

public abstract class EnumerationConverter extends EnumerationFilter {

	protected Enumeration sourceEnum;

	public EnumerationConverter(Enumeration sourceEnum) {
		super();
		this.sourceEnum = sourceEnum;
	}

	/**
	 * @see #hasMoreElements()
	 */
	public boolean hasMoreElements() {
		return sourceEnum.hasMoreElements();
	}

	/**
	 * @see #nextElement()
	 * Subclasses should override ths method to convert the
	 * source enum objects to the new types.
	 */
	public abstract Object nextElement();
}
