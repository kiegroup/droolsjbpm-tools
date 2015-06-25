package org.kie.eclipse.wizard.project;

import org.kie.eclipse.runtime.IRuntime;
import org.kie.eclipse.runtime.IRuntimeManager;

public interface IKieEmptyProjectWizardPage extends IKieProjectWizardPage {

	boolean shouldCreateMavenProject();
	boolean shouldCreateKJarProject();
	IRuntime getRuntime();
	IRuntimeManager getRuntimeManager();

}
