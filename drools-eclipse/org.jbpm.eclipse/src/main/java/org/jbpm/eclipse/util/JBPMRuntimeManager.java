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

package org.jbpm.eclipse.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.drools.eclipse.builder.DroolsBuilder;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.jbpm.eclipse.JBPMEclipsePlugin;
import org.jbpm.eclipse.preferences.JBPMConstants;
import org.kie.eclipse.runtime.AbstractRuntimeManager;
import org.kie.eclipse.runtime.DefaultRuntimeRecognizer;
import org.kie.eclipse.runtime.IRuntime;
import org.kie.eclipse.runtime.IRuntimeRecognizer;

public class JBPMRuntimeManager extends AbstractRuntimeManager {

	private static final String RUNTIME_RECOGNIZER = "org.jbpm.eclipse.runtimeRecognizer";

    private static JBPMRuntimeManager manager;

    public static JBPMRuntimeManager getDefault() {
    	if( manager == null )
    		manager = new JBPMRuntimeManager();
    	return manager;
    }
    
	@Override
	public String getBundleRuntimeLocation() {
    	return ".jbpm.runtime";
	}
	
	@Override
	public String getRuntimePreferenceKey() {
		return JBPMConstants.JBPM_RUNTIMES;
	}
	
	@Override
	public String getSettingsFilename() {
		return ".jbpm.runtime";
	}
	@Override
	public String getBundleSymbolicName() {
		return "org.jbpm.eclipse";
	}
	@Override
	public IRuntime createNewRuntime() {
		return new JBPMRuntime();
	}
	@Override
	public void logException(Throwable t) {
		JBPMEclipsePlugin.log(t);
	}
	
	@Override
	public IPreferenceStore getPreferenceStore() {
		return JBPMEclipsePlugin.getDefault().getPreferenceStore();
	}
	
	@Override
	public void addBuilder(IJavaProject project, IProgressMonitor monitor) throws JavaModelException, CoreException {
		if (!JBPMClasspathContainer.hasJBPMClassPath(project)) {
			IClasspathContainer cp = new JBPMClasspathContainer(project);
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
    
	public String getRuntimeRecognizerId() {
		return RUNTIME_RECOGNIZER;
	}
}
