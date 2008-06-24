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
package org.eclipse.webdav;

import org.eclipse.webdav.internal.kernel.Context;

/**
 * Factory for constructing WebDAV contexts.
 * <p>
 * The context corresponds to the header portion of
 * a WebDAV request or response.
 * <p>
 * <b>Note:</b> This class/interface is part of an interim API that is still under 
 * development and expected to change significantly before reaching stability. 
 * It is being made available at this early stage to solicit feedback from pioneering 
 * adopters on the understanding that any code that uses this API will almost 
 * certainly be broken (repeatedly) as the API evolves.
 * </p>
 *
 * @see Context
 */
public interface IContextFactory {
	/**
	 * Create a new empty context.
	 *
	 * @return the new empty <code>Context</code>.
	 */
	public IContext newContext();

	/**
	 * Creates a new context based on the given context.
	 * <p>
	 * The newly created context acts like an acetate layer
	 * over the base context: any changes made to the new
	 * context are confined to that layer and do not affect
	 * the base context; but changes to the base context show
	 * through to the new context unless masked by another
	 * change made to the new context (c.f. properties).</p>
	 *
	 * @param baseContext the default values for the new context.
	 * @return the new wrapping <code>Context</code>.
	 */
	public IContext newContext(IContext baseContext);
}
