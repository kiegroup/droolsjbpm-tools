package org.kie.eclipse.wizard.project;

import org.eclipse.core.runtime.IProgressMonitor;
import org.kie.eclipse.runtime.IRuntime;
import org.kie.eclipse.runtime.IRuntimeManager;


public interface IKieProjectStartWizardPage extends IKieProjectWizardPage {
	int getInitialProjectContent();
	IRuntime getRuntime();
	IRuntimeManager getRuntimeManager();
	IProgressMonitor getProgressMonitor();
	void setProgressMonitor(IProgressMonitor progressMonitor);
}
