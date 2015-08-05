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

package org.kie.eclipse.server;

import java.io.File;
import java.net.URI;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.lib.StoredConfig;
import org.kie.eclipse.IKieConstants;
import org.kie.eclipse.utils.GitUtils;
import org.kie.eclipse.utils.PreferencesUtils;

/**
 *
 */
public class KieRepositoryHandler extends KieResourceHandler implements IKieRepositoryHandler, IKieConstants {

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
	
	@Override
	public Object load() {
		if (repository == null) {
			try {
				final File repoRoot = new File(PreferencesUtils.getRepoRoot(this));
				final Set<File> gitDirs = new HashSet<File>();
				GitUtils.findGitDirsRecursive(repoRoot, gitDirs, false);
				for (File dir : gitDirs) {
					if (getName().equals(dir.getParentFile().getName())) {
						Git git = Git.open(dir);
						Repository repository = git.getRepository();
						StoredConfig storedConfig = repository.getConfig();
						Set<String> remotes = storedConfig.getSubsections("remote");
						for (String remoteName : remotes) {
							try {
								String url = storedConfig.getString("remote", remoteName, "url");
								URI uri = new URI(url);
								int port = uri.getPort();
								String host = uri.getHost();
								String scheme = uri.getScheme();
								String path[] = uri.getPath().split("/");
								String repoName = path[path.length-1];
								if (	name.equals(repoName) &&
										host.equals(getServer().getHost()) &&
										port == getDelegate().getGitPort() &&
										"ssh".equals(scheme)) {
									this.repository = repository;
									break;
								}
							}
							catch (Exception e) {
								e.printStackTrace();
							}
							finally {
								if (git!=null) {
									git.close();
									git = null;
								}
							}
						}
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		return repository;
	}

	@Override
	public void dispose() {
		super.dispose();
		if (repository!=null) {
			repository.close();
			repository = null;
		}
	}

	@Override
	public boolean isLoaded() {
		return repository != null;
	}
	
	@Override
	public Repository getRepository() {
		return repository;
	}
}
