package org.kie.eclipse.wizard.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.kie.eclipse.runtime.IRuntime;
import org.kie.eclipse.runtime.IRuntimeManager;

public interface IKieOnlineExampleProjectWizardPage extends IKieProjectWizardPage {

	String downloadOnlineExampleProject(IProject project, IProgressMonitor monitor);
	String getProductId();
	IRuntime getRuntime();
	IRuntimeManager getRuntimeManager();
}
