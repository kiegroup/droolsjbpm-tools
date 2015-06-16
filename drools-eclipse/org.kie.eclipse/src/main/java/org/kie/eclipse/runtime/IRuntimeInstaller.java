package org.kie.eclipse.runtime;

import org.eclipse.core.runtime.IProgressMonitor;

public interface IRuntimeInstaller {
	String install(String runtimeId, String location, IProgressMonitor monitor);
	String getProduct();
	String getVersion();
	String getRuntimeId(); // this is just a concatenation of the product and version strings
	String getRuntimeName();
	
}
