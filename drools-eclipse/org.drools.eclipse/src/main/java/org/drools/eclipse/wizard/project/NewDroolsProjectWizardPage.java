/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.eclipse.wizard.project;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.util.DroolsRuntime;
import org.drools.eclipse.util.FileUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.expression.IMatchExpression;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.progress.IProgressService;
import org.osgi.framework.ServiceReference;

public class NewDroolsProjectWizardPage extends WizardNewProjectCreationPage {

	public final static int EMPTY_PROJECT = 0;
	public final static int ONLINE_EXAMPLE_PROJECT = 1;
	public final static int SAMPLE_FILES_PROJECT = 2;
	private final static String DEFAULT_REPOSITORY_ID = "org.drools.sample.project.jbpm-playground";
	private final static String DROOLS_SAMPLE_PROJECTS_REPOSITORY = "org.drools.eclipse.sampleProjectsRepository";
	private final static String DEFAULT_REPOSITORY_URL = "https://raw.githubusercontent.com/bbrodt/jbpm-playground/master/site/";
	private final Collection<IInstallableUnit> EMPTY_IU_LIST = new ArrayList<IInstallableUnit>();
	
	private NewDroolsProjectRuntimeWizardPage runtimePage;
	
    private Button addSampleJavaRuleCodeButton;
    private Button addSampleRuleButton;
    private Button addSampleJavaDecisionTableCodeButton;
    private Button addSampleDecisionTableCodeButton;
    private Button addSampleRuleFlowButton;
    private Button addSampleJavaRuleFlowCodeButton;
    
    private Composite onlineExampleProjectGroup;
    private Button onlineExampleProjectButton;
    private Composite emptyProjectGroup;
    private Composite sampleFilesProjectGroup;
    private Button emptyProjectButton;
    private Button sampleFilesProjectButton;
    private Composite browserGroup;
    
	IQueryResult<IInstallableUnit> queryResult;
	
    private boolean addSampleJavaRuleCode = true;
    private boolean addSampleRule = true;
    private boolean addSampleJavaDecisionTableCode = false;
    private boolean addSampleDecisionTableCode = false;
    private boolean addSampleJavaRuleFlowCode = false;
    private boolean addSampleRuleFlow = false;
    private int initialProjectContent = EMPTY_PROJECT;
	private IUTreeViewer onlineExamplesTree;
    private Collection<IInstallableUnit> installableUnits;
    private Browser browser;
    private ServiceReference<?> providerRef;
    private String repositoryUrl;
    
    public NewDroolsProjectWizardPage(String pageName) {
        super(pageName);
        setTitle("New Drools Project");
        setDescription("Create a new Drools Project");
    }

    public void createControl(Composite parent) {
		runtimePage = (NewDroolsProjectRuntimeWizardPage) getWizard().getPage(NewDroolsProjectWizard.RUNTIME_PAGE);
		
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setFont(parent.getFont());
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        createControls(composite);
        // Show description on opening
        setErrorMessage(null);
        setMessage(null);
        setControl(composite);
    }

    private void showGroup(Composite group, boolean show) {
    	if (group!=null && !group.isDisposed()) {
	    	GridData gd = (GridData)group.getLayoutData();
	    	gd.exclude = !show;
	    	group.setVisible(show);
	    	if (show) {
		        // we may have to resize the shell to fit all the controls
				Point oldSize = getShell().getSize();
				Point newSize = getShell().computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
				if (oldSize.x<newSize.x || oldSize.y<newSize.y) {
					newSize.x = Math.max(oldSize.x, newSize.x);
					newSize.y = Math.max(oldSize.y, newSize.y);
			        getShell().setSize(newSize);
				}
	    	}
	    	group.getParent().layout();
    	}
    }
    
    private void createControls(final Composite parent) {
        GridData gd;
        Label projectContentsLabel = new Label(parent, SWT.NONE);
        projectContentsLabel.setText("Select the initial contents for this new Project:");
        projectContentsLabel.setLayoutData(new GridData());
        
        Composite buttonGroup = new Composite(parent, SWT.NONE);
        buttonGroup.setLayoutData(new GridData());
        buttonGroup.setLayout(new GridLayout(2,false));
        
        emptyProjectButton = new Button(buttonGroup, SWT.RADIO| SWT.LEFT);
        emptyProjectButton.setText("Create an empty Project");
        gd = new GridData(GridData.BEGINNING);
        gd.horizontalIndent = 10;
        gd.horizontalSpan = 2;
        emptyProjectButton.setLayoutData(gd);

        sampleFilesProjectButton = new Button(buttonGroup, SWT.RADIO| SWT.LEFT);
        sampleFilesProjectButton.setText("Populate the Project with sample files");
        gd = new GridData(GridData.BEGINNING);
        gd.horizontalIndent = 10;
        gd.horizontalSpan = 2;
        sampleFilesProjectButton.setLayoutData(gd);
        
        onlineExampleProjectButton = new Button(buttonGroup, SWT.RADIO| SWT.LEFT);
        onlineExampleProjectButton.setText("Download a sample Project from:");
        gd = new GridData(GridData.BEGINNING);
        gd.horizontalIndent = 10;
        onlineExampleProjectButton.setLayoutData(gd);
        
        final Combo repositoryCombo = new Combo(buttonGroup, SWT.DROP_DOWN | SWT.READ_ONLY);
        gd = new GridData(GridData.END);
        gd.horizontalIndent = 10;
        repositoryCombo.setLayoutData(gd);
        repositoryCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = repositoryCombo.getSelectionIndex();
				if (index>=0) {
					repositoryUrl = (String) repositoryCombo.getData(Integer.toString(index));
					if (onlineExampleProjectButton.getSelection()) {
						handleOnlineExampleProjectSelected();
					}
				}
			}
        });
        fillRepositoryCombo(repositoryCombo);
    	
        emptyProjectGroup = new Composite(parent, SWT.NONE);
        emptyProjectGroup.setLayout(new GridLayout());
        emptyProjectGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
    	
        sampleFilesProjectGroup = new Composite(parent, SWT.NONE);
        sampleFilesProjectGroup.setLayout(new GridLayout());
        gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
        sampleFilesProjectGroup.setLayoutData(gd);
    	
        onlineExampleProjectGroup = new Composite(parent, SWT.NONE);
        onlineExampleProjectGroup.setLayout(new GridLayout());
        gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
        onlineExampleProjectGroup.setLayoutData(gd);
        
        emptyProjectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (initialProjectContent == EMPTY_PROJECT)
					return;
				if (emptyProjectButton.getSelection()) {
					initialProjectContent = EMPTY_PROJECT;
					installableUnits = null;
			        showGroup(browserGroup, false);
			        showGroup(onlineExampleProjectGroup, false);
			        showGroup(sampleFilesProjectGroup, false);
			        showGroup(emptyProjectGroup, true);
					setPageComplete(isPageComplete());
				}
			}
        });
        
        sampleFilesProjectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (initialProjectContent == SAMPLE_FILES_PROJECT)
					return;
				if (sampleFilesProjectButton.getSelection()) {
					initialProjectContent = SAMPLE_FILES_PROJECT;
					installableUnits = null;
			        showGroup(browserGroup, false);
			        showGroup(onlineExampleProjectGroup, false);
			        showGroup(emptyProjectGroup, true);
			        showGroup(sampleFilesProjectGroup, true);
					setPageComplete(isPageComplete());
				}
			}
        });
        
        onlineExampleProjectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (initialProjectContent == ONLINE_EXAMPLE_PROJECT)
					return;
				if (onlineExampleProjectButton.getSelection()) {
					// delay the creation of these controls until this radio button
					// is selected because this may cause a delay as we fetch the
					// sample projects update site.
					// Note that the createOnlineExampleProjectControls() method
					// is aware that it may be called more than once.
					handleOnlineExampleProjectSelected();
				}
			}
        });
        
        showGroup(emptyProjectGroup, true);
        showGroup(sampleFilesProjectGroup, false);
        showGroup(onlineExampleProjectGroup, false);
        emptyProjectButton.setSelection(true);
        
        createEmptyProjectControls(emptyProjectGroup);
        createSampleFilesProjectControls(sampleFilesProjectGroup);
    }

    private void fillRepositoryCombo(Combo repositoryCombo) {
    	repositoryCombo.removeAll();
        try {
            IConfigurationElement[] config = Platform.getExtensionRegistry()
                    .getConfigurationElementsFor(DROOLS_SAMPLE_PROJECTS_REPOSITORY);
            int selectionIndex = 0;
            int index = 0;
            for (IConfigurationElement e : config) {
            	String id = e.getAttribute("id");
            	String name = e.getAttribute("name");
            	String url = e.getAttribute("url");
                repositoryCombo.add(name);
                repositoryCombo.setData(Integer.toString(index), url);
                if (DEFAULT_REPOSITORY_ID.equals(id))
                	selectionIndex = index;
                ++index;
            }
            repositoryCombo.select(selectionIndex);
			repositoryUrl = (String) repositoryCombo.getData(Integer.toString(selectionIndex));
            
        } catch (Exception ex) {
			MessageDialog.openError(getShell(),
					"Error",
					ex.getMessage());
        }
    }
    
    private String getRepositoryUrl() {
    	if (repositoryUrl!=null)
    		return repositoryUrl;
    	return DEFAULT_REPOSITORY_URL;
    }
    
    private void createEmptyProjectControls(Composite parent) {
    	super.createControl(parent);
    }
    
    private void createSampleFilesProjectControls(Composite parent) {

        addSampleRuleButton = createCheckBox(parent,
            "Add a sample HelloWorld rule file to this project.");
        addSampleRuleButton.setSelection(addSampleRule);
        addSampleRuleButton.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                addSampleRule = ((Button) e.widget).getSelection();
            }
        });
        addSampleJavaRuleCodeButton = createCheckBox(parent,
            "Add a sample Java class for loading and executing the HelloWorld rules.");
        addSampleJavaRuleCodeButton.setSelection(addSampleJavaRuleCode);
        addSampleJavaRuleCodeButton.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                addSampleJavaRuleCode = ((Button) e.widget).getSelection();
            }
        });
        addSampleDecisionTableCodeButton = createCheckBox(parent,
            "Add a sample HelloWorld decision table file to this project.");
        addSampleDecisionTableCodeButton.setSelection(addSampleDecisionTableCode);
        addSampleDecisionTableCodeButton.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                addSampleDecisionTableCode = ((Button) e.widget).getSelection();
            }
        });
        addSampleJavaDecisionTableCodeButton = createCheckBox(parent,
            "Add a sample Java class for loading and executing the HelloWorld decision table.");
        addSampleJavaDecisionTableCodeButton.setSelection(addSampleDecisionTableCode);
        addSampleJavaDecisionTableCodeButton.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                addSampleJavaDecisionTableCode = ((Button) e.widget).getSelection();
            }
        });
        addSampleRuleFlowButton = createCheckBox(parent,
            "Add a sample HelloWorld process file to this project.");
        addSampleRuleFlowButton.setSelection(addSampleRuleFlow);
        addSampleRuleFlowButton.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                addSampleRuleFlow = ((Button) e.widget).getSelection();
            }
        });
        addSampleJavaRuleFlowCodeButton = createCheckBox(parent,
            "Add a sample Java class for loading and executing the HelloWorld process.");
        addSampleJavaRuleFlowCodeButton.setSelection(addSampleJavaRuleFlowCode);
        addSampleJavaRuleFlowCodeButton.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                addSampleJavaRuleFlowCode = ((Button) e.widget).getSelection();
            }
        });
    }
    
    private Button createCheckBox(Composite group, String label) {
        Button button = new Button(group, SWT.CHECK | SWT.LEFT);
        button.setText(label);
        GridData data = new GridData();
        button.setLayoutData(data);
        return button;
    }
    
    private String createOnlineExampleProjectControls(final Composite parent) {
    	final String[] status = new String[1];
		IProgressService ps = PlatformUI.getWorkbench().getProgressService();
		try {
			ps.busyCursorWhile(new IRunnableWithProgress() {
				public void run(IProgressMonitor pm) {
					try {
						IProvisioningAgent agent = createProvisiongAgent();
						IMetadataRepositoryManager manager = (IMetadataRepositoryManager) agent
								.getService(IMetadataRepositoryManager.SERVICE_NAME);
						IMetadataRepository repository = manager.loadRepository(
								new URI(getRepositoryUrl()),
								pm);
						queryResult = repository.query(QueryUtil.createIUAnyQuery(), pm);
					} catch (Exception ex) {
						disposeOnlineExampleProjectControls();
						status[0] = ex.getMessage();
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			status[0] = e.getMessage();
		}
		
		if (onlineExamplesTree == null) {
			onlineExamplesTree = new IUTreeViewer(parent, SWT.BORDER);
			onlineExamplesTree.initialize();
			
			browserGroup = new Composite(parent, SWT.BORDER);
			browserGroup.setLayout(new GridLayout());
			browserGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			browser = new Browser(browserGroup, SWT.NONE);
			browser.setLayoutData(new GridData(GridData.FILL_BOTH));
			
			onlineExamplesTree.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					IInstallableUnit iu = (IInstallableUnit) e.data;
					
					boolean browserLoaded = true;
					try {
						IMatchExpression<IInstallableUnit> f = iu.getFilter();
//						for (Object p : f.getParameters()) {
//							// we can use this to identify the required Drools Runtime version:
//							// for example, setting the "Architecture" field in feature.xml
//							// (in the Overview page) to "Drools 6.0.x" produces this:
//							//           (osgi.arch=Drools 6.x)
//							System.out.println(p);
//						}
//						System.out.println("IU Properties:");
//						for (Entry<String, String> pe : iu.getProperties().entrySet()) {
//							System.out.println("    "+pe.getKey()+"="+pe.getValue());
//						}
//						for (ITouchpointData tp : iu.getTouchpointData()) {
//							System.out.println("IU Touchpoints:");
//							for (Entry<String, ITouchpointInstruction> tpe : tp.getInstructions().entrySet()) {
//								System.out.println("    "+tpe.getKey()+"="+tpe.getValue());
//							}
//						}
						String text = iu.getProperty(IInstallableUnit.PROP_DESCRIPTION, "df_LT");
						String url = iu.getProperty(IInstallableUnit.PROP_DESCRIPTION_URL);
						if (text != null && !text.isEmpty()) {
							browser.setText(text);
						}
						else if (testURL(url)) {
							browser.setUrl(url);
						}
						else {
							browser.setText("<html><body>No description is available for <b>"
									+ iu.getProperty(IInstallableUnit.PROP_NAME)
									+ "</b></body></html>");
						}
					} catch (Exception ex) {
						browserLoaded = false;
						MessageDialog.openError(getShell(),
								"Error",
								"Cannot initialize Browser");
					}
					
					showGroup(browserGroup, browserLoaded);
					onlineExamplesTree.getTree().pack();
					parent.layout();
					
					installableUnits = onlineExamplesTree.getSelectedIUs();
					setPageComplete(isPageComplete());
				}
			});
		}
		
		onlineExamplesTree.setInput(queryResult);
		showGroup(browserGroup, false);
		browser.setText("");
		
		if (onlineExamplesTree.getTree().getItemCount() == 0) {
			disposeOnlineExampleProjectControls();
			return "No examples were found in the repository.";
		}

		return null;
    }

	private void handleOnlineExampleProjectSelected() {
		String error = createOnlineExampleProjectControls(onlineExampleProjectGroup);
		if (error==null) {
			initialProjectContent = ONLINE_EXAMPLE_PROJECT;
	        showGroup(emptyProjectGroup, false);
	        showGroup(browserGroup, false);
	        showGroup(onlineExampleProjectGroup, true);
	        showGroup(sampleFilesProjectGroup, false);
			installableUnits = onlineExamplesTree.getSelectedIUs();
		}
		else {
			MessageDialog.openError(getShell(),
					"Error",
					NLS.bind("Unable to load online examples from\n{0}\nCause:\n{1}",
							getRepositoryUrl(),
							error)
			);
			onlineExampleProjectButton.setSelection(false);
			if (initialProjectContent == EMPTY_PROJECT) {
				emptyProjectButton.setSelection(true);
			}
			else {
				sampleFilesProjectButton.setSelection(true);
			}
			installableUnits = null;
		}
		setPageComplete(isPageComplete());
	}
	
	private void disposeOnlineExampleProjectControls() {
		if (onlineExamplesTree!=null) {
			if (!onlineExamplesTree.getTree().isDisposed())
				onlineExamplesTree.dispose();
			onlineExamplesTree = null;
		}
		if (browser!=null) {
			if (!browser.isDisposed())
				browser.dispose();
			browser = null;
		}
	}
	
    public String downloadOnlineExampleProject(IProject project, IProgressMonitor monitor) {
    	String projectName = project.getName();
		try {
			for (IInstallableUnit iu : getInstallableUnits()) {
				if (projectName.equals(iu.getId())) {
					for (IArtifactKey k : iu.getArtifacts()) {
						String filename = k.getId() + "_" + k.getVersion() + ".jar";
						URL url = new URL(getRepositoryUrl() + "/plugins/" + filename);
						java.io.File jarFile = FileUtils.downloadFile(url, monitor);
						FileUtils.extractJarFile(jarFile, project, monitor);
						// delete this temporary file
						jarFile.delete();
						project.refreshLocal(IProject.DEPTH_INFINITE, monitor);
					}
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
			return e.getMessage();
		}
		return null;
    }
    
	/**
     * Check if a given URL is accessible and if the page exists.
     * 
     * @param urlString
     * @return
     */
    private boolean testURL(String urlString) {
    	boolean result = true;
		HttpURLConnection httpConnection = null;
		try {
			URL url = new URL(urlString);
			httpConnection = (HttpURLConnection) url.openConnection();
			httpConnection.getContent();
			httpConnection.disconnect();
			httpConnection = null;
		} catch (UnknownHostException ex) {
			result = false;
		} catch (IOException ex) {
			result = false;
		} finally {
			// cleanup
			if (httpConnection != null)
				httpConnection.disconnect();
		}
		return result;
    }

    public int getInitialProjectContent() {
    	return initialProjectContent;
    }
    
    public boolean createRuleFile() {
        return addSampleRule;
    }

    public boolean createJavaRuleFile() {
        return addSampleJavaRuleCode;
    }

    public boolean createDecisionTableFile() {
        return addSampleDecisionTableCode;
    }

    public boolean createJavaDecisionTableFile() {
        return addSampleJavaDecisionTableCode;
    }

    public boolean createRuleFlowFile() {
        return addSampleRuleFlow;
    }

    public boolean createJavaRuleFlowFile() {
        return addSampleJavaRuleFlowCode;
    }

    public Collection<IInstallableUnit> getInstallableUnits() {
    	if (installableUnits==null)
    		return EMPTY_IU_LIST;
    	return installableUnits;
    }
    
    public Collection<IProjectDescription> getNewProjectDescriptions() {
    	Collection<IProjectDescription> result = new ArrayList<IProjectDescription> ();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
    	if (initialProjectContent == ONLINE_EXAMPLE_PROJECT) {
    		for (IInstallableUnit iu : getInstallableUnits()) {
    			String name = iu.getId();
                IProject project = getProjectHandle(name);
                IProjectDescription description = workspace.newProjectDescription(project.getName());
                result.add(description);
    		}
    	}
    	else {
            IProject project = getProjectHandle();
            IPath newPath = useDefaults() ? null : getLocationPath();
            IProjectDescription description = workspace.newProjectDescription(project.getName());
            description.setLocation(newPath);
            result.add(description);
    	}
    	return result;
    }
    
    private IProject getProjectHandle(String name) {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
    }

    public DroolsRuntime getDroolsRuntime() {
    	return runtimePage.getDroolsRuntime();
    }
    
    public void setPageComplete(boolean complete) {
    	super.setPageComplete(complete);
    	if (runtimePage!=null)
    		runtimePage.setPageComplete(getInitialProjectContent()==ONLINE_EXAMPLE_PROJECT);
    }
    
	@Override
	public boolean isPageComplete() {
		if (getInitialProjectContent()==ONLINE_EXAMPLE_PROJECT)
			return getInstallableUnits().size()>0;
		return super.validatePage();
	}

	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible)
			setPageComplete(isPageComplete());
	}

	@Override
	public boolean canFlipToNextPage() {
		if (getInitialProjectContent()==ONLINE_EXAMPLE_PROJECT)
			return false;
		return super.canFlipToNextPage();
	}

	@Override
	public void dispose() {
		if (providerRef!=null) {
			DroolsEclipsePlugin.getContext().ungetService(providerRef);
			providerRef = null;
		}
		super.dispose();
	}

	private IProvisioningAgent createProvisiongAgent() throws ProvisionException {
		IProvisioningAgent result = null;
		providerRef = DroolsEclipsePlugin.getContext().getServiceReference(IProvisioningAgentProvider.SERVICE_NAME);
		if (providerRef == null) {
			throw new RuntimeException("No provisioning agent provider is available"); //$NON-NLS-1$
		}
		IProvisioningAgentProvider provider = (IProvisioningAgentProvider) DroolsEclipsePlugin.getContext().getService(providerRef);
		if (provider == null) {
			throw new RuntimeException("No provisioning agent provider is available"); //$NON-NLS-1$
		}
		
		// obtain agent for currently running system
		result = provider.createAgent(null);

		return result;
	}
}
