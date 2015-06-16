/*******************************************************************************
 * Copyright (c) 2013 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.kie.eclipse.runtime;


/**
 * Listens to changes in the runtime manager
 */
public interface IRuntimeManagerListener {
	/**
	 * A runtime has been added
	 * @param rt
	 */
	public void runtimeAdded(IRuntime rt);
	
	/**
	 * A runtime has been removed
	 * @param rt
	 */
	public void runtimeRemoved(IRuntime rt);
	/**
	 * A runtime has been changed
	 * @param rt
	 */
	public void runtimesChanged(IRuntime[] newList);
}
