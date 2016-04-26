package org.kie.eclipse.wizard.project;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.kie.eclipse.runtime.IRuntime;
import org.kie.eclipse.runtime.IRuntimeManager;

public abstract class AbstractKieEmptyProjectWizardPage extends KieProjectWizardPage implements IKieEmptyProjectWizardPage {
    
    protected IRuntimeManager runtimeManager;
    private List<IRuntime> runtimes = new ArrayList<IRuntime>();
    private boolean isDefaultRuntime = true;
    private IRuntime selectedRuntime;
    private IRuntime effectiveRuntime;
    private Combo runtimesCombo;
    private Text pomArtifactIdText;
    private String pomGroupId;
    private String pomArtifactId;
    private String pomVersion;
    
    protected enum KieProjectBuildType {
    	JAVA_PROJECT,
    	MAVEN_PROJECT,
    };
    KieProjectBuildType projectBuildType = KieProjectBuildType.JAVA_PROJECT;

	abstract protected void createControls(Composite parent);
    abstract public IRuntimeManager getRuntimeManager();
    abstract protected IRuntime createRuntime();
    abstract public int showRuntimePreferenceDialog();
    abstract public String getProductName();
	abstract public String getProductId();
	
	public AbstractKieEmptyProjectWizardPage(String pageName) {
		super(pageName);
		this.setTitle("Create an Empty Project");
		runtimeManager = getRuntimeManager();
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		
		Composite projectTypeRadioButtons = new Composite((Composite) getControl(), SWT.NONE);
        projectTypeRadioButtons.setLayout(new GridLayout(3, false));
        projectTypeRadioButtons.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        Label projectTypeLabel = new Label(projectTypeRadioButtons, SWT.NONE);
        projectTypeLabel.setText("Build the Project using:");
        final Button javaProjectButton = createRadioButton(projectTypeRadioButtons, "Java and "+getProductName()+" Runtime classes");
        final Button mavenProjectButton = createRadioButton(projectTypeRadioButtons, "Maven");

        final Composite mavenControls = createMavenControls((Composite) getControl());
        final Composite javaControls = createJavaControls((Composite) getControl());

        mavenProjectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (((Button) e.widget).getSelection()) {
					projectBuildType = KieProjectBuildType.MAVEN_PROJECT;
					setControlVisible(javaControls, false);
					setControlVisible(mavenControls, true);
				}
			}
		});
		javaProjectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (((Button) e.widget).getSelection()) {
					projectBuildType = KieProjectBuildType.JAVA_PROJECT;
					setControlVisible(javaControls, true);
					setControlVisible(mavenControls, false);
				}
			}
		});

        // create a Maven project by default
		boolean isMavenProject = projectBuildType==KieProjectBuildType.MAVEN_PROJECT;
        mavenProjectButton.setSelection(isMavenProject);
        javaProjectButton.setSelection(!isMavenProject);
		setControlVisible(mavenControls, isMavenProject);
		setControlVisible(javaControls, !isMavenProject);

		createControls((Composite) getControl());
	}
	
	protected Composite createMavenControls(Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginLeft = 20;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        String text;
        createLabel(composite, "Group ID:");
        text = getPomGroupId();
        final Text pomGroupIdText = createText(composite, text);
        pomGroupIdText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				pomGroupId = pomGroupIdText.getText();
			}
		});
        
        createLabel(composite, "Artifact ID:");
        text = getPomArtifactId();
        pomArtifactIdText = createText(composite, text);
        pomArtifactIdText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				pomArtifactId = pomArtifactIdText.getText();
			}
		});
        
        createLabel(composite, "Version:");
        text = getPomVersion();
        final Text pomVersionText = createText(composite, text);
        pomVersionText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				pomVersion = pomVersionText.getText();
			}
		});
        
        return composite;
	}

	protected Composite createJavaControls(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(3, false);
		layout.marginLeft = 20;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        final Label runtimesLabel = new Label(composite, SWT.NONE);
        runtimesLabel.setText("Version:");
        runtimesLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1));
        
        runtimesCombo = new Combo(composite, SWT.READ_ONLY);
        runtimesCombo.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
        runtimesCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	Integer key = runtimesCombo.getSelectionIndex();
                IRuntime rt = (IRuntime) runtimesCombo.getData(key.toString());
                if (rt!=selectedRuntime) {
                	selectedRuntime = rt;
                	effectiveRuntime = null;
                }
                isDefaultRuntime = rt.isDefault();
                setPageComplete(isPageComplete());
            }
        });

		final Link changeWorkspaceSettingsLink = new Link(composite, SWT.NONE);
        changeWorkspaceSettingsLink.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false, 1, 1));
        changeWorkspaceSettingsLink.setText("<A>Manage Runtime definitions...</A>");
        changeWorkspaceSettingsLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
	        	showRuntimePreferenceDialog();
        		fillRuntimesCombo();
			}
        });
        
        fillRuntimesCombo();

        return composite;
	}
	
	@Override
	public boolean isPageComplete() {
		return super.isPageComplete() && runtimes.size() > 0;
	}
	
	@Override
    protected boolean validatePage() {
		if (pomArtifactIdText!=null) {
			pomArtifactIdText.setText(getProjectName());
		}
		return super.validatePage();
	}
	
	public String getPomGroupId() {
		if (pomGroupId==null)
			return "com.sample";
		return pomGroupId;
	}
	
	public String getPomArtifactId() {
		if (pomArtifactId==null)
			return getProjectName();
		return pomArtifactId;
	}
	
	public String getPomVersion() {
		if (pomVersion==null)
			return "1.0.0-SNAPSHOT";
		return pomVersion;
	}
		
	public boolean shouldCreateMavenProject() {
		return projectBuildType==KieProjectBuildType.MAVEN_PROJECT;
	}
	
	public boolean shouldCreateKJarProject() {
		return true;
	}

    private void fillRuntimesCombo() {
    	runtimes.clear();
    	for (IRuntime rt : runtimeManager.getConfiguredRuntimes())
    		runtimes.add(rt);

    	if (runtimeManager.getDefaultRuntime()==null) {
    		isDefaultRuntime = false;
    	}

    	if (runtimes.size()==0) {
	    	IRuntime rt = createRuntime();
	    	rt.setName(runtimeManager.getBundleRuntimeName());
	    	rt.setVersion(runtimeManager.getBundleRuntimeVersion());
	    	rt.setDefault(true);
	    	runtimes.add(rt);
	    	
	    	setControlVisible(runtimesCombo, true);
	    	isDefaultRuntime = true;
    	}
    	
        setErrorMessage(null);
        runtimesCombo.removeAll();
        Integer selectedKey = 0;
        Integer key = 0;
        for (IRuntime rt : runtimes) {
        	String name = rt.getName();
        	if (rt.getPath()==null) {
        		name += " (will be created)";
        	}
        	if (rt.isDefault())
        		selectedKey = key;
            runtimesCombo.add(name);
            runtimesCombo.setData(key.toString(), rt);
            ++key;
        }
        
        runtimesCombo.select(selectedKey);
        selectedRuntime = (IRuntime) runtimesCombo.getData(selectedKey.toString());
    }

    public boolean shouldDownloadRuntime() {
    	if (shouldCreateMavenProject()) {
    		// maven projects don't need a runtime.
    		// just return the bundle runtime
    		return false;
    	}
    	if (effectiveRuntime==null) {
        	effectiveRuntime = runtimeManager.getEffectiveRuntime(selectedRuntime, isDefaultRuntime);
			if (effectiveRuntime == null) {
				return true;
			}
    	}
    	return false;
    }
    
    public IRuntime getRuntime() {
    	if (this.shouldCreateMavenProject()) {
    		// maven projects don't need a runtime.
    		// just return the bundle runtime
    		return runtimeManager.getBundleRuntime();
    	}
    	if (effectiveRuntime==null) {
        	effectiveRuntime = runtimeManager.getEffectiveRuntime(selectedRuntime, isDefaultRuntime);
			if (effectiveRuntime == null) {
				effectiveRuntime = runtimeManager.downloadOrCreateBundleRuntime(getProgressMonitor());
        	}
    	}
    	return effectiveRuntime;
    }
    
    public boolean isDefaultRuntime() {
    	return isDefaultRuntime;
    }
    
	@Override
	public IWizardPage getNextPage() {
		return null;
	}
    
	public KieProjectBuildType getProjectBuildType() {
		return projectBuildType;
	}
}
