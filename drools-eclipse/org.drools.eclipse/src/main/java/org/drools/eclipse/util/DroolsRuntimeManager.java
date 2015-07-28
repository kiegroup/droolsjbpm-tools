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

package org.drools.eclipse.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.builder.DroolsBuilder;
import org.drools.eclipse.preferences.IDroolsConstants;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.kie.eclipse.runtime.AbstractRuntimeManager;
import org.kie.eclipse.runtime.IRuntime;

public class DroolsRuntimeManager extends AbstractRuntimeManager {

	/**
	 * This is the "hidden" Eclipse Workspace Project name that will hold a copy
	 * of the Drools Runtime that is packaged with this plugin; currently just a
	 * simple project with a "lib" folder containing all of the required Drools
	 * Runtime jars. If the user has not yet created a default Runtime, this
	 * project will be created, populated and used as the default.
	 */
	private static final String DROOLS_BUNDLE_RUNTIME_LOCATION = ".drools.runtime";

	/**
	 * This is the name of a file contained in the workspace project's
	 * ".settings" folder used to store the selected runtime name.
	 */
	private static final String DROOLS_SETTINGS_FILENAME = ".drools.runtime";

	/**
	 * Name of this plugin.
	 */
	private static final String DROOLS_BUNDLE_NAME = "org.drools.eclipse";

	private static DroolsRuntimeManager manager;

	public static DroolsRuntimeManager getDefault() {
		if (manager == null)
			manager = new DroolsRuntimeManager();
		return manager;
	}

	@Override
	public String getBundleRuntimeLocation() {
		return DROOLS_BUNDLE_RUNTIME_LOCATION;
	}

	@Override
	public String getRuntimePreferenceKey() {
		return IDroolsConstants.DROOLS_RUNTIMES;
	}

	@Override
	public boolean isMavenized(IRuntime runtime) {
		return runtime != null && runtime.getVersion() != null && runtime.getVersion().startsWith("6");
	}

	@Override
	public String getSettingsFilename() {
		return DROOLS_SETTINGS_FILENAME;
	}

	@Override
	public String getBundleSymbolicName() {
		return DROOLS_BUNDLE_NAME;
	}

	@Override
	public IRuntime createNewRuntime() {
		return new DroolsRuntime();
	}

	@Override
	public void logException(Throwable t) {
		DroolsEclipsePlugin.log(t);
	}

	@Override
	public IPreferenceStore getPreferenceStore() {
		return DroolsEclipsePlugin.getDefault().getPreferenceStore();
	}

	@Override
	public void addBuilder(IJavaProject project, IProgressMonitor monitor) throws JavaModelException, CoreException {
		if (!DroolsClasspathContainer.hasDroolsClasspath(project)) {
			IClasspathContainer cp = new DroolsClasspathContainer(project);
	        JavaCore.setClasspathContainer(cp.getPath(),
	                new IJavaProject[] { project },
	                new IClasspathContainer[] { cp }, monitor);
			List<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
			list.addAll(Arrays.asList(project.getRawClasspath()));
			list.add(JavaCore.newContainerEntry(cp.getPath()));
			project.setRawClasspath((IClasspathEntry[]) list.toArray(new IClasspathEntry[list.size()]), monitor);
		}
		
        IProjectDescription description = project.getProject().getDescription();
        ICommand[] commands = description.getBuildSpec();
        for (ICommand cmd : commands) {
        	if (cmd.getBuilderName().equals(DroolsBuilder.BUILDER_ID))
        		return;
        }
        ICommand[] newCommands = new ICommand[commands.length + 1];
        System.arraycopy(commands, 0, newCommands, 0, commands.length);
        
        ICommand droolsCommand = description.newCommand();
        droolsCommand.setBuilderName(DroolsBuilder.BUILDER_ID);
        newCommands[commands.length] = droolsCommand;
        
        description.setBuildSpec(newCommands);
        project.getProject().setDescription(description, monitor);
		
	}
}
