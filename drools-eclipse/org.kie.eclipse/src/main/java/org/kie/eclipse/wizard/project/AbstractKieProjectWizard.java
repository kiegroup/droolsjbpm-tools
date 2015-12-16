/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

package org.kie.eclipse.wizard.project;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.kie.eclipse.runtime.IRuntime;
import org.kie.eclipse.utils.FileUtils;

/**
 * A wizard to create a new Drools project.
 */
public abstract class AbstractKieProjectWizard extends BasicNewResourceWizard {

    public static final String START_PAGE = "NewProjectStartPage";
    public static final String EMPTY_PROJECT_PAGE = "NewEmptProjectPage";
    public static final String SAMPLE_FILES_PROJECT_PAGE = "NewSampleFilesProjectPage";
    public static final String ONLINE_EXAMPLE_PROJECT_PAGE = "NewOnlineExampleProjectPage";
    public static final String MAIN_PAGE = "NewProjectMainPage";
    public static final String RUNTIME_PAGE = "NewProjectRuntimePage";
    
    protected IKieProjectStartWizardPage startPage;
    protected IKieEmptyProjectWizardPage emptyProjectPage;
    protected IKieSampleFilesProjectWizardPage sampleFilesProjectPage;
    protected IKieOnlineExampleProjectWizardPage onlineExampleProjectPage;
    
    abstract protected IKieProjectWizardPage createStartPage(String pageId);
    abstract protected IKieProjectWizardPage createEmptyProjectPage(String pageId);
    abstract protected IKieProjectWizardPage createSampleFilesProjectPage(String pageId);
    abstract protected IKieProjectWizardPage createOnlineExampleProjectPage(String pageId);

    abstract protected void createMavenArtifacts(IJavaProject project, IProgressMonitor monitor);
    abstract protected void createKJarArtifacts(IJavaProject project, IProgressMonitor monitor);
    abstract protected void createOutputLocation(IJavaProject project, IProgressMonitor monitor) throws JavaModelException, CoreException;
    
    public void addPages() {
        super.addPages();
        startPage = (IKieProjectStartWizardPage) createStartPage(START_PAGE);
        addPage(startPage);
        emptyProjectPage = (IKieEmptyProjectWizardPage) createEmptyProjectPage(EMPTY_PROJECT_PAGE);
        addPage(emptyProjectPage);
        sampleFilesProjectPage = (IKieSampleFilesProjectWizardPage) createSampleFilesProjectPage(SAMPLE_FILES_PROJECT_PAGE);
        addPage(sampleFilesProjectPage);
        onlineExampleProjectPage = (IKieOnlineExampleProjectWizardPage) createOnlineExampleProjectPage(ONLINE_EXAMPLE_PROJECT_PAGE);
        addPage(onlineExampleProjectPage);

        setNeedsProgressMonitor(true);
    }

	public boolean performFinish() {
    	IProject newProjectHandle = null;
    	for (IProjectDescription pd : startPage.getNewProjectDescriptions()) {
   			newProjectHandle = createNewProject(pd);
   			initializeNewProject(newProjectHandle);
    	}
        if (newProjectHandle == null) {
            return false;
        }
        selectAndReveal(newProjectHandle);
        return true;
    }
    
    protected IProject initializeNewProject(final IProject newProjectHandle) {
         WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
            protected void execute(IProgressMonitor monitor)
                    throws CoreException {
                try {
                    IJavaProject project = JavaCore.create(newProjectHandle);
                    createRuntimeSettings(project, monitor);
                    createOutputLocation(project, monitor);
                    setClasspath(project, monitor);
                    addBuilders(project, monitor);
                    createInitialContent(project, monitor);
                    newProjectHandle.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
                } catch (IOException _ex) {
                    ErrorDialog.openError(getShell(), "Problem creating new project",
                        null, null);
                }
            }
        };
        try {
            getContainer().run(true, true, op);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        
        return newProjectHandle;
    }
    
    protected IProject createNewProject(final IProjectDescription description) {
        addNatures(description);
        final IProject newProjectHandle = FileUtils.getProjectHandle(description.getName());
        // create the new project operation
        WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
            protected void execute(IProgressMonitor monitor)
                    throws CoreException {
                createProject(description, newProjectHandle, monitor);
            }
        };

        // run the new project creation operation
        try {
            getContainer().run(true, true, op);
        } catch (InterruptedException e) {
            return null;
        } catch (InvocationTargetException e) {
            Throwable t = e.getTargetException();
            if (t instanceof CoreException) {
                if (((CoreException) t).getStatus().getCode() == IResourceStatus.CASE_VARIANT_EXISTS) {
                    MessageDialog.openError(getShell(),
                        "NewProject.errorMessage",
                        "NewProject.caseVariantExistsError"
                                + newProjectHandle.getName());
                } else {
                    ErrorDialog.openError(getShell(),
                        "NewProject.errorMessage", null, // no special message
                        ((CoreException) t).getStatus());
                }
            } else {
                e.printStackTrace();
            }
            return null;
        }

        return newProjectHandle;
    }
    
    protected void addNatures(IProjectDescription projectDescription) {
    	FileUtils.addJavaNature(projectDescription);
    	boolean shouldAddMavenNature = false;
    	if (startPage.getInitialProjectContent()==IKieProjectWizardPage.EMPTY_PROJECT)
    		shouldAddMavenNature = emptyProjectPage.shouldCreateMavenProject();
    	else if (startPage.getInitialProjectContent()==IKieProjectWizardPage.SAMPLE_FILES_PROJECT) 
    		shouldAddMavenNature = sampleFilesProjectPage.shouldCreateMavenProject();
    	if (shouldAddMavenNature) {
    		FileUtils.addMavenNature(projectDescription);
    	}
    }
    
    protected void createProject(IProjectDescription description, IProject projectHandle, IProgressMonitor monitor)
            throws CoreException, OperationCanceledException {
        try {
            monitor.beginTask("", 2000);
            projectHandle.create(description, new SubProgressMonitor(monitor,
                    1000));
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }
            projectHandle.open(IResource.BACKGROUND_REFRESH,
                new SubProgressMonitor(monitor, 1000));
        } finally {
            monitor.done();
        }
    }
    
    protected void createRuntimeSettings(IJavaProject javaProject, IProgressMonitor monitor) throws CoreException {
        IRuntime runtime = startPage.getRuntime();
        if (runtime != null) {
        	boolean isDefaultRuntime = false;
        	if (startPage.getInitialProjectContent()==IKieProjectWizardPage.EMPTY_PROJECT)
        		isDefaultRuntime = emptyProjectPage.isDefaultRuntime();
        	else if (startPage.getInitialProjectContent()==IKieProjectWizardPage.SAMPLE_FILES_PROJECT) 
        		isDefaultRuntime = sampleFilesProjectPage.isDefaultRuntime();
        	if (!isDefaultRuntime) {
        		startPage.getRuntimeManager().setRuntime(runtime, javaProject.getProject(), monitor);
        	}
        }
    }


    protected void createOutputLocation(IJavaProject project, String folderName, IProgressMonitor monitor)
            throws JavaModelException, CoreException {
        IFolder folder = FileUtils.createFolder(project, folderName, monitor);
        IPath path = folder.getFullPath();
        project.setOutputLocation(path, null);
    }

    protected void addBuilders(IJavaProject project, IProgressMonitor monitor) throws CoreException {
    	FileUtils.addJavaBuilder(project, monitor);
    	boolean shouldAddMavenBuilder = false;
    	if (startPage.getInitialProjectContent()==IKieProjectWizardPage.EMPTY_PROJECT)
    		shouldAddMavenBuilder = emptyProjectPage.shouldCreateMavenProject();
    	else if (startPage.getInitialProjectContent()==IKieProjectWizardPage.SAMPLE_FILES_PROJECT) 
    		shouldAddMavenBuilder = sampleFilesProjectPage.shouldCreateMavenProject();
    	if (shouldAddMavenBuilder) {
    		FileUtils.addMavenBuilder(project, monitor);
    	}
    	else
    		startPage.getRuntimeManager().addBuilder(project, monitor);
    }
    
    protected void setClasspath(IJavaProject project, IProgressMonitor monitor)
            throws JavaModelException, CoreException {
        project.setRawClasspath(new IClasspathEntry[0], monitor);
        addSourceFolders(project, monitor);
        FileUtils.addJRELibraries(project, monitor);
    	boolean shouldAddMavenLibrary = false;
    	if (startPage.getInitialProjectContent()==IKieProjectWizardPage.EMPTY_PROJECT)
    		shouldAddMavenLibrary = emptyProjectPage.shouldCreateMavenProject();
    	else if (startPage.getInitialProjectContent()==IKieProjectWizardPage.SAMPLE_FILES_PROJECT) 
    		shouldAddMavenLibrary = sampleFilesProjectPage.shouldCreateMavenProject();
    	if (shouldAddMavenLibrary) {
    		FileUtils.addMavenLibraries(project, monitor);
    	}
    }

    protected void addSourceFolders(IJavaProject project, IProgressMonitor monitor) throws JavaModelException, CoreException {
    	if (startPage.getInitialProjectContent()!=IKieProjectWizardPage.ONLINE_EXAMPLE_PROJECT) {
	        List<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
	        list.addAll(Arrays.asList(project.getRawClasspath()));
	        addSourceFolder(project, list, "src/main/java", monitor);
            if (startPage.getRuntime().getVersion().startsWith("6")) {
	        	addSourceFolder(project, list, "src/main/resources", monitor);
	        } else {
	        	addSourceFolder(project, list, "src/main/rules", monitor);
	        }
	        project.setRawClasspath((IClasspathEntry[]) list.toArray(new IClasspathEntry[list.size()]), null);
    	}
    }

    protected void createInitialContent(IJavaProject javaProject, IProgressMonitor monitor)
            throws CoreException, JavaModelException, IOException {
    	if (startPage.getInitialProjectContent() == IKieProjectWizardPage.ONLINE_EXAMPLE_PROJECT) {
    		onlineExampleProjectPage.downloadOnlineExampleProject(javaProject.getProject(), monitor);
    		// Add these folders to the classpath if they exist, otherwise ignore.
    		FileUtils.addFolderToClasspath(javaProject, "src/main/java", false, monitor);
    		FileUtils.addFolderToClasspath(javaProject, "src/main/resources", false, monitor);
    		FileUtils.addFolderToClasspath(javaProject, "src/test/java", false, monitor);
    		FileUtils.addFolderToClasspath(javaProject, "src/test/resources", false, monitor);
    		FileUtils.addFolderToClasspath(javaProject, "src/main/rules", false, monitor);
    	}
    	else if (startPage.getInitialProjectContent() == IKieProjectWizardPage.EMPTY_PROJECT) {
    		if (emptyProjectPage.shouldCreateKJarProject())
    			createKJarArtifacts(javaProject, monitor);
    		if (emptyProjectPage.shouldCreateMavenProject())
    			createMavenArtifacts(javaProject, monitor);
    	}
    	else if (startPage.getInitialProjectContent() == IKieProjectWizardPage.SAMPLE_FILES_PROJECT) {
    		if (sampleFilesProjectPage.shouldCreateKJarProject())
    			createKJarArtifacts(javaProject, monitor);
    		if (sampleFilesProjectPage.shouldCreateMavenProject())
    			createMavenArtifacts(javaProject, monitor);
    	}
    }
    
    protected void addSourceFolder(IJavaProject project, List<IClasspathEntry> list, String s, IProgressMonitor monitor) throws CoreException {
        IFolder folder = project.getProject().getFolder(s);
        FileUtils.createFolder(folder, monitor);
        IPackageFragmentRoot ipackagefragmentroot = project.getPackageFragmentRoot(folder);
        list.add(JavaCore.newSourceEntry(ipackagefragmentroot.getPath()));
    }

	@Override
	public boolean canFinish() {
    	if (startPage.getInitialProjectContent()==IKieProjectWizardPage.EMPTY_PROJECT)
    		return emptyProjectPage.isPageComplete();
    	if (startPage.getInitialProjectContent()==IKieProjectWizardPage.SAMPLE_FILES_PROJECT)
    		return sampleFilesProjectPage.isPageComplete();
    	if (startPage.getInitialProjectContent()==IKieProjectWizardPage.ONLINE_EXAMPLE_PROJECT)
    		return this.onlineExampleProjectPage.isPageComplete();
    	return false;
    }
}
