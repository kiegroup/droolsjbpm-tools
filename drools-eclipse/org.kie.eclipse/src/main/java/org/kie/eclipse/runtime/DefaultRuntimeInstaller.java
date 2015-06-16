package org.kie.eclipse.runtime;

import java.util.Collection;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jgit.fnmatch.FileNameMatcher;

public class DefaultRuntimeInstaller extends AbstractRuntimeInstaller {

	public static DefaultRuntimeInstaller INSTANCE = new DefaultRuntimeInstaller();

	public DefaultRuntimeInstaller() {}
	
	@Override
	public String install(String runtimeId, String location, IProgressMonitor monitor) {
		FileNameMatcher fn;
		AbstractRuntimeInstaller installer = FACTORY.getInstaller(runtimeId);
		if (installer==null) {
			return "No installer found for "+runtimeId;
		}
		return null;
	}
	
	public static Collection<? extends IRuntimeInstaller> getInstallers() {
		return FACTORY.createInstallers();
	}

	@Override
	public String getRuntimeId() {
		return getProduct() + "_" + getVersion();
	}
}
