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

package org.kie.eclipse.wizard.project;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
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
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.kie.eclipse.runtime.IRuntime;
import org.kie.eclipse.utils.FileUtils;

/**
 * A wizard to create a new Drools project.
 */
public abstract class AbstractKieProjectWizard extends BasicNewResourceWizard {

    public static final String DROOLS_CLASSPATH_CONTAINER_PATH = "DROOLS/Drools";
    
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
    protected IWizardPage lastPage = null;
    
    abstract protected IKieProjectWizardPage createStartPage(String pageId);
    abstract protected IKieProjectWizardPage createEmptyProjectPage(String pageId);
    abstract protected IKieProjectWizardPage createSampleFilesProjectPage(String pageId);
    abstract protected IKieProjectWizardPage createOnlineExampleProjectPage(String pageId);

    abstract protected IClasspathContainer createClasspathContainer(IJavaProject project, IProgressMonitor monitor);
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
        
        lastPage = createLastPage();
        if (lastPage!=null)
        	addPage(lastPage);

        setNeedsProgressMonitor(true);
    }
    
    protected IWizardPage createLastPage() {
    	return null;
    }
    
    public IWizardPage getLastPage() {
    	return lastPage;
    }
    
	public boolean performFinish() {
    	IProject newProjectHandle = null;
    	for (IProjectDescription pd : startPage.getNewProjectDescriptions()) {
    		if (newProjectHandle==null) {
    			newProjectHandle = createNewProject(pd);
    		}
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
                    addBuilders(project, monitor);
                    setClasspath(project, monitor);
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
    	addJavaNature(projectDescription);
    	addMavenNature(projectDescription);
    }
    
    protected void addJavaNature(IProjectDescription projectDescription) {
        List<String> list = new ArrayList<String>();
        list.addAll(Arrays.asList(projectDescription.getNatureIds()));
        list.add("org.eclipse.jdt.core.javanature");
        projectDescription.setNatureIds((String[]) list.toArray(new String[list.size()]));
    }
    
    protected void addMavenNature(IProjectDescription projectDescription) {
    	boolean shouldAddMavenNature = false;
    	if (startPage.getInitialProjectContent()==IKieProjectWizardPage.EMPTY_PROJECT)
    		shouldAddMavenNature = emptyProjectPage.shouldCreateMavenProject();
    	else if (startPage.getInitialProjectContent()==IKieProjectWizardPage.SAMPLE_FILES_PROJECT) 
    		shouldAddMavenNature = sampleFilesProjectPage.shouldCreateMavenProject();
    	if (shouldAddMavenNature) {
	        List<String> list = new ArrayList<String>();
	        list.addAll(Arrays.asList(projectDescription.getNatureIds()));
	        list.add("org.eclipse.m2e.core.maven2Nature");
	        projectDescription.setNatureIds((String[]) list.toArray(new String[list.size()]));
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
        IFolder folder = createFolder(project, folderName, monitor);
        IPath path = folder.getFullPath();
        project.setOutputLocation(path, null);
    }

    protected void addBuilders(IJavaProject project, IProgressMonitor monitor) throws CoreException {
    	addJavaBuilder(project, monitor);
    	addMavenBuilder(project, monitor);
    }
    
    protected void addJavaBuilder(IJavaProject project, IProgressMonitor monitor) throws CoreException {
        IProjectDescription description = project.getProject().getDescription();
        ICommand[] commands = description.getBuildSpec();
        ICommand[] newCommands = new ICommand[commands.length + 1];
        System.arraycopy(commands, 0, newCommands, 0, commands.length);

        ICommand javaCommand = description.newCommand();
        javaCommand.setBuilderName("org.eclipse.jdt.core.javabuilder");
        newCommands[commands.length] = javaCommand;
        
        description.setBuildSpec(newCommands);
        project.getProject().setDescription(description, monitor);
    }

    protected void addMavenBuilder(IJavaProject project, IProgressMonitor monitor) throws CoreException {
    	boolean shouldAddMavenBuilder = false;
    	if (startPage.getInitialProjectContent()==IKieProjectWizardPage.EMPTY_PROJECT)
    		shouldAddMavenBuilder = emptyProjectPage.shouldCreateMavenProject();
    	else if (startPage.getInitialProjectContent()==IKieProjectWizardPage.SAMPLE_FILES_PROJECT) 
    		shouldAddMavenBuilder = sampleFilesProjectPage.shouldCreateMavenProject();
    	if (shouldAddMavenBuilder) {
            IProjectDescription description = project.getProject().getDescription();
            ICommand[] commands = description.getBuildSpec();
            ICommand[] newCommands = new ICommand[commands.length + 1];
            System.arraycopy(commands, 0, newCommands, 0, commands.length);

            ICommand mavenCommand = description.newCommand();
            mavenCommand.setBuilderName("org.eclipse.m2e.core.maven2Builder");
            newCommands[commands.length] = mavenCommand;
            
            description.setBuildSpec(newCommands);
            project.getProject().setDescription(description, monitor);
    	}
    }
    
    protected void setClasspath(IJavaProject project, IProgressMonitor monitor)
            throws JavaModelException, CoreException {
        project.setRawClasspath(new IClasspathEntry[0], monitor);
        addSourceFolders(project, monitor);
        addJRELibraries(project, monitor);
        addRuntimeLibraries(project, monitor);
        addMavenLibraries(project, monitor);
    }

    protected void addSourceFolders(IJavaProject project, IProgressMonitor monitor) throws JavaModelException, CoreException {
    	if (startPage.getInitialProjectContent()!=IKieProjectWizardPage.ONLINE_EXAMPLE_PROJECT) {
	        List<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
	        list.addAll(Arrays.asList(project.getRawClasspath()));
	        addSourceFolder(project, list, "src/main/java", monitor);
        	addSourceFolder(project, list, "src/main/resources", monitor);
	        if (emptyProjectPage.shouldCreateMavenProject()) {
	        	createFolder(project, "src/main/resources/META-INF", monitor);
	        	createFolder(project, "src/main/resources/META-INF/maven", monitor);
	        } else {
	        	addSourceFolder(project, list, "src/main/rules", monitor);
	        }
	        project.setRawClasspath((IClasspathEntry[]) list.toArray(new IClasspathEntry[list.size()]), null);
    	}
    }
    
    protected void addFolderToClasspath(IJavaProject project, String folderName, IProgressMonitor monitor) throws JavaModelException, CoreException {
        List<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
        list.addAll(Arrays.asList(project.getRawClasspath()));
        IFolder folder = project.getProject().getFolder(folderName);
        if (folder.exists()) {
        	addSourceFolder(project, list, folderName, monitor);
        	project.setRawClasspath((IClasspathEntry[]) list.toArray(new IClasspathEntry[list.size()]), null);
        }
    }
    
    protected void addJRELibraries(IJavaProject project, IProgressMonitor monitor) throws JavaModelException {
        List<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
        list.addAll(Arrays.asList(project.getRawClasspath()));
        list.addAll(Arrays.asList(PreferenceConstants.getDefaultJRELibrary()));
        project.setRawClasspath((IClasspathEntry[]) list.toArray(new IClasspathEntry[list.size()]), monitor);
    }
    
    protected IClasspathContainer createRuntimeLibraryContainer(IJavaProject project, IProgressMonitor monitor)
            throws JavaModelException {
    	IClasspathContainer cp = createClasspathContainer(project, monitor);
        JavaCore.setClasspathContainer(cp.getPath(),
            new IJavaProject[] { project },
            new IClasspathContainer[] { cp }, monitor);
        return cp;
    }

    protected void addRuntimeLibraries(IJavaProject project, IProgressMonitor monitor)
            throws JavaModelException {
    	IClasspathContainer cp = createRuntimeLibraryContainer(project, monitor);
        List<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
        list.addAll(Arrays.asList(project.getRawClasspath()));
        list.add(JavaCore.newContainerEntry(cp.getPath()));
        project.setRawClasspath((IClasspathEntry[]) list.toArray(new IClasspathEntry[list.size()]), monitor);
    }

    protected void addMavenLibraries(IJavaProject project, IProgressMonitor monitor)
            throws JavaModelException {
    	boolean shouldAddMavenLibrary = false;
    	if (startPage.getInitialProjectContent()==IKieProjectWizardPage.EMPTY_PROJECT)
    		shouldAddMavenLibrary = emptyProjectPage.shouldCreateMavenProject();
    	else if (startPage.getInitialProjectContent()==IKieProjectWizardPage.SAMPLE_FILES_PROJECT) 
    		shouldAddMavenLibrary = sampleFilesProjectPage.shouldCreateMavenProject();
    	if (shouldAddMavenLibrary) {
			List<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
			list.addAll(Arrays.asList(project.getRawClasspath()));
			list.add(JavaCore.newContainerEntry(new Path("org.eclipse.m2e.MAVEN2_CLASSPATH_CONTAINER")));
			project.setRawClasspath((IClasspathEntry[]) list.toArray(new IClasspathEntry[list.size()]), monitor);
    	}
    }

    public void addJUnitLibrary(IJavaProject project, IProgressMonitor monitor)
    		throws JavaModelException {
		List<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
		list.addAll(Arrays.asList(project.getRawClasspath()));
		list.add(JavaCore.newContainerEntry(new Path("org.eclipse.jdt.junit.JUNIT_CONTAINER/4")));
		project.setRawClasspath((IClasspathEntry[]) list
		    .toArray(new IClasspathEntry[list.size()]), monitor);
    }

    protected void createInitialContent(IJavaProject javaProject, IProgressMonitor monitor)
            throws CoreException, JavaModelException, IOException {
    	if (startPage.getInitialProjectContent() == IKieProjectWizardPage.ONLINE_EXAMPLE_PROJECT) {
    		onlineExampleProjectPage.downloadOnlineExampleProject(javaProject.getProject(), monitor);
    		// Add these folders to the classpath if they exist, otherwise ignore.
    		addFolderToClasspath(javaProject, "src/main/java", monitor);
    		addFolderToClasspath(javaProject, "src/main/resources", monitor);
    		addFolderToClasspath(javaProject, "src/test/java", monitor);
    		addFolderToClasspath(javaProject, "src/test/resources", monitor);
    		addFolderToClasspath(javaProject, "src/main/rules", monitor);
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
        createFolder(folder, monitor);
        IPackageFragmentRoot ipackagefragmentroot = project.getPackageFragmentRoot(folder);
        list.add(JavaCore.newSourceEntry(ipackagefragmentroot.getPath()));
    }
    
    protected IFolder createFolder(IJavaProject project, String s, IProgressMonitor monitor) throws CoreException {
    	IFolder folder = project.getProject().getFolder(s);
    	createFolder(folder, monitor);
    	return folder;
    }

    protected void createFolder(IFolder folder, IProgressMonitor monitor) throws CoreException {
        IContainer container = folder.getParent();
        if (container != null && !container.exists()
                && (container instanceof IFolder))
            createFolder((IFolder) container, monitor);
        if (!folder.exists()) {
            folder.create(true, true, monitor);
        }
    }

	protected byte[] readStream(InputStream inputstream) throws IOException {
	    byte bytes[] = (byte[]) null;
	    int i = 0;
	    byte tempBytes[] = new byte[1024];
	    for (int j = inputstream.read(tempBytes); j != -1; j = inputstream.read(tempBytes)) {
	        byte tempBytes2[] = new byte[i + j];
	        if (i > 0) {
	            System.arraycopy(bytes, 0, tempBytes2, 0, i);
	        }
	        System.arraycopy(tempBytes, 0, tempBytes2, i, j);
	        bytes = tempBytes2;
	        i += j;
	    }
	
	    return bytes;
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
