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

import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.egit.core.RepositoryCache;
import org.eclipse.jgit.lib.Repository;

/**
 *
 */
public class KieProjectHandler extends KieResourceHandler implements IKieProjectHandler {

	IProject project;
	
	/**
	 * @param repository
	 * @param string
	 */
	public KieProjectHandler(IKieRepositoryHandler repository, String name) {
		super(repository, name);
	}

	@Override
	public Object getResource() {
		return project;
	}

	public Object load() {
		if (project==null) {
			Repository repository = (Repository) parent.load();
			if (repository!=null) {
				RepositoryCache repositoryCache = org.eclipse.egit.core.Activator.getDefault().getRepositoryCache();
				
				for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
					if (project.getName().equals(name)) {
						Repository projectRepo = repositoryCache.getRepository(project);
						if (repository==projectRepo) {
							this.project = project;
							break;
						}
					}
				}
			}
		}
		return project;
	}
	
	@Override
	public boolean isLoaded() {
		return project!=null;
	}
	
	public List<? extends IKieResourceHandler> getChildren() throws Exception {
		return null;
	}
}
