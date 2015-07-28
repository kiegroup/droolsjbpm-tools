package org.kie.eclipse.navigator.view.utils;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import org.drools.eclipse.util.DroolsRuntimeManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.egit.core.EclipseGitProgressTransformer;
import org.eclipse.egit.core.RepositoryUtil;
import org.eclipse.egit.core.op.ConnectProviderOperation;
import org.eclipse.egit.core.project.RepositoryFinder;
import org.eclipse.egit.core.project.RepositoryMapping;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.window.Window.IExceptionHandler;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.TransportGitSsh;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.util.FS;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.kie.eclipse.navigator.view.actions.repository.KieCredentialsProvider;
import org.kie.eclipse.navigator.view.content.ProjectNode;
import org.kie.eclipse.navigator.view.content.RepositoryNode;
import org.kie.eclipse.runtime.IRuntime;
import org.kie.eclipse.runtime.IRuntimeManager;
import org.kie.eclipse.server.IKieRepositoryHandler;
import org.kie.eclipse.server.IKieServerHandler;
import org.kie.eclipse.server.IKieServiceDelegate;
import org.kie.eclipse.server.KieRepositoryHandler;
import org.kie.eclipse.utils.FileUtils;
import org.kie.eclipse.utils.PreferencesUtils;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class ActionUtils {

	public static IRuntimeManager runtimeManager = DroolsRuntimeManager.getDefault();
	private ActionUtils() {
	}

	public static void importRepository(IExceptionHandler exceptionHandler, RepositoryNode container) {
		final IKieRepositoryHandler handler = (IKieRepositoryHandler) container.getHandler();
		final IKieServerHandler server = (IKieServerHandler) handler.getRoot();
		final IKieServiceDelegate delegate = container.getHandler().getDelegate();
		try {
			String host = delegate.getServer().getHost();
			int port = delegate.getGitPort();
			String username = delegate.getUsername();
			String password = delegate.getPassword();
			URIish uri = PreferencesUtils.getRepoURI(host, port, username, handler.getName());
			// LoginDialog dlg = new LoginDialog(getShell(), uri);
			// dlg.setUsername(username);
			// dlg.setPassword(password);
			// if (dlg.open() != Dialog.OK)
			// return;
			// username = dlg.getUsername();
			// password = dlg.getPassword();
			uri = PreferencesUtils.getRepoURI(host, port, username, handler.getName());
			final String localPath = PreferencesUtils.getRepoPath(handler);
			final String remotePath = uri.toString();
			final CredentialsProvider credentialsProvider = new KieCredentialsProvider(server, username, password);
			Job job = new Job("Import Repository") {
				@Override
				protected IStatus run(final IProgressMonitor monitor) {
					Repository localRepo = null;
					try {
						EclipseGitProgressTransformer gitMonitor = new EclipseGitProgressTransformer(monitor);
						localRepo = new FileRepository(localPath + File.separator + ".git");
						CloneCommand cloneCmd = Git.cloneRepository().setURI(remotePath).setDirectory(new File(localPath))
								.setProgressMonitor(gitMonitor).setCloneAllBranches(true).setTimeout(60)
								.setCredentialsProvider(credentialsProvider)
								.setTransportConfigCallback(new TransportConfigCallback() {
									@Override
									public void configure(Transport transport) {
										TransportGitSsh gt = (TransportGitSsh) transport;
										gt.setSshSessionFactory(new JschConfigSessionFactory() {
											@Override
											protected Session createSession(Host hc, String user, String host, int port, FS fs)
													throws JSchException {
												Session session = super.createSession(hc, user, host, port, fs);
												session.setConfig("PreferredAuthentications", "password");
												return session;
											}

											@Override
											protected void configure(Host hc, Session session) {
												session.setConfig("PreferredAuthentications", "password");
											}
										});
									}
								});

						cloneCmd.call();
						localRepo.close();

						RepositoryUtil util = org.eclipse.egit.ui.Activator.getDefault().getRepositoryUtil();
						util.addConfiguredRepository(localRepo.getDirectory());
						localRepo = null;
					}
					catch (Exception e) {
						exceptionHandler.handleException(e);
					}
					finally {
						if (localRepo != null)
							localRepo.close();
					}
					return Status.OK_STATUS;
				}
			};
			job.setUser(true);
			job.schedule();
		}
		catch (Exception e) {
			exceptionHandler.handleException(e);
		}
	}

	public static IJavaProject importProject(ProjectNode projectNode, IExceptionHandler exceptionHandler) {
		final String projectName = projectNode.getName();
		final AtomicReference<IJavaProject> ar = new AtomicReference<IJavaProject>();
		IWorkspaceRunnable wsr = new IWorkspaceRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					IJavaProject javaProject = createOrOpenProject(projectNode, projectName, monitor);
					if (javaProject != null) {
						ar.set(javaProject);
						IProject project = javaProject.getProject();
						Map<IProject, File> projectsToConnect = new HashMap<IProject, File>();
						RepositoryFinder finder = new RepositoryFinder(project);
						finder.setFindInChildren(false);
						Collection<RepositoryMapping> mappings = finder.find(new SubProgressMonitor(monitor, 1));
						if (!mappings.isEmpty()) {
							RepositoryMapping mapping = mappings.iterator().next();
							projectsToConnect.put(project, mapping.getGitDirAbsolutePath().toFile());
							ConnectProviderOperation connect = new ConnectProviderOperation(projectsToConnect);
							connect.execute(monitor);
						}
					}
				}
				catch (final Exception e1) {
					Display.getDefault().asyncExec(new Runnable() {
						@Override
						public void run() {
							exceptionHandler.handleException(e1);
						}
					});
				}
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(wsr, null);
		}
		catch (Exception e) {
			exceptionHandler.handleException(e);
		}
		return ar.get();
	}

	public static IJavaProject createOrOpenProject(ProjectNode projectNode, String projectName, IProgressMonitor monitor)
			throws CoreException, IllegalArgumentException {
		final AtomicReference<IJavaProject> ar = new AtomicReference<IJavaProject>();
		final IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IProject project = workspace.getRoot().getProject(projectName);
		final RepositoryNode repoNode = (RepositoryNode) projectNode.getContainer();
		Repository repository = ((KieRepositoryHandler) repoNode.getHandler()).getRepository();
		final IPath location = new Path(repository.getWorkTree().toString()).append(projectName);
		
		if (project.exists()) {
			if (!project.isOpen()) {
				IPath oldLocation = project.getFile(IProjectDescription.DESCRIPTION_FILE_NAME).getLocation();
				if (oldLocation != null && oldLocation.equals(location)) {
					project.open(monitor);
					project.refreshLocal(IResource.DEPTH_INFINITE, monitor);
					return null;
				}
			}
			throw new IllegalArgumentException("The Project " + projectName + " already exists");
		}
		
        WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
            protected void execute(IProgressMonitor monitor)
                    throws CoreException {
                try {
            		IProjectDescription pd = workspace.newProjectDescription(projectName);
            		pd.setLocation(location);
            		// add the natures and builders
                	FileUtils.addJavaNature(pd);
                	FileUtils.addMavenNature(pd);
            		project.create(pd, new SubProgressMonitor(monitor, 30));
            		project.open(IResource.BACKGROUND_REFRESH, new SubProgressMonitor(monitor, 50));
            		
                    // create a default runtime if one does not exist
                    IRuntime runtime = runtimeManager.getEffectiveRuntime(null, true);
                    runtimeManager.setRuntime(runtime, project, monitor);
                    
                    IJavaProject javaProject = JavaCore.create(project);
                    FileUtils.createOutputLocation(javaProject, "bin", monitor);
                    
                	FileUtils.addJavaBuilder(javaProject, monitor);
                	FileUtils.addMavenBuilder(javaProject, monitor);
                	runtimeManager.addBuilder(javaProject, monitor);
                	
                	FileUtils.addFolderToClasspath(javaProject, "src/main/java", true, monitor);
                	FileUtils.addFolderToClasspath(javaProject, "src/main/resources", true, monitor);
                	FileUtils.addFolderToClasspath(javaProject, "src/main/rules", true, monitor);
					ar.set(javaProject);


                	FileUtils.createFolder(javaProject, "src/main/resources/META-INF", monitor);
                   	FileUtils.createFolder(javaProject, "src/main/resources/META-INF/maven", monitor);
                   	
                   	JsonObject projectProperties = projectNode.getHandler().getProperties();
                   	JsonValue jv = projectProperties.get("groupId");
                   	String groupId = null;
                   	if (jv==null || jv.asString().isEmpty()) {
                   		if (projectNode.getParent() instanceof RepositoryNode) {
	                       	JsonObject orgProperties = projectNode.getParent().getParent().getHandler().getProperties();
	                   		jv = orgProperties.get("defaultGroupId");
                   		}
                   	}
                   	if (jv!=null)
                   		groupId = jv.asString();

					String artifactId = null;
					jv = projectProperties.get("artifactId");
					if (jv != null)
						artifactId = jv.asString();

                   	String version = null;
					jv = projectProperties.get("version");
                   	if (jv!=null)
                   		version = jv.asString();
                   	FileUtils.createMavenArtifacts(javaProject, groupId, artifactId, version, monitor);
                   	FileUtils.createKJarArtifacts(javaProject, monitor);
                   	
                    project.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
                    
                } catch (Exception e) {
                    ErrorDialog.openError(Display.getDefault().getActiveShell(),
                    	"Problem creating new project",
                        e.getMessage(), null);
                }
            }
        };
        try {
        	op.run(monitor);
        } catch (Throwable t) {
            t.printStackTrace();
        }

        return ar.get();

	}

	public static void createProjectArtifacts(IJavaProject javaProject, String groupId, String artifactId, String version, IProgressMonitor monitor)
			throws CoreException, IllegalArgumentException {
		
        WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
            protected void execute(IProgressMonitor monitor)
                    throws CoreException {
                try {
                	FileUtils.createFolder(javaProject, "src/main/resources/META-INF", monitor);
                   	FileUtils.createFolder(javaProject, "src/main/resources/META-INF/maven", monitor);
                   	
                   	FileUtils.createMavenArtifacts(javaProject, groupId, artifactId, version, monitor);
                   	FileUtils.createKJarArtifacts(javaProject, monitor);
                   	
                    javaProject.getProject().build(IncrementalProjectBuilder.FULL_BUILD, monitor);
                } catch (Exception e) {
                    ErrorDialog.openError(Display.getDefault().getActiveShell(),
                    	"Problem creating project artifacts",
                        e.getMessage(), null);
                }
            }
        };
        try {
        	op.run(monitor);
        } catch (Throwable t) {
            t.printStackTrace();
        }
	}
}
