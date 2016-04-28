/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.eclipse.runtime;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.kie.eclipse.utils.FileUtils;

public class DefaultRuntimeInstaller extends AbstractRuntimeInstaller {

	public static DefaultRuntimeInstaller INSTANCE = new DefaultRuntimeInstaller();

	public DefaultRuntimeInstaller() {
	}

	@Override
	public IRuntime install(IRuntimeManager runtimeManager, String runtimeId, final IProject project, IProgressMonitor monitor) {
		final IRuntime runtime = runtimeManager.createNewRuntime();
		String version = AbstractRuntime.getVersionFromId(runtimeId);
		runtime.setName(getRuntimeName());
		runtime.setVersion(version);
		runtime.setPath(project.getLocation().toString());
		// runtime.setJars(jarsCreated.toArray(new String[jarsCreated.size()]));

		int totalWork = 2 * getRepositories().size();
		SubMonitor subMonitor = SubMonitor.convert(monitor, totalWork);
		for (Repository repo : getRepositories()) {
			java.io.File jarFile = null;
			try {
				URL url = new URL(repo.getUrl());
				jarFile = FileUtils.downloadFile(url, subMonitor.newChild(1));
				if (jarFile==null) {
					return null;
				}
				// build file include/exclude lists
				List<String> includes = new ArrayList<String>();
				List<String> excludes = new ArrayList<String>();
				ArtifactList artifactList = repo.getArtifactList();
				for (Artifact artifact : artifactList.getArtifacts()) {
					if (artifact.getInclude()!=null)
						includes.add(artifact.getInclude());
					if (artifact.getExclude()!=null)
						excludes.add(artifact.getExclude());
				}

				int fileCount = FileUtils.extractJarFile(jarFile,
						includes.toArray(new String[includes.size()]),
						excludes.toArray(new String[excludes.size()]),
						project, subMonitor.newChild(1));
				// if no files were extracted, return failure
				if (fileCount==0)
					return null;
				
				runtimeManager.recognizeJars(runtime);
				// the recognizer doesn't do a very good job of recognizing the
				// version number and product, but we already know this from the
				// installer extension point definition.
				runtime.setVersion(version);
				runtime.setProduct(product);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (CoreException e) {
				e.printStackTrace();
			}
			finally {
				try {
					if (jarFile!=null && jarFile.exists())
						jarFile.delete();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		
		return runtime;
	}

	public static Collection<? extends IRuntimeInstaller> getInstallers() {
		return FACTORY.createInstallers();
	}
}
