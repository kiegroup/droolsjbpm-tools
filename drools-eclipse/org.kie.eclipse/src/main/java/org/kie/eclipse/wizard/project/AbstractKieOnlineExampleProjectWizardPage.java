package org.kie.eclipse.wizard.project;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map.Entry;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.equinox.p2.core.IProvisioningAgentProvider;
import org.eclipse.equinox.p2.core.ProvisionException;
import org.eclipse.equinox.p2.metadata.IArtifactKey;
import org.eclipse.equinox.p2.metadata.IInstallableUnit;
import org.eclipse.equinox.p2.metadata.ITouchpointData;
import org.eclipse.equinox.p2.metadata.ITouchpointInstruction;
import org.eclipse.equinox.p2.metadata.expression.IMatchExpression;
import org.eclipse.equinox.p2.query.IQueryResult;
import org.eclipse.equinox.p2.query.QueryUtil;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.kie.eclipse.Activator;
import org.kie.eclipse.utils.FileUtils;
import org.osgi.framework.ServiceReference;

public abstract class AbstractKieOnlineExampleProjectWizardPage extends WizardPage implements IKieOnlineExampleProjectWizardPage {
	private final static String DROOLS_SAMPLE_PROJECTS_REPOSITORY = "org.kie.eclipse.sampleProjectsRepository";
	private final Collection<IInstallableUnit> EMPTY_IU_LIST = new ArrayList<IInstallableUnit>();
    private Composite onlineExampleProjectGroup;
    private Composite browserGroup;
	private IQueryResult<IInstallableUnit> queryResult;
	private IUTreeViewer onlineExamplesTree;
    private Collection<IInstallableUnit> installableUnits;
    private Browser browser;
    private ServiceReference<?> providerRef;
    private String repositoryUrl;

    public abstract String getProductId();

	public AbstractKieOnlineExampleProjectWizardPage(String pageName) {
		super(pageName);
	}

	@Override
	public void createControl(Composite parent) {
        GridData gd;
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setFont(parent.getFont());
        composite.setLayout(new GridLayout(2, false));
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label label = new Label(composite, SWT.NONE);
		label.setText("Select Online Example Repository:");
		
        final Combo repositoryCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
        gd = new GridData(GridData.END);
        gd.horizontalIndent = 10;
        repositoryCombo.setLayoutData(gd);
        repositoryCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = repositoryCombo.getSelectionIndex();
				if (index>=0) {
					repositoryUrl = (String) repositoryCombo.getData(Integer.toString(index));
					handleOnlineExampleProjectSelected();
				}
			}
        });
        fillRepositoryCombo(repositoryCombo);

        onlineExampleProjectGroup = new Composite(composite, SWT.NONE);
        onlineExampleProjectGroup.setLayout(new GridLayout());
        gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
        gd.horizontalSpan = 2;
        onlineExampleProjectGroup.setLayoutData(gd);
        showGroup(onlineExampleProjectGroup, false);

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

    private void fillRepositoryCombo(Combo repositoryCombo) {
    	repositoryCombo.removeAll();
    	repositoryCombo.add("<None Selected>");

        try {
            IConfigurationElement[] config = Platform.getExtensionRegistry()
                    .getConfigurationElementsFor(DROOLS_SAMPLE_PROJECTS_REPOSITORY);
            int index = 1;
            for (IConfigurationElement e : config) {
            	String id = e.getAttribute("id");
            	String name = e.getAttribute("name");
            	String product = e.getAttribute("product");
            	String url = e.getAttribute("url");
            	String gitUrl = e.getAttribute("gitUrl");
            	
            	if (product==null || product.equals(getProductId())) {
	                if (gitUrl!=null) {
		    			try {
		    				// Get all refs from this Git repository, including
		    				// branches and tags. The branch or tag name will
		    				// replace the strings "${BRACNH}" or "$TAG}" in the
		    				// url when fetching the available sample projects.
		    	            Collection<Ref> refs;
		    				refs = Git.lsRemoteRepository()
		    				        .setHeads(true)
		    				        .setTags(true)
		    				        .setRemote(gitUrl)
		    				        .call();
	
		    				if (refs.size()>0) {
			    	            for (Ref ref : refs) {
	//		    	                System.out.println("Ref: " + ref.getName());
			    	                String a[] = ref.getName().split("/");
			    	                if (a.length==3) {
			    	                	String tagUrl = null;
			    	                	if ("heads".equals(a[1]) && url.contains("${BRANCH}")) {
			    	                		tagUrl = url.replace("${BRANCH}", a[2]);	
			    	                	}
			    	                	else if ("tags".equals(a[1]) && url.contains("${TAG}")) {
			    	                		tagUrl = url.replace("${TAG}", a[2]);	
			    	                	}
			    	                	if (tagUrl!=null) {
			    	                		repositoryCombo.add(name + " (" + a[2] + ")");
			    	                		repositoryCombo.setData(Integer.toString(index), tagUrl);
			    	                		++index;
			    	                	}
			    	                }
			    	            }
		    				}
		    				else {
		    					gitUrl = null;
		    				}
		    			} catch (InvalidRemoteException e2) {
		    				gitUrl = null;
		    				e2.printStackTrace();
		    			} catch (TransportException e2) {
		    				gitUrl = null;
		    				e2.printStackTrace();
		    			} catch (GitAPIException e2) {
		    				gitUrl = null;
		    				e2.printStackTrace();
		    			}
	                }
	                
	                if (gitUrl==null){
	                	repositoryCombo.add(name);
	                    repositoryCombo.setData(Integer.toString(index), url);
	                    ++index;
	                }
            	}
            }
            
            repositoryCombo.select(0);
			repositoryUrl = null;
            
        } catch (Exception ex) {
			MessageDialog.openError(getShell(),
					"Error",
					ex.getMessage());
        }
    }
    
    private String getRepositoryUrl() {
   		return repositoryUrl;
    }

    private String createOnlineExampleProjectControls() {
    	if (repositoryUrl==null) {
    		queryResult = null;
    		return "No Repository selected";
    	}
    	
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
						Display.getDefault().asyncExec(new Runnable() {
							@Override
							public void run() {
								disposeOnlineExampleProjectControls();
							}
						});
						status[0] = ex.getMessage();
					}
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			status[0] = e.getMessage();
		}
		if (status[0]!=null)
			return status[0];
		
		if (onlineExamplesTree == null) {
			onlineExamplesTree = new IUTreeViewer(onlineExampleProjectGroup, SWT.BORDER);
			onlineExamplesTree.initialize();
			
			browserGroup = new Composite(onlineExampleProjectGroup, SWT.BORDER);
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
						for (Object p : f.getParameters()) {
							// we can use this to identify the required Drools Runtime version:
							// for example, setting the "Architecture" field in feature.xml
							// (in the Overview page) to "Drools 6.0.x" produces this:
							//           (osgi.arch=Drools 6.x)
							System.out.println(p);
						}
						System.out.println("IU Properties:");
						for (Entry<String, String> pe : iu.getProperties().entrySet()) {
							System.out.println("    "+pe.getKey()+"="+pe.getValue());
						}
						for (ITouchpointData tp : iu.getTouchpointData()) {
							System.out.println("IU Touchpoints:");
							for (Entry<String, ITouchpointInstruction> tpe : tp.getInstructions().entrySet()) {
								System.out.println("    "+tpe.getKey()+"="+tpe.getValue());
							}
						}
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
					onlineExampleProjectGroup.layout();
					
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
		String error = createOnlineExampleProjectControls();
		if (error==null) {
	        showGroup(browserGroup, false);
	        showGroup(onlineExampleProjectGroup, true);
			installableUnits = onlineExamplesTree.getSelectedIUs();
		}
		else {
	        showGroup(browserGroup, false);
	        showGroup(onlineExampleProjectGroup, false);
			if (repositoryUrl!=null) {
				MessageDialog.openError(getShell(),
						"Error",
						NLS.bind("Unable to load online examples from\n{0}\nCause:\n{1}",
								getRepositoryUrl(),
								error)
				);
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

    public Collection<IInstallableUnit> getInstallableUnits() {
    	if (installableUnits==null)
    		return EMPTY_IU_LIST;
    	return installableUnits;
    }

	@Override
	public void dispose() {
		if (providerRef!=null) {
			Activator.getContext().ungetService(providerRef);
			providerRef = null;
		}
		super.dispose();
	}

	private IProvisioningAgent createProvisiongAgent() throws ProvisionException {
		IProvisioningAgent result = null;
		providerRef = Activator.getContext().getServiceReference(IProvisioningAgentProvider.SERVICE_NAME);
		if (providerRef == null) {
			throw new RuntimeException("No provisioning agent provider is available"); //$NON-NLS-1$
		}
		IProvisioningAgentProvider provider = (IProvisioningAgentProvider) Activator.getContext().getService(providerRef);
		if (provider == null) {
			throw new RuntimeException("No provisioning agent provider is available"); //$NON-NLS-1$
		}
		
		// obtain agent for currently running system
		result = provider.createAgent(null);

		return result;
	}
    
    public Collection<IProjectDescription> getNewProjectDescriptions() {
    	Collection<IProjectDescription> result = new ArrayList<IProjectDescription> ();
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
		for (IInstallableUnit iu : getInstallableUnits()) {
			String name = iu.getId();
            IProject project = getProjectHandle(name);
            IProjectDescription description = workspace.newProjectDescription(project.getName());
            result.add(description);
		}
    	return result;
    }
    
    private IProject getProjectHandle(String name) {
        return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
    }
    
	@Override
	public boolean isPageComplete() {
		return getInstallableUnits().size()>0;
	}

	@Override
	public IWizardPage getNextPage() {
		return ((AbstractKieProjectWizard)getWizard()).getLastPage();
	}
}
