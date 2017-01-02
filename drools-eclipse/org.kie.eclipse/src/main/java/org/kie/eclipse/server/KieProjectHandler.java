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
 ******************************************************************************/

package org.kie.eclipse.server;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jgit.lib.Repository;
import org.kie.eclipse.utils.GitUtils;

/**
 *
 */
public class KieProjectHandler extends KieResourceHandler implements IKieProjectHandler {

	IProject project;
	File directory;
	
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
	
	@Override
	public void setResource(Object resource) {
		if (resource==null || resource instanceof IProject) {
			project = (IProject) resource;
		}
	}

	@Override
	public Object load() {
		if (project==null) {
			Repository repository = (Repository) parent.load();
			if (repository!=null) {
				for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
					if (project.getName().equals(name)) {
						File directory = project.getLocation().toFile();
						final Set<File> gitDirs = new HashSet<File>();
						GitUtils.findGitDirsRecursive(directory.getParentFile(), gitDirs, false);
						for (File dir : gitDirs) {
							if (repository.getDirectory().equals(dir)) {
								this.project = project;
								this.directory = directory;
								break;
							}
						}
					}
					if (this.project!=null)
						break;
				}
			}
		}
		return project;
	}
	
	@Override
	public boolean isLoaded() {
		return project!=null;
	}
	
	@Override
	public void dispose() {
		super.dispose();
		project = null;
		directory = null;
	}

	@Override
	public List<? extends IKieResourceHandler> getChildren() throws Exception {
		return null;
	}
	   
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof KieProjectHandler) {
			KieProjectHandler that = (KieProjectHandler) obj;
			if (this.directory!=null) {
				if (!this.directory.equals(that.directory))
					return false;
			}
			else if (that.directory!=null)
				return false;
			
			if (this.project!=null) {
				if (!this.project.equals(that.project) || this.project.isOpen()!=that.project.isOpen())
					return false;
			}
			else if (that.project!=null) {
				return false;
			}
		}
		return super.equals(obj);
	}
		
	@Override
	public File getDirectory() {
		return directory;
	}
}
