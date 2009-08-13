package org.drools.eclipse.wizard.project;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.builder.DroolsBuilder;
import org.drools.eclipse.preferences.DroolsRuntimesPreferencePage;
import org.drools.eclipse.util.DroolsClasspathContainer;
import org.drools.eclipse.util.DroolsRuntime;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 * A wizard to create a new Drools project.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class NewDroolsProjectWizard extends BasicNewResourceWizard {

    public static final String DROOLS_CLASSPATH_CONTAINER_PATH = "DROOLS/Drools";
    
    private IProject newProject;
    private WizardNewProjectCreationPage mainPage;
    private NewDroolsProjectWizardPage extraPage;
    private NewDroolsProjectRuntimeWizardPage runtimePage;
    
    public void addPages() {
        super.addPages();
        mainPage = new WizardNewProjectCreationPage("basicNewProjectPage");
        mainPage.setTitle("New Drools Project");
        mainPage.setDescription("Create a new Drools Project");
        this.addPage(mainPage);
        extraPage = new NewDroolsProjectWizardPage();
        addPage(extraPage);
        runtimePage = new NewDroolsProjectRuntimeWizardPage();
        addPage(runtimePage);
        setNeedsProgressMonitor(true);
    }

    public boolean performFinish() {
        createDroolsProject();
        if (newProject == null) {
            return false;
        }
        selectAndReveal(newProject);
        return true;
    }

    private void createDroolsProject() {
        newProject = createNewProject();
        WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
            protected void execute(IProgressMonitor monitor)
                    throws CoreException {
                try {
                	IJavaProject project = JavaCore.create(newProject);
                	createDroolsRuntime(project, monitor);
                    createOutputLocation(project, monitor);
                    addJavaBuilder(project, monitor);
                    setClasspath(project, monitor);
                    createInitialContent(project, monitor);
                	newProject.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
                } catch (IOException _ex) {
                	ErrorDialog.openError(getShell(), "Problem creating Drools project",
                        null, null);
                }
            }
        };
        try {
            getContainer().run(true, true, op);
        } catch (Throwable t) {
            DroolsEclipsePlugin.log(t);
        }
    }
    
    private IProject createNewProject() {
        if (newProject != null) {
            return newProject;
        }
        final IProject newProjectHandle = mainPage.getProjectHandle();

        // get a project descriptor
        IPath newPath = null;
        if (!mainPage.useDefaults())
            newPath = mainPage.getLocationPath();

        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        final IProjectDescription description = workspace
                .newProjectDescription(newProjectHandle.getName());
        description.setLocation(newPath);
        addNatures(description);

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
                DroolsEclipsePlugin.log(e);
            }
            return null;
        }

        return newProjectHandle;
    }
    
    private void addNatures(IProjectDescription projectDescription) {
        List list = new ArrayList();
        list.addAll(Arrays.asList(projectDescription.getNatureIds()));
        list.add("org.eclipse.jdt.core.javanature");
        projectDescription.setNatureIds((String[]) list
            .toArray(new String[list.size()]));
    }
    
    private void createProject(IProjectDescription description,
            IProject projectHandle, IProgressMonitor monitor)
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
    
    private void createDroolsRuntime(IJavaProject project, IProgressMonitor monitor) throws CoreException {
		DroolsRuntime runtime = runtimePage.getDroolsRuntime();
		if (runtime != null) {
			IFile file = project.getProject().getFile(".settings/.drools.runtime");
			String runtimeString = "<runtime>" + runtime.getName() + "</runtime>";
			if (!file.exists()) {
				IFolder folder = project.getProject().getFolder(".settings");
				if (!folder.exists()) {
					folder.create(true, true, null);
				}
				file.create(new ByteArrayInputStream(runtimeString.getBytes()), true, null);
			} else {
				file.setContents(new ByteArrayInputStream(runtimeString.getBytes()), true, false, null);
			}
		}
	}

    private void createOutputLocation(IJavaProject project, IProgressMonitor monitor)
            throws JavaModelException, CoreException {
        IFolder folder = project.getProject().getFolder("bin");
        createFolder(folder, monitor);
        IPath path = folder.getFullPath();
        project.setOutputLocation(path, null);
    }

    private void addJavaBuilder(IJavaProject project, IProgressMonitor monitor) throws CoreException {
        IProjectDescription description = project.getProject().getDescription();
        ICommand[] commands = description.getBuildSpec();
        ICommand[] newCommands = new ICommand[commands.length + 2];
        System.arraycopy(commands, 0, newCommands, 0, commands.length);

        ICommand javaCommand = description.newCommand();
        javaCommand.setBuilderName("org.eclipse.jdt.core.javabuilder");
        newCommands[commands.length] = javaCommand;
        
        ICommand droolsCommand = description.newCommand();
        droolsCommand.setBuilderName(DroolsBuilder.BUILDER_ID);
        newCommands[commands.length + 1] = droolsCommand;
        
        description.setBuildSpec(newCommands);
        project.getProject().setDescription(description, monitor);
    }

    private void setClasspath(IJavaProject project, IProgressMonitor monitor)
            throws JavaModelException, CoreException {
        project.setRawClasspath(new IClasspathEntry[0], monitor);
        addSourceFolders(project, monitor);
        addJRELibraries(project, monitor);
        addDroolsLibraries(project, monitor);
    }

    private void addSourceFolders(IJavaProject project, IProgressMonitor monitor) throws JavaModelException, CoreException {
        List list = new ArrayList();
        list.addAll(Arrays.asList(project.getRawClasspath()));
        addSourceFolder(project, list, "src/main/java", monitor);
        addSourceFolder(project, list, "src/main/rules", monitor);
        project.setRawClasspath((IClasspathEntry[]) list.toArray(new IClasspathEntry[list.size()]), null);
    }
    
    private void addJRELibraries(IJavaProject project, IProgressMonitor monitor) throws JavaModelException {
        List list = new ArrayList();
        list.addAll(Arrays.asList(project.getRawClasspath()));
        list.addAll(Arrays.asList(PreferenceConstants.getDefaultJRELibrary()));
        project.setRawClasspath((IClasspathEntry[]) list
            .toArray(new IClasspathEntry[list.size()]), monitor);
    }

    private static IPath getClassPathContainerPath() {
        return new Path(DROOLS_CLASSPATH_CONTAINER_PATH);
    }

    private static void createDroolsLibraryContainer(IJavaProject project, IProgressMonitor monitor)
            throws JavaModelException {
        JavaCore.setClasspathContainer(getClassPathContainerPath(),
            new IJavaProject[] { project },
            new IClasspathContainer[] { new DroolsClasspathContainer(
                    project, getClassPathContainerPath()) }, monitor);
    }

    public static void addDroolsLibraries(IJavaProject project, IProgressMonitor monitor)
            throws JavaModelException {
        createDroolsLibraryContainer(project, monitor);
        List list = new ArrayList();
        list.addAll(Arrays.asList(project.getRawClasspath()));
        list.add(JavaCore.newContainerEntry(getClassPathContainerPath()));
        project.setRawClasspath((IClasspathEntry[]) list
            .toArray(new IClasspathEntry[list.size()]), monitor);
    }

    private void createInitialContent(IJavaProject project, IProgressMonitor monitor)
            throws CoreException, JavaModelException, IOException {
    	try {
	    	if (extraPage.createJavaRuleFile()) {
	    		createRuleSampleLauncher(project);
	    	}
	    	if (extraPage.createRuleFile()) {
	    		createRule(project, monitor);
	    	}
	    	if (extraPage.createDecisionTableFile()) {
	    		createDecisionTable(project, monitor);
	    	}
	    	if (extraPage.createJavaDecisionTableFile()) {
	    		createDecisionTableSampleLauncher(project);
	    	}
	    	if (extraPage.createRuleFlowFile()) {
	    		createRuleFlow(project, monitor);
	    	}
	    	if (extraPage.createJavaRuleFlowFile()) {
	    		createRuleFlowSampleLauncher(project);
	    	}
    	} catch (Throwable t) {
    		t.printStackTrace();
    	}
	}

    /**
     * Create the sample rule launcher file.
     */
    private void createRuleSampleLauncher(IJavaProject project)
            throws JavaModelException, IOException {
        
    	String s;
    	if (runtimePage.getGenerationType() == NewDroolsProjectRuntimeWizardPage.DROOLS4) {
    		s = "org/drools/eclipse/wizard/project/RuleLauncherSample_4.java.template";
    	} else {
    		s = "org/drools/eclipse/wizard/project/RuleLauncherSample.java.template";
    	}
        IFolder folder = project.getProject().getFolder("src/main/java");
        IPackageFragmentRoot packageFragmentRoot = project
                .getPackageFragmentRoot(folder);
        IPackageFragment packageFragment = packageFragmentRoot
                .createPackageFragment("com.sample", true, null);
        InputStream inputstream = getClass().getClassLoader()
                .getResourceAsStream(s);
        packageFragment.createCompilationUnit("DroolsTest.java", new String(
                readStream(inputstream)), true, null);
    }

    /**
     * Create the sample decision table launcher file.
     */
    private void createDecisionTableSampleLauncher(IJavaProject project)
            throws JavaModelException, IOException {
        
        String s;
        if (runtimePage.getGenerationType() == NewDroolsProjectRuntimeWizardPage.DROOLS4) {
    		s = "org/drools/eclipse/wizard/project/DecisionTableLauncherSample_4.java.template";
        } else {
        	s = "org/drools/eclipse/wizard/project/DecisionTableLauncherSample.java.template";
        }
        IFolder folder = project.getProject().getFolder("src/main/java");
        IPackageFragmentRoot packageFragmentRoot = project
                .getPackageFragmentRoot(folder);
        IPackageFragment packageFragment = packageFragmentRoot
                .createPackageFragment("com.sample", true, null);
        InputStream inputstream = getClass().getClassLoader()
                .getResourceAsStream(s);
        packageFragment.createCompilationUnit("DecisionTableTest.java", new String(
                readStream(inputstream)), true, null);
    }

    /**
     * Create the sample rule file.
     */
    private void createRule(IJavaProject project, IProgressMonitor monitor)
            throws CoreException {
        String fileName = "org/drools/eclipse/wizard/project/Sample.drl.template";
        IFolder folder = project.getProject().getFolder("src/main/rules");
        IFile file = folder.getFile("Sample.drl");
        InputStream inputstream = getClass().getClassLoader().getResourceAsStream(fileName);
        if (!file.exists()) {
        	file.create(inputstream, true, monitor);
        } else {
        	file.setContents(inputstream, true, false, monitor);
        }
    }

    /**
     * Create the sample decision table file.
     */
    private void createDecisionTable(IJavaProject project, IProgressMonitor monitor)
            throws CoreException {
        String fileName = "org/drools/eclipse/wizard/project/Sample.xls.template";
        IFolder folder = project.getProject().getFolder("src/main/rules");
        IFile file = folder.getFile("Sample.xls");
        InputStream inputstream = getClass().getClassLoader().getResourceAsStream(fileName);
        if (!file.exists()) {
        	file.create(inputstream, true, monitor);
        } else {
        	file.setContents(inputstream, true, false, monitor);
        }
    }

    /**
     * Create the sample RuleFlow file.
     */
    private void createRuleFlow(IJavaProject project, IProgressMonitor monitor)
            throws CoreException {

    	if (runtimePage.getGenerationType() == NewDroolsProjectRuntimeWizardPage.DROOLS4) {
    		String fileName = "org/drools/eclipse/wizard/project/ruleflow_4.rf.template";
	        IFolder folder = project.getProject().getFolder("src/main/rules");
	        IFile file = folder.getFile("ruleflow.rf");
	        InputStream inputstream = getClass().getClassLoader().getResourceAsStream(fileName);
	        if (!file.exists()) {
	        	file.create(inputstream, true, monitor);
	        } else {
	        	file.setContents(inputstream, true, false, monitor);
	        }
	        
	        fileName = "org/drools/eclipse/wizard/project/ruleflow_4.rfm.template";
	        file = folder.getFile("ruleflow.rfm");
	        inputstream = getClass().getClassLoader().getResourceAsStream(fileName);
	        if (!file.exists()) {
	        	file.create(inputstream, true, monitor);
	        } else {
	        	file.setContents(inputstream, true, false, monitor);
	        }
	        
	        fileName = "org/drools/eclipse/wizard/project/ruleflow_4.drl.template";
	        file = folder.getFile("ruleflow.drl");
	        inputstream = getClass().getClassLoader().getResourceAsStream(fileName);
	        if (!file.exists()) {
	        	file.create(inputstream, true, monitor);
	        } else {
	        	file.setContents(inputstream, true, false, monitor);
	        }
    	} else if (runtimePage.getGenerationType() == NewDroolsProjectRuntimeWizardPage.DROOLS5) {
	        String fileName = "org/drools/eclipse/wizard/project/ruleflow.rf.template";
	        IFolder folder = project.getProject().getFolder("src/main/rules");
	        IFile file = folder.getFile("ruleflow.rf");
	        InputStream inputstream = getClass().getClassLoader().getResourceAsStream(fileName);
	        if (!file.exists()) {
	        	file.create(inputstream, true, monitor);
	        } else {
	        	file.setContents(inputstream, true, false, monitor);
	        }
    	} else {
    	    String fileName = "org/drools/eclipse/wizard/project/sample.bpmn.template";
            IFolder folder = project.getProject().getFolder("src/main/rules");
            IFile file = folder.getFile("sample.bpmn");
            InputStream inputstream = getClass().getClassLoader().getResourceAsStream(fileName);
            if (!file.exists()) {
                file.create(inputstream, true, monitor);
            } else {
                file.setContents(inputstream, true, false, monitor);
            }
    	}
    }

    /**
     * Create the sample RuleFlow launcher file.
     */
    private void createRuleFlowSampleLauncher(IJavaProject project)
            throws JavaModelException, IOException {
        
        String s;
        if (runtimePage.getGenerationType() == NewDroolsProjectRuntimeWizardPage.DROOLS4) {
    		s = "org/drools/eclipse/wizard/project/RuleFlowLauncherSample_4.java.template";
        } else if (runtimePage.getGenerationType() == NewDroolsProjectRuntimeWizardPage.DROOLS5) {
        	s = "org/drools/eclipse/wizard/project/RuleFlowLauncherSample.java.template";
        } else {
            s = "org/drools/eclipse/wizard/project/ProcessLauncherSample_bpmn.java.template";
        }
        IFolder folder = project.getProject().getFolder("src/main/java");
        IPackageFragmentRoot packageFragmentRoot = project
                .getPackageFragmentRoot(folder);
        IPackageFragment packageFragment = packageFragmentRoot
                .createPackageFragment("com.sample", true, null);
        InputStream inputstream = getClass().getClassLoader()
                .getResourceAsStream(s);
        packageFragment.createCompilationUnit("ProcessTest.java", new String(
                readStream(inputstream)), true, null);
    }

    protected void initializeDefaultPageImageDescriptor() {
    	ImageDescriptor desc = DroolsEclipsePlugin.getImageDescriptor("icons/drools-large.PNG");
        setDefaultPageImageDescriptor(desc);
    }

    private byte[] readStream(InputStream inputstream) throws IOException {
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
    
    private void addSourceFolder(IJavaProject project, List list, String s, IProgressMonitor monitor) throws CoreException {
        IFolder folder = project.getProject().getFolder(s);
        createFolder(folder, monitor);
        IPackageFragmentRoot ipackagefragmentroot = project.getPackageFragmentRoot(folder);
        list.add(JavaCore.newSourceEntry(ipackagefragmentroot.getPath()));
    }
    
    private void createFolder(IFolder folder, IProgressMonitor monitor) throws CoreException {
        IContainer container = folder.getParent();
        if (container != null && !container.exists()
                && (container instanceof IFolder))
            createFolder((IFolder) container, monitor);
        if (!folder.exists()) {
        	folder.create(true, true, monitor);
        }
    }
}
