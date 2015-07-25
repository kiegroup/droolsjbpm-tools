/*******************************************************************************
 * Copyright (c) 2011, 2012, 2013, 2014 Red Hat, Inc.
 *  All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 *
 * @author Bob Brodt
 ******************************************************************************/

package org.kie.eclipse.navigator.view.server;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.egit.core.RepositoryCache;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jgit.events.ConfigChangedEvent;
import org.eclipse.jgit.events.ConfigChangedListener;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.RepositoryCache.FileKey;
import org.eclipse.jgit.lib.StoredConfig;
import org.eclipse.jgit.util.FS;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;
import org.kie.eclipse.navigator.IKieNavigatorConstants;
import org.kie.eclipse.navigator.preferences.PreferencesUtils;

/**
 *
 */
@SuppressWarnings("restriction")
public class KieRepositoryHandler extends KieResourceHandler implements IKieRepositoryHandler, ConfigChangedListener, IKieNavigatorConstants {

	static RepositoryCache repositoryCache = org.eclipse.egit.core.Activator.getDefault().getRepositoryCache();
	Repository repository;
	
	/**
	 * @param organization
	 * @param string
	 */
	public KieRepositoryHandler(IKieOrganizationHandler organization, String name) {
		super(organization, name);
	}

	public KieRepositoryHandler(IKieServerHandler server, String name) {
		super(server, name);
	}

	@Override
	public Object getResource() {
		return repository;
	}

	@Override
	public List<? extends IKieResourceHandler> getChildren() throws Exception {
		if (children==null || children.isEmpty()) {
			children = getDelegate().getProjects(this);
		}
		return children;
	}
	
	@Override
	public List<IKieProjectHandler> getProjects() throws Exception {
		return (List<IKieProjectHandler>) getChildren();
	}
	
	public Object load() {
		if (repository == null) {
			final File repoRoot = new File(PreferencesUtils.getRepoRoot(this));
			final Set<File> gitDirs = new HashSet<File>();
			final IRunnableWithProgress runnable = new IRunnableWithProgress() {

				@Override
				public void run(IProgressMonitor monitor)
						throws InvocationTargetException, InterruptedException {
					monitor.beginTask("Searching for Repositories",IProgressMonitor.UNKNOWN);
					try {
						findGitDirsRecursive(repoRoot, gitDirs, monitor, false);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
					if (monitor.isCanceled()) {
						throw new InterruptedException();
					}
				}
			};
			IProgressService ps = PlatformUI.getWorkbench().getProgressService();
			try {
				ps.busyCursorWhile(runnable);
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			
			for (File dir : gitDirs) {
				if (getName().equals(dir.getParentFile().getName())) {
					try {
						Repository repository = repositoryCache.lookupRepository(dir);
						StoredConfig storedConfig = repository.getConfig();
						Set<String> remotes = storedConfig.getSubsections("remote");
						for (String remoteName : remotes) {
							String url = storedConfig.getString("remote", remoteName, "url");
							System.out.println(repository.getDirectory());
							System.out.println(url);
							try {
								URI u = new URI(url);
								int port = u.getPort();
								String host = u.getHost();
								String scheme = u.getScheme();
								String path[] = u.getPath().split("/");
								String repoName = path[path.length-1];
								if (	name.equals(repoName) &&
										host.equals(getServer().getHost()) &&
										port == getDelegate().getGitPort() &&
										"ssh".equals(scheme)) {
									this.repository = repository;
									break;
								}
							} catch (URISyntaxException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
			if (repository!=null)
				// TODO: why doesn't this work?
				repository.getListenerList().addListener(ConfigChangedListener.class, this);

		}
		return repository;
	}
	
	
	private void findGitDirsRecursive(File repoRoot, Set<File> gitDirs,
			IProgressMonitor monitor, boolean lookForNestedRepositories) {

		if (!repoRoot.exists() || !repoRoot.isDirectory()) {
			return;
		}
		File[] children = repoRoot.listFiles();
		
		// simply ignore null
		if (children == null)
			return;

		for (File child : children) {
			if (monitor.isCanceled())
				return;
			if (!child.isDirectory())
				continue;

			if (FileKey.isGitRepository(child, FS.DETECTED)) {
				gitDirs.add(child);
			}
			else if (FileKey.isGitRepository(new File(child,
					Constants.DOT_GIT), FS.DETECTED)) {
				gitDirs.add(new File(child, Constants.DOT_GIT));
			}
			else if (lookForNestedRepositories) {
				monitor.subTask(child.getPath());
				findGitDirsRecursive(child, gitDirs, monitor, lookForNestedRepositories);
			}
		}
	}

	@Override
	public boolean isLoaded() {
		return repository != null;
	}
	
	public Repository getRepository() {
		return repository;
	}

	@Override
	public void dispose() {
		if (repository!=null) {
//			repository.getListenerList()
		}
		super.dispose();
	}

	@Override
	public void onConfigChanged(ConfigChangedEvent event) {
		// TODO: why doesn't this work?
		System.out.println("onConfigChanged: repository="+repository.getDirectory().getName());
	}
}
