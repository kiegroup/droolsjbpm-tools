/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

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
