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

package org.kie.eclipse.runtime;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

public interface IRuntimeInstaller  extends Comparable<IRuntimeInstaller> {
	IRuntime install(IRuntimeManager runtimeManager, String runtimeId, IProject project, IProgressMonitor monitor);
	String getProduct();
	String[] getVersions();
	String[] getRuntimeIds(); // this is just a concatenation of the product and version strings
	String getRuntimeName();
	
}
