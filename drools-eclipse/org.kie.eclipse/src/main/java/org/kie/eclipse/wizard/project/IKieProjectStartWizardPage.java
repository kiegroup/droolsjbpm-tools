package org.kie.eclipse.wizard.project;

import org.kie.eclipse.runtime.IRuntime;
import org.kie.eclipse.runtime.IRuntimeManager;


public interface IKieProjectStartWizardPage extends IKieProjectWizardPage {
	int getInitialProjectContent();
	IRuntime getRuntime();
	IRuntimeManager getRuntimeManager();
}
