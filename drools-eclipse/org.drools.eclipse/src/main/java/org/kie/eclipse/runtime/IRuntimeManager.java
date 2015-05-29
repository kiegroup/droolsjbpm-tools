package org.kie.eclipse.runtime;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;



public interface IRuntimeManager {
	IRuntime getDefaultRuntime();
	IRuntime[] getConfiguredRuntimes();
	String[] getAllRuntimeNames();
	String[] getAllRuntimeIds();
	String getBundleRuntimeName();
	IRuntime getEffectiveRuntime(IRuntime selectedRuntime, boolean useDefault);
	boolean isMavenized(String runtimeId);
	void setRuntime(IRuntime runtime, IProject project, IProgressMonitor monitor) throws CoreException;
	IRuntime getRuntime(IProject project);
}
