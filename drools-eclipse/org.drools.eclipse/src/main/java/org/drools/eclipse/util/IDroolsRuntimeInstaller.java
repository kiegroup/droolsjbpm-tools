package org.drools.eclipse.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

public interface IDroolsRuntimeInstaller {
	String install(IProject project, IProgressMonitor monitor);
}
