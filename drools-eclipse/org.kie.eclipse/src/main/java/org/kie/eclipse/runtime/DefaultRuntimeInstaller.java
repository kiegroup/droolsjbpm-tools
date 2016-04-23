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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jgit.fnmatch.FileNameMatcher;
import org.kie.eclipse.utils.FileUtils;

public class DefaultRuntimeInstaller extends AbstractRuntimeInstaller {

	public static DefaultRuntimeInstaller INSTANCE = new DefaultRuntimeInstaller();

	public DefaultRuntimeInstaller() {
	}

	@Override
	public IRuntime install(IRuntimeManager runtimeManager, String runtimeId, final IProject project, IProgressMonitor monitor) {
		FileNameMatcher fn;
		AbstractRuntimeInstaller installer = FACTORY.getInstaller(runtimeId);
		if (installer == null) {
			return null; // "No installer found for "+runtimeId;
		}
		final IRuntime runtime = runtimeManager.createNewRuntime();
		runtime.setName(getRuntimeName());
		runtime.setVersion(getVersion());
		runtime.setPath(project.getLocation().toString());
		// runtime.setJars(jarsCreated.toArray(new String[jarsCreated.size()]));

		SubMonitor subMonitor = SubMonitor.convert(monitor, getRepositories().size());
		for (Repository repo : getRepositories()) {
			try {
				URL url = new URL(repo.getUrl());
				java.io.File jarFile = FileUtils.downloadFile(url, subMonitor);
				if (jarFile==null) {
					return null;
				}
				System.out.println("Finished downloading "+jarFile.getAbsolutePath());
				FileUtils.extractJarFile(jarFile, project, subMonitor);
				System.out.println("Finished extracting "+jarFile.getName()+" to "+project.getLocation());
				runtimeManager.recognizeJars(runtime);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (CoreException e) {
				e.printStackTrace();
			}
			subMonitor.worked(1);
		}
		
		return runtime;
	}

	public static Collection<? extends IRuntimeInstaller> getInstallers() {
		return FACTORY.createInstallers();
	}
}
