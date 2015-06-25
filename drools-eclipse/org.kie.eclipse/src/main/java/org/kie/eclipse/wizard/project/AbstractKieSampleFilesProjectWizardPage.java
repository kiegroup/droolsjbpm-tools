package org.kie.eclipse.wizard.project;

import org.eclipse.jface.wizard.IWizardPage;


public abstract class AbstractKieSampleFilesProjectWizardPage extends AbstractKieEmptyProjectWizardPage implements IKieSampleFilesProjectWizardPage {

	public AbstractKieSampleFilesProjectWizardPage(String pageName) {
		super(pageName);
	}

	@Override
	public boolean shouldCreateMavenProject() {
		return true;
	}

	@Override
	public boolean shouldCreateKJarProject() {
		return true;
	}

	@Override
	public IWizardPage getNextPage() {
		return null;
	}
}
