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

package org.jbpm.eclipse.wizard.project;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.jbpm.eclipse.JBPMEclipsePlugin;
import org.jbpm.eclipse.util.JBPMClasspathContainer;
import org.kie.eclipse.wizard.project.AbstractKieProjectMainWizardPage;
import org.kie.eclipse.wizard.project.AbstractKieProjectRuntimeWizardPage;
import org.kie.eclipse.wizard.project.AbstractKieProjectWizard;

/**
 * A wizard to create a new jBPM project.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class NewJBPMProjectWizard extends AbstractKieProjectWizard {

    public static final String JBPM_CLASSPATH_CONTAINER_PATH = "JBPM/jbpm";
    public static final String JUNIT_CLASSPATH_CONTAINER_PATH = "org.eclipse.jdt.junit.JUNIT_CONTAINER/4";
    public static final String DROOLS_BUILDER_ID = "org.drools.eclipse.droolsbuilder";
    
    private NewJBPMProjectWizardPage mainPage;
    private NewJBPMProjectRuntimeWizardPage runtimePage;

    protected void initializeDefaultPageImageDescriptor() {
        ImageDescriptor desc = JBPMEclipsePlugin.getImageDescriptor("icons/jbpm-large.png");
        setDefaultPageImageDescriptor(desc);
    }

    protected void createOutputLocation(IJavaProject project, IProgressMonitor monitor)
            throws JavaModelException, CoreException {
        IFolder folder = project.getProject().getFolder("bin");
        createFolder(folder, monitor);
        IPath path = folder.getFullPath();
        project.setOutputLocation(path, null);
    }

    protected void addBuilders(IJavaProject project, IProgressMonitor monitor) throws CoreException {
    	super.addBuilders(project, monitor);
    	addJBPMBuilder(project, monitor);
    }

    private void addJBPMBuilder(IJavaProject project, IProgressMonitor monitor) throws CoreException {
        IProjectDescription description = project.getProject().getDescription();
        ICommand[] commands = description.getBuildSpec();
        ICommand[] newCommands = new ICommand[commands.length + 1];
        System.arraycopy(commands, 0, newCommands, 0, commands.length);
        
        ICommand droolsCommand = description.newCommand();
        droolsCommand.setBuilderName(DROOLS_BUILDER_ID);
        newCommands[commands.length] = droolsCommand;
        
        description.setBuildSpec(newCommands);
        project.getProject().setDescription(description, monitor);
    }

	protected IClasspathContainer createClasspathContainer(IJavaProject project) {
		return new JBPMClasspathContainer(project, getJbpmClassPathContainerPath());
	}

    protected void setClasspath(IJavaProject project, IProgressMonitor monitor)
            throws JavaModelException, CoreException {
        project.setRawClasspath(new IClasspathEntry[0], monitor);
        addSourceFolders(project, monitor);
        addJRELibraries(project, monitor);
        addJBPMLibraries(project, monitor);
        if (mainPage.createJUnitFile()) {
        	addJUnitLibrary(project, monitor);
        }
    }

    private static IPath getJbpmClassPathContainerPath() {
        return new Path(JBPM_CLASSPATH_CONTAINER_PATH);
    }

    private static IPath getJUnitClassPathContainerPath() {
        return new Path(JUNIT_CLASSPATH_CONTAINER_PATH);
    }

    private static void createJBPMLibraryContainer(IJavaProject project, IProgressMonitor monitor)
            throws JavaModelException {
        JavaCore.setClasspathContainer(getJbpmClassPathContainerPath(),
            new IJavaProject[] { project },
            new IClasspathContainer[] { new JBPMClasspathContainer(
                    project, getJbpmClassPathContainerPath()) }, monitor);
    }

    public static void addJBPMLibraries(IJavaProject project, IProgressMonitor monitor)
            throws JavaModelException {
        createJBPMLibraryContainer(project, monitor);
        List<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
        list.addAll(Arrays.asList(project.getRawClasspath()));
        list.add(JavaCore.newContainerEntry(getJbpmClassPathContainerPath()));
        project.setRawClasspath((IClasspathEntry[]) list
            .toArray(new IClasspathEntry[list.size()]), monitor);
    }

    public static void addJUnitLibrary(IJavaProject project, IProgressMonitor monitor)
    		throws JavaModelException {
		createJBPMLibraryContainer(project, monitor);
		List<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
		list.addAll(Arrays.asList(project.getRawClasspath()));
		list.add(JavaCore.newContainerEntry(getJUnitClassPathContainerPath()));
		project.setRawClasspath((IClasspathEntry[]) list
		    .toArray(new IClasspathEntry[list.size()]), monitor);
    }

    protected boolean createInitialContent(IJavaProject javaProject, IProgressMonitor monitor)
            throws CoreException, JavaModelException, IOException {
    	if (!super.createInitialContent(javaProject, monitor)) {
        	if (mainPage.getInitialProjectContent() == AbstractKieProjectMainWizardPage.SAMPLE_FILES_PROJECT) {
		    	try {
		    		String exampleType = mainPage.getExampleType();
		    		createProcess(javaProject, monitor, exampleType);
			    	if (mainPage.createJUnitFile()) {
			    		createProcessSampleJUnit(javaProject, exampleType, monitor);
			    	}
		    	} catch (Throwable t) {
		    		t.printStackTrace();
		    	}
        	}
    	}
    	return true;
	}

    /**
     * Create the sample process file.
     */
    private void createProcess(IJavaProject project, IProgressMonitor monitor, String exampleType) throws CoreException {
	    String fileName = "org/jbpm/eclipse/wizard/project/" + exampleType + ".bpmn.template";
        IFolder folder = project.getProject().getFolder("src/main/resources");
        IFile file = folder.getFile("sample.bpmn");
        InputStream inputstream = getClass().getClassLoader().getResourceAsStream(fileName);
        if (!file.exists()) {
            file.create(inputstream, true, monitor);
        } else {
            file.setContents(inputstream, true, false, monitor);
        }
    }

    /**
     * Create the sample process junit test file.
     */
    private void createProcessSampleJUnit(IJavaProject project, String exampleType, IProgressMonitor monitor)
            throws JavaModelException, IOException {
    	String s = "org/jbpm/eclipse/wizard/project/ProcessJUnit-" + exampleType + ".java";
    	String generationType = runtimePage.getVersion();
        if (NewJBPMProjectRuntimeWizardPage.JBPM5.equals(generationType)) {        
        	s += ".v5.template";
        } else {
        	s += ".template";
        }
        IFolder folder = project.getProject().getFolder("src/main/java");
        IPackageFragmentRoot packageFragmentRoot = project
                .getPackageFragmentRoot(folder);
        IPackageFragment packageFragment = packageFragmentRoot
                .createPackageFragment("com.sample", true, monitor);
        InputStream inputstream = getClass().getClassLoader()
                .getResourceAsStream(s);
        packageFragment.createCompilationUnit("ProcessTest.java", new String(
                readStream(inputstream)), true, monitor);
    }

	@Override
	protected AbstractKieProjectMainWizardPage createMainPage(String pageId) {
		if (mainPage==null)
			mainPage = new NewJBPMProjectWizardPage(pageId);
        return mainPage;
	}

	@Override
	protected AbstractKieProjectRuntimeWizardPage createRuntimePage(String pageId) {
		if (runtimePage==null)
			runtimePage = new NewJBPMProjectRuntimeWizardPage(pageId);
		return runtimePage;
	}
}
