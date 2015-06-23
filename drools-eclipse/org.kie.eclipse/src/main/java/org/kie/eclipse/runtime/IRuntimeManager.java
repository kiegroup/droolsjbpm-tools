/*
 * Copyright 2015 JBoss Inc
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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;



public interface IRuntimeManager {
	IRuntime getDefaultRuntime();
	IRuntime[] getConfiguredRuntimes();
	String getBundleRuntimeName();
	String getBundleRuntimeVersion();
	IRuntime createNewRuntime();
	IRuntime createBundleRuntime(String location);
	IRuntime getEffectiveRuntime(IRuntime selectedRuntime, boolean useDefault);
	boolean isMavenized(IRuntime runtime);
	void setRuntimes(IRuntime[] runtimes);
	void setRuntime(IRuntime runtime, IProject project, IProgressMonitor monitor) throws CoreException;
	IRuntime getRuntime(IProject project);
	String getSettingsFilename();
	void recognizeJars(IRuntime runtime);
	void addListener(IRuntimeManagerListener listener);
	void removeListener(IRuntimeManagerListener listener);
}
