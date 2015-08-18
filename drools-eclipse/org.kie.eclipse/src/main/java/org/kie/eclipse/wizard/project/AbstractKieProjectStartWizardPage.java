package org.kie.eclipse.wizard.project;

import java.util.Collection;

import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.kie.eclipse.Activator;
import org.kie.eclipse.runtime.IRuntime;
import org.kie.eclipse.runtime.IRuntimeManager;

public class AbstractKieProjectStartWizardPage extends WizardPage implements IKieProjectStartWizardPage {

	private int initialProjectContent = IKieProjectWizardPage.EMPTY_PROJECT;

	public AbstractKieProjectStartWizardPage(String pageName) {
		super(pageName);
		setDescription("Select the initial Project contents");
	}

	@Override
	public void createControl(Composite parent) {
        GridData gd;
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setFont(parent.getFont());
        composite.setLayout(new GridLayout(2, false));
        gd = new GridData(GridData.FILL_HORIZONTAL);
        composite.setLayoutData(gd);

        final Button emptyProjectButton = new Button(composite, SWT.RADIO | SWT.PUSH);
        emptyProjectButton.setImage(Activator.getImage("icons/wizards/empty-project-pushed.png"));
        gd = new GridData(GridData.CENTER);
        gd.horizontalIndent = 10;
        gd.verticalIndent = 20;
        emptyProjectButton.setLayoutData(gd);
        
        final Label emptyProjectDescription = new Label(composite, SWT.WRAP);
        emptyProjectDescription.setText(
        		"Create an empty project. "+
        		"The project classpath will be configured with the selected Runtime and optional Maven and KJar artifacts. "+
        		"This option may be used for creating both Rule/Process and Work Item Definition Projects."
        );
        gd = new GridData(GridData.FILL, SWT.CENTER, true, false, 1, 1);
        gd.verticalIndent = 20;
        gd.horizontalIndent = 20;
        gd.widthHint = 100;
        gd.heightHint = 80;
        emptyProjectDescription.setLayoutData(gd);

        final Button sampleFilesProjectButton = new Button(composite, SWT.RADIO | SWT.PUSH);
        sampleFilesProjectButton.setImage(Activator.getImage("icons/wizards/sample-files-project.png"));
        gd = new GridData(GridData.BEGINNING);
        gd.horizontalIndent = 10;
        gd.verticalIndent = 20;
        sampleFilesProjectButton.setLayoutData(gd);

        final Label sampleFilesProjectDescription = new Label(composite, SWT.WRAP);
        sampleFilesProjectDescription.setText(
        		"Create a project and populate it with some example files to help you get started quickly. "+
        		"This also creates the recommended folder structure for organizing all of your deployable artifacts."
        );
        gd = new GridData(GridData.FILL, SWT.CENTER, true, false, 1, 1);
        gd.verticalIndent = 20;
        gd.horizontalIndent = 20;
        gd.widthHint = 100;
        gd.heightHint = 80;
        sampleFilesProjectDescription.setLayoutData(gd);
        
        final Button onlineExampleProjectButton = new Button(composite, SWT.RADIO | SWT.PUSH);
        onlineExampleProjectButton.setImage(Activator.getImage("icons/wizards/online-example-project.png"));
        gd = new GridData(GridData.BEGINNING);
        gd.horizontalIndent = 10;
        gd.verticalIndent = 20;
        onlineExampleProjectButton.setLayoutData(gd);

        final Label onlineExampleProjectDescription = new Label(composite, SWT.WRAP);
        onlineExampleProjectDescription.setText(
        		"If you have internet acces, you may select from one of several different example projects from our online repository. "+
        		"Examples are available ready to deploy and run on different versions of the Runtime."
        );
        gd = new GridData(GridData.FILL, SWT.CENTER, true, false, 1, 1);
        gd.verticalIndent = 20;
        gd.horizontalIndent = 20;
        gd.widthHint = 100;
        gd.heightHint = 80;
        onlineExampleProjectDescription.setLayoutData(gd);

        emptyProjectButton.setSelection(true);
        emptyProjectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (initialProjectContent == IKieProjectWizardPage.EMPTY_PROJECT)
					return;
				initialProjectContent = IKieProjectWizardPage.EMPTY_PROJECT;
				emptyProjectButton.setImage(Activator.getImage("icons/wizards/empty-project-pushed.png"));
		        sampleFilesProjectButton.setImage(Activator.getImage("icons/wizards/sample-files-project.png"));
		        onlineExampleProjectButton.setImage(Activator.getImage("icons/wizards/online-example-project.png"));
			}
        });
        
        sampleFilesProjectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (initialProjectContent == IKieProjectWizardPage.SAMPLE_FILES_PROJECT)
					return;
				initialProjectContent = IKieProjectWizardPage.SAMPLE_FILES_PROJECT;
				emptyProjectButton.setImage(Activator.getImage("icons/wizards/empty-project.png"));
		        sampleFilesProjectButton.setImage(Activator.getImage("icons/wizards/sample-files-project-pushed.png"));
		        onlineExampleProjectButton.setImage(Activator.getImage("icons/wizards/online-example-project.png"));
			}
        });
        
        onlineExampleProjectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (initialProjectContent == IKieProjectWizardPage.ONLINE_EXAMPLE_PROJECT)
					return;
				initialProjectContent = IKieProjectWizardPage.ONLINE_EXAMPLE_PROJECT;
				emptyProjectButton.setImage(Activator.getImage("icons/wizards/empty-project.png"));
		        sampleFilesProjectButton.setImage(Activator.getImage("icons/wizards/sample-files-project.png"));
		        onlineExampleProjectButton.setImage(Activator.getImage("icons/wizards/online-example-project-pushed.png"));
			}
        });
        
        setErrorMessage(null);
        setMessage(null);
        setControl(composite);
	}

	@Override
	public boolean isPageComplete() {
		return true;
	}

	@Override
	public IWizardPage getNextPage() {
		return getProjectContentPage();
	}

	public int getInitialProjectContent() {
		return initialProjectContent;
	}

	@Override
	public Collection<IProjectDescription> getNewProjectDescriptions() {
		IKieProjectWizardPage page = (IKieProjectWizardPage) getProjectContentPage();
		return page.getNewProjectDescriptions();
	}

	public IRuntime getRuntime() {
		if (initialProjectContent==IKieProjectWizardPage.EMPTY_PROJECT) {
			IKieEmptyProjectWizardPage page = (IKieEmptyProjectWizardPage) getWizard().getPage(AbstractKieProjectWizard.EMPTY_PROJECT_PAGE);
			return page.getRuntime();
		}
		else if (initialProjectContent==IKieProjectWizardPage.SAMPLE_FILES_PROJECT) {
			IKieSampleFilesProjectWizardPage page = (IKieSampleFilesProjectWizardPage) getWizard().getPage(AbstractKieProjectWizard.SAMPLE_FILES_PROJECT_PAGE);
			return page.getRuntime();
		}
		else if (initialProjectContent==IKieProjectWizardPage.ONLINE_EXAMPLE_PROJECT) {
			IKieOnlineExampleProjectWizardPage page = (IKieOnlineExampleProjectWizardPage) getWizard().getPage(AbstractKieProjectWizard.ONLINE_EXAMPLE_PROJECT_PAGE);
			return page.getRuntime();
		}
		return null;
	}

	@Override
	public IRuntimeManager getRuntimeManager() {
		if (initialProjectContent==IKieProjectWizardPage.EMPTY_PROJECT) {
			IKieEmptyProjectWizardPage page = (IKieEmptyProjectWizardPage) getWizard().getPage(AbstractKieProjectWizard.EMPTY_PROJECT_PAGE);
			return page.getRuntimeManager();
		}
		else if (initialProjectContent==IKieProjectWizardPage.SAMPLE_FILES_PROJECT) {
			IKieSampleFilesProjectWizardPage page = (IKieSampleFilesProjectWizardPage) getWizard().getPage(AbstractKieProjectWizard.SAMPLE_FILES_PROJECT_PAGE);
			return page.getRuntimeManager();
		}
		else if (initialProjectContent==IKieProjectWizardPage.ONLINE_EXAMPLE_PROJECT) {
			IKieOnlineExampleProjectWizardPage page = (IKieOnlineExampleProjectWizardPage) getWizard().getPage(AbstractKieProjectWizard.ONLINE_EXAMPLE_PROJECT_PAGE);
			return page.getRuntimeManager();
		}
		return null;
	}
	
	private IWizardPage getProjectContentPage() {
		if (initialProjectContent==IKieProjectWizardPage.EMPTY_PROJECT)
			return getWizard().getPage(AbstractKieProjectWizard.EMPTY_PROJECT_PAGE);
		if (initialProjectContent==IKieProjectWizardPage.SAMPLE_FILES_PROJECT)
			return getWizard().getPage(AbstractKieProjectWizard.SAMPLE_FILES_PROJECT_PAGE);
		if (initialProjectContent==IKieProjectWizardPage.ONLINE_EXAMPLE_PROJECT)
			return getWizard().getPage(AbstractKieProjectWizard.ONLINE_EXAMPLE_PROJECT_PAGE);
		return null;
	}
}
