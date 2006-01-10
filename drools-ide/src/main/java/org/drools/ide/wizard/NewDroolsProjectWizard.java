package org.drools.ide.wizard;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.drools.ide.DroolsIDEPlugin;
import org.drools.ide.builder.DroolsBuilder;
import org.drools.ide.util.DroolsClasspathContainer;
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
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;

/**
 * A wizard to create a new Drools project.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class NewDroolsProjectWizard extends BasicNewResourceWizard {

    private IProject newProject;
    private WizardNewProjectCreationPage mainPage;
    
    public void addPages() {
        super.addPages();
        mainPage = new WizardNewProjectCreationPage("basicNewProjectPage");
        mainPage.setTitle("New Drools Project");
        mainPage.setDescription("Create a new Drools Project");
        this.addPage(mainPage);
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
        try {
            newProject = createNewProject();
            IJavaProject project = JavaCore.create(newProject);
            createOutputLocation(project);
            addJavaBuilder(project);
            setClasspath(project);
            createInitialContent(project);
            newProject.build(IncrementalProjectBuilder.FULL_BUILD, null);
        } catch (JavaModelException javamodelexception) {
            ErrorDialog.openError(getShell(), "Problem creating Drools project",
                null, javamodelexception.getStatus());
        } catch (CoreException coreexception) {
            ErrorDialog.openError(getShell(), "Problem creating Drools project",
                null, coreexception.getStatus());
        } catch (IOException _ex) {
            ErrorDialog.openError(getShell(), "Problem creating Drools project",
                null, null);
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
                DroolsIDEPlugin.log(e);
            }
            return null;
        }

        return newProjectHandle;
    }
    
    private void addNatures(IProjectDescription projectDescription) {
        List<String> list = new ArrayList<String>();
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
    
    private void createOutputLocation(IJavaProject project)
            throws JavaModelException, CoreException {
        IFolder ifolder = project.getProject().getFolder("bin");
        createFolder(ifolder);
        IPath ipath = ifolder.getFullPath();
        project.setOutputLocation(ipath, null);
    }

    private void addJavaBuilder(IJavaProject project) throws CoreException {
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
        project.getProject().setDescription(description, null);
    }

    private void setClasspath(IJavaProject project)
            throws JavaModelException, CoreException {
        project.setRawClasspath(new IClasspathEntry[0], null);
        addSourceFolders(project);
        addJRELibraries(project);
        addDroolsLibraries(project);
    }

    private void addSourceFolders(IJavaProject project) throws JavaModelException, CoreException {
        List<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
        list.addAll(Arrays.asList(project.getRawClasspath()));
        addSourceFolder(project, list, "src/java");
        addSourceFolder(project, list, "src/rules");
        project.setRawClasspath((IClasspathEntry[]) list.toArray(new IClasspathEntry[list.size()]), null);
    }
    
    private void addJRELibraries(IJavaProject project) throws JavaModelException {
        List<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
        list.addAll(Arrays.asList(project.getRawClasspath()));
        list.addAll(Arrays.asList(PreferenceConstants.getDefaultJRELibrary()));
        project.setRawClasspath((IClasspathEntry[]) list
            .toArray(new IClasspathEntry[list.size()]), null);
    }

    private IPath getClassPathContainerPath() {
        return new Path("DROOLS/" + getDroolsNamePref());
    }

    private void createDroolsLibraryContainer(IJavaProject project)
            throws JavaModelException {
        JavaCore.setClasspathContainer(getClassPathContainerPath(),
            new IJavaProject[] { project },
            new IClasspathContainer[] { new DroolsClasspathContainer(
                    project, getClassPathContainerPath()) }, null);
    }

    private String getDroolsNamePref() {
        return "Drools 2.5";
    }

    private void addDroolsLibraries(IJavaProject project)
            throws JavaModelException {
        createDroolsLibraryContainer(project);
        List<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
        list.addAll(Arrays.asList(project.getRawClasspath()));
        list.add(JavaCore.newContainerEntry(getClassPathContainerPath()));
        project.setRawClasspath((IClasspathEntry[]) list
            .toArray(new IClasspathEntry[list.size()]), null);
    }

    private void createInitialContent(IJavaProject project)
            throws CoreException, JavaModelException, IOException {
		createRulesProject(project);
		createRule(project);
	}

    private void createRule(IJavaProject project)
            throws JavaModelException, IOException {
        String s = "org/drools/ide/resource/DroolsTest.java.template";
        IFolder folder = project.getProject().getFolder("src/java");
        IPackageFragmentRoot packageFragmentRoot = project
                .getPackageFragmentRoot(folder);
        IPackageFragment packageFragment = packageFragmentRoot
                .createPackageFragment("com.sample", true, null);
        InputStream inputstream = getClass().getClassLoader()
                .getResourceAsStream(s);
        packageFragment.createCompilationUnit("DroolsTest.java", new String(
                readStream(inputstream)), true, null);
    }
    
    private void createRulesProject(IJavaProject project)
            throws CoreException {
        String fileName = "org/drools/ide/resource/Rules.java.drl.template";
        IFolder folder = project.getProject().getFolder("src/rules");
        IFile file = folder.getFile("Rules.java.drl");
        InputStream inputstream = getClass().getClassLoader().getResourceAsStream(fileName);
        file.create(inputstream, true, null);
    }

    protected void initializeDefaultPageImageDescriptor() {
        ImageDescriptor desc = AbstractUIPlugin.imageDescriptorFromPlugin(
                "org.eclipse.ui.ide", "icons/full/wizban/newprj_wiz.gif");
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
    
    private void addSourceFolder(IJavaProject project, List<IClasspathEntry> list, String s) throws CoreException {
        IFolder folder = project.getProject().getFolder(s);
        createFolder(folder);
        IPackageFragmentRoot ipackagefragmentroot = project.getPackageFragmentRoot(folder);
        list.add(JavaCore.newSourceEntry(ipackagefragmentroot.getPath()));
    }
    
    private void createFolder(IFolder folder) throws CoreException {
        IContainer container = folder.getParent();
        if (container != null && !container.exists()
                && (container instanceof IFolder))
            createFolder((IFolder) container);
        folder.create(true, true, null);
    }

}
