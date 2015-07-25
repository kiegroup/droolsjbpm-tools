package org.kie.eclipse.wizard.project;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.kie.eclipse.runtime.IRuntime;
import org.kie.eclipse.runtime.IRuntimeManager;

public abstract class AbstractKieEmptyProjectWizardPage extends KieProjectWizardPage implements IKieEmptyProjectWizardPage {
    
    protected IRuntimeManager runtimeManager;
    private List<IRuntime> runtimes = new ArrayList<IRuntime>();
    private boolean isDefaultRuntime = true;
    private IRuntime selectedRuntime;
    private IRuntime effectiveRuntime;
    private Button projectSpecificRuntime;
    private Combo runtimesCombo;
    
	private boolean createMavenProject = true;
	private boolean createKJarProject = true;

	abstract protected void createControls(Composite parent);
    abstract public IRuntimeManager getRuntimeManager();
    abstract protected IRuntime createRuntime();
    abstract public int showRuntimePreferenceDialog();
	
	public AbstractKieEmptyProjectWizardPage(String pageName) {
		super(pageName);
		this.setTitle("Create an Empty Project");
		runtimeManager = getRuntimeManager();
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		createRuntimeControls((Composite) getControl());
		createKJarControls((Composite) getControl());
		createControls((Composite) getControl());
	}
	
	protected Composite createRuntimeControls(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(3, false));
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        projectSpecificRuntime = new Button(composite, SWT.CHECK);
        projectSpecificRuntime.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 3, 1));
        projectSpecificRuntime.setSelection(isDefaultRuntime);
        projectSpecificRuntime.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                isDefaultRuntime = ((Button) e.widget).getSelection();
                runtimesCombo.setEnabled(!isDefaultRuntime);
            }
        });
        
        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1));
        nameLabel.setText("Use Runtime:");
        
        runtimesCombo = new Combo(composite, SWT.READ_ONLY);
        runtimesCombo.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false, 1, 1));
        runtimesCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	Integer key = runtimesCombo.getSelectionIndex();
                IRuntime rt = (IRuntime) runtimesCombo.getData(key.toString());
                if (rt!=selectedRuntime) {
                	selectedRuntime = rt;
                	effectiveRuntime = null;
//                	mavenPanel.setVisible(runtimeManager.isMavenized(selectedRuntime));
                }
                setPageComplete(isPageComplete());
            }
        });
        
        final Link changeWorkspaceSettingsLink = new Link(composite, SWT.NONE);
        changeWorkspaceSettingsLink.setLayoutData(new GridData(GridData.END, GridData.CENTER, true, false, 1, 1));
        changeWorkspaceSettingsLink.setText("<A>Change Workspace Settings...</A>");
        changeWorkspaceSettingsLink.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
	        	if (showRuntimePreferenceDialog() == Window.OK)
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
    
	protected Composite createKJarControls(Composite parent) {
    	GridData gd;
        final Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout(1, false));
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalAlignment = GridData.FILL;
        gd.horizontalSpan = 2;
        gd.verticalIndent = 10;
        gd.horizontalIndent = 16;
        composite.setLayoutData(gd);

        final Button createMavenProjectButton = new Button(composite, SWT.CHECK);
        createMavenProjectButton.setText("Create as Maven Project");
		createMavenProjectButton.setToolTipText("Generates a default Maven \"Project Object Model\" (POM) File");
        createMavenProjectButton.setSelection(createMavenProject);
        
        final Button createKJarProjectButton = new Button(composite, SWT.CHECK);
        createKJarProjectButton.setText("Create as KJar Project");
        createKJarProjectButton.setToolTipText("Generates a default KJar Module Descriptor as well as the necessary Maven artifacts");
        createKJarProjectButton.setSelection(createKJarProject);

        createMavenProjectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				createMavenProject = createMavenProjectButton.getSelection();
			}
        });
        createKJarProjectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				createKJarProject = createKJarProjectButton.getSelection();
				if (createKJarProject) {
					createMavenProjectButton.setSelection(true);
					createMavenProjectButton.setEnabled(false);
				}
				else
					createMavenProjectButton.setEnabled(true);
			}
        });
        
        return composite;
	}
	
	public boolean shouldCreateMavenProject() {
		return createMavenProject;
	}
	
	public boolean shouldCreateKJarProject() {
		return createKJarProject;
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
    	runtimesCombo.setEnabled(!isDefaultRuntime);
    	
        setErrorMessage(null);
        runtimesCombo.removeAll();
        Integer key = 0;
        for (IRuntime rt : runtimes) {
        	String name = rt.getName();
        	if (rt.getPath()==null) {
        		name += " (will be created)";
        	}
            runtimesCombo.add(name);
            runtimesCombo.setData(key.toString(), rt);
            ++key;
        }
        
        key = 0; 
        runtimesCombo.select(key);
        selectedRuntime = (IRuntime) runtimesCombo.getData(key.toString());

        IRuntime defaultRuntime = runtimeManager.getDefaultRuntime();
        if (defaultRuntime==null) {
        	if (runtimes.size()==1)
        		defaultRuntime = runtimes.get(0);
        }
        projectSpecificRuntime.setText("Use default Runtime (" +
        		(defaultRuntime == null ? "undefined)" : defaultRuntime.getName() + ")"));
        projectSpecificRuntime.setEnabled(isDefaultRuntime);
    }

    public IRuntime getRuntime() {
    	if (effectiveRuntime==null)
        	effectiveRuntime = runtimeManager.getEffectiveRuntime(selectedRuntime, isDefaultRuntime);
    	return effectiveRuntime;
    }
    
    public boolean isDefaultRuntime() {
    	return isDefaultRuntime;
    }
    
	@Override
	public IWizardPage getNextPage() {
		return ((AbstractKieProjectWizard)getWizard()).getLastPage();
	}
}
