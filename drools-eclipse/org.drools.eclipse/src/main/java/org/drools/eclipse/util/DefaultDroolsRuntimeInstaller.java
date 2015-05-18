package org.drools.eclipse.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jgit.fnmatch.FileNameMatcher;

public class DefaultDroolsRuntimeInstaller extends DroolsRuntimeInstallerBase {

	public static DefaultDroolsRuntimeInstaller INSTANCE = new DefaultDroolsRuntimeInstaller();

	private DefaultDroolsRuntimeInstaller() {}
	
	@Override
	public String install(String runtimeId, IProject project, IProgressMonitor monitor) {
		FileNameMatcher fn;
		DroolsRuntimeInstallerBase installer = FACTORY.getInstaller(runtimeId);
		if (installer!=null) {
			
		}
		return null;
	}
}
