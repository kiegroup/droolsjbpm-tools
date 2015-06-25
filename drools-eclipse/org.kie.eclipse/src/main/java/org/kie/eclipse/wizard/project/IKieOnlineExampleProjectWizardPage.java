package org.kie.eclipse.wizard.project;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

public interface IKieOnlineExampleProjectWizardPage extends IKieProjectWizardPage {

	String downloadOnlineExampleProject(IProject project, IProgressMonitor monitor);
	String getProductId();

}
