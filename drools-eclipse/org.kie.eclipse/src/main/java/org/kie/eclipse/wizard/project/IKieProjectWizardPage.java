package org.kie.eclipse.wizard.project;

import java.util.Collection;

import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.jface.wizard.IWizardPage;

public interface IKieProjectWizardPage extends IWizardPage {
	public final static int EMPTY_PROJECT = 0;
	public final static int ONLINE_EXAMPLE_PROJECT = 1;
	public final static int SAMPLE_FILES_PROJECT = 2;

	Collection<IProjectDescription> getNewProjectDescriptions();
}
