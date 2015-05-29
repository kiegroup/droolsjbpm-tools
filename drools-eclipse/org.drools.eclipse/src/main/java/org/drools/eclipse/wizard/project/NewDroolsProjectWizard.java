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

package org.drools.eclipse.wizard.project;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.builder.DroolsBuilder;
import org.drools.eclipse.util.DroolsClasspathContainer;
import org.drools.eclipse.util.DroolsRuntime;
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
import org.kie.eclipse.wizard.project.AbstractKieProjectMainWizardPage;
import org.kie.eclipse.wizard.project.AbstractKieProjectRuntimeWizardPage;
import org.kie.eclipse.wizard.project.AbstractKieProjectWizard;

/**
 * A wizard to create a new Drools project.
 */
public class NewDroolsProjectWizard extends AbstractKieProjectWizard {

    public static final String DROOLS_CLASSPATH_CONTAINER_PATH = "DROOLS/Drools";
    
    public static final String MAIN_PAGE = "extendedNewProjectPage";
    public static final String RUNTIME_PAGE = "extendedNewProjectRuntimePage";
    
    private NewDroolsProjectWizardPage mainPage;
    private NewDroolsProjectRuntimeWizardPage runtimePage;

    protected void initializeDefaultPageImageDescriptor() {
        ImageDescriptor desc = DroolsEclipsePlugin.getImageDescriptor("icons/drools-large.PNG");
        setDefaultPageImageDescriptor(desc);
    }

    protected void createOutputLocation(IJavaProject project, IProgressMonitor monitor)
            throws JavaModelException, CoreException {
        IFolder folder = createFolder(project, "target", monitor);
        IPath path = folder.getFullPath();
        project.setOutputLocation(path, null);
    }

    protected void addBuilders(IJavaProject project, IProgressMonitor monitor) throws CoreException {
    	super.addBuilders(project, monitor);
    	addDroolsBuilder(project, monitor);
    }
    
    private void addDroolsBuilder(IJavaProject project, IProgressMonitor monitor) throws CoreException {
        IProjectDescription description = project.getProject().getDescription();
        ICommand[] commands = description.getBuildSpec();
        ICommand[] newCommands = new ICommand[commands.length + 1];
        System.arraycopy(commands, 0, newCommands, 0, commands.length);
        
        ICommand droolsCommand = description.newCommand();
        droolsCommand.setBuilderName(DroolsBuilder.BUILDER_ID);
        newCommands[commands.length] = droolsCommand;
        
        description.setBuildSpec(newCommands);
        project.getProject().setDescription(description, monitor);
    }

    private static IPath getClassPathContainerPath() {
        return new Path(DROOLS_CLASSPATH_CONTAINER_PATH);
    }

	@Override
	protected IClasspathContainer createClasspathContainer(IJavaProject project) {
		return new DroolsClasspathContainer(project, getClassPathContainerPath());
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
        List<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
        list.addAll(Arrays.asList(project.getRawClasspath()));
        list.add(JavaCore.newContainerEntry(getClassPathContainerPath()));
        project.setRawClasspath((IClasspathEntry[]) list
            .toArray(new IClasspathEntry[list.size()]), monitor);
    }

    protected boolean createInitialContent(IJavaProject javaProject, IProgressMonitor monitor)
            throws CoreException, JavaModelException, IOException {
    	if (!super.createInitialContent(javaProject, monitor)) {
	    	if (mainPage.getInitialProjectContent() == NewDroolsProjectWizardPage.EMPTY_PROJECT) {
	        	createKModule(javaProject, monitor);
	        	createPom(javaProject, monitor);
	    	}
	    	else if (mainPage.getInitialProjectContent() == NewDroolsProjectWizardPage.SAMPLE_FILES_PROJECT) {
		        try {
		        	boolean createKModule = false;
		            if (mainPage.createJavaRuleFile()) {
		                createRuleSampleLauncher(javaProject);
		                createKModule = true;
		            }
		            if (mainPage.createRuleFile()) {
		                createRule(javaProject, monitor);
		                createKModule = true;
		            }
		            if (mainPage.createDecisionTableFile()) {
		                createDecisionTable(javaProject, monitor);
		                createKModule = true;
		            }
		            if (mainPage.createJavaDecisionTableFile()) {
		                createDecisionTableSampleLauncher(javaProject);
		                createKModule = true;
		            }
		            if (mainPage.createRuleFlowFile()) {
		                createRuleFlow(javaProject, monitor);
		                createKModule = true;
		            }
		            if (mainPage.createJavaRuleFlowFile()) {
		                createRuleFlowSampleLauncher(javaProject);
		                createKModule = true;
		            }
		            if (createKModule) {
		            	createKModule(javaProject, monitor);
		            	createPom(javaProject, monitor);
		            }
		        } catch (Throwable t) {
		            t.printStackTrace();
		        }
	    	}
    	}
    	return true;
    }

    /**
     * Create the sample rule launcher file.
     */
    private void createRuleSampleLauncher(IJavaProject project)
            throws JavaModelException, IOException {

        String runtime = runtimePage.getRuntimeId();
        if (DroolsRuntime.ID_DROOLS_4.equals(runtime)) {
            createProjectJavaFile(project, "org/drools/eclipse/wizard/project/RuleLauncherSample_4.java.template", "DroolsTest.java");
        } else if (DroolsRuntime.ID_DROOLS_5.equals(runtime) ||
                DroolsRuntime.ID_DROOLS_5_1.equals(runtime)) {
            createProjectJavaFile(project, "org/drools/eclipse/wizard/project/RuleLauncherSample_5.java.template", "DroolsTest.java");
        } else {
            createProjectJavaFile(project, "org/drools/eclipse/wizard/project/RuleLauncherSample_6.java.template", "DroolsTest.java");
        }
    }

    /**
     * Create the sample decision table launcher file.
     */
    private void createDecisionTableSampleLauncher(IJavaProject project)
            throws JavaModelException, IOException {
        
        String runtime = runtimePage.getRuntimeId();
        if (DroolsRuntime.ID_DROOLS_4.equals(runtime)) {
            createProjectJavaFile(project, "org/drools/eclipse/wizard/project/DecisionTableLauncherSample_4.java.template", "DecisionTableTest.java");
        } else if (DroolsRuntime.ID_DROOLS_5.equals(runtime) ||
            DroolsRuntime.ID_DROOLS_5_1.equals(runtime)) {
            createProjectJavaFile(project, "org/drools/eclipse/wizard/project/DecisionTableLauncherSample_5.java.template", "DecisionTableTest.java");
        } else {
            createProjectJavaFile(project, "org/drools/eclipse/wizard/project/DecisionTableLauncherSample_6.java.template", "DecisionTableTest.java");
        }
    }

	private void createProjectJavaFile(IJavaProject project, String templateFile, String javaFile) throws JavaModelException, IOException {
		IFolder folder = project.getProject().getFolder("src/main/java");
        IPackageFragmentRoot packageFragmentRoot = project.getPackageFragmentRoot(folder);
        IPackageFragment packageFragment = packageFragmentRoot.createPackageFragment("com.sample", true, null);
        InputStream inputstream = getClass().getClassLoader().getResourceAsStream(templateFile);
        packageFragment.createCompilationUnit(javaFile, new String(readStream(inputstream)), true, null);
	}
    
	private void createProjectFile(IJavaProject project, IProgressMonitor monitor, String templateFile, String folderName, String fileName) throws CoreException {
        InputStream inputstream = getClass().getClassLoader().getResourceAsStream(templateFile);
        createProjectFile(project, monitor, inputstream, folderName, fileName);
	}

	private void createProjectFile(IJavaProject project, IProgressMonitor monitor, InputStream inputstream, String folderName, String fileName) throws CoreException {
        IFile file;
        if (folderName == null) {
            file = project.getProject().getFile(fileName);
        } else {
            IFolder folder = project.getProject().getFolder(folderName);
            file = folder.getFile(fileName);
        }

        if (!file.exists()) {
            file.create(inputstream, true, monitor);
        } else {
            file.setContents(inputstream, true, false, monitor);
        }
	}

	/**
     * Create the sample rule file.
     */
    private void createRule(IJavaProject project, IProgressMonitor monitor) throws CoreException {
        if (DroolsRuntime.ID_DROOLS_6.equals(runtimePage.getRuntimeId())) {
            createFolder(project, "src/main/resources/rules", monitor);
            createProjectFile(project, monitor, "org/drools/eclipse/wizard/project/Sample.drl.template", "src/main/resources/rules", "Sample.drl");
        } else {
            createProjectFile(project, monitor, "org/drools/eclipse/wizard/project/Sample.drl.template", "src/main/rules", "Sample.drl");
        }
    }

    private void createKModule(IJavaProject project, IProgressMonitor monitor) throws CoreException {
        if (DroolsRuntime.ID_DROOLS_6.equals(runtimePage.getRuntimeId())) {
        	createProjectFile(project, monitor, generateKModule(), "src/main/resources/META-INF", "kmodule.xml");
        }
    }

    private void createPom(IJavaProject project, IProgressMonitor monitor) throws CoreException {
        if (DroolsRuntime.ID_DROOLS_6.equals(runtimePage.getRuntimeId())) {
            String groupId = runtimePage.getGroupId();
            String artifactId = runtimePage.getArtifactId();
            String version = runtimePage.getVersion();
            createProjectFile(project, monitor, generatePomProperties(groupId, artifactId, version), "src/main/resources/META-INF/maven", "pom.properties");
            createProjectFile(project, monitor, generatePom(groupId, artifactId, version), null, "pom.xml");
        }
    }

    /**
     * Create the sample decision table file.
     */
    private void createDecisionTable(IJavaProject project, IProgressMonitor monitor) throws CoreException {
        if (DroolsRuntime.ID_DROOLS_6.equals(runtimePage.getRuntimeId())) {
    		createFolder(project, "src/main/resources/dtables", monitor);
        	createProjectFile(project, monitor, "org/drools/eclipse/wizard/project/Sample.xls.template", "src/main/resources/dtables", "Sample.xls");
    	} else {
        	createProjectFile(project, monitor, "org/drools/eclipse/wizard/project/Sample.xls.template", "src/main/rules", "Sample.xls");
    	}
    }

    /**
     * Create the sample RuleFlow file.
     */
    private void createRuleFlow(IJavaProject project, IProgressMonitor monitor) throws CoreException {

        String runtimeId = runtimePage.getRuntimeId();
        if (DroolsRuntime.ID_DROOLS_4.equals(runtimeId)) {
        	createProjectFile(project, monitor, "org/drools/eclipse/wizard/project/ruleflow_4.rf.template", "src/main/rules", "ruleflow.rf");
        	createProjectFile(project, monitor, "org/drools/eclipse/wizard/project/ruleflow_4.rfm.template", "src/main/rules", "ruleflow.rfm");
        	createProjectFile(project, monitor, "org/drools/eclipse/wizard/project/ruleflow_4.drl.template", "src/main/rules", "ruleflow.drl");
        } else if (DroolsRuntime.ID_DROOLS_5.equals(runtimeId)) {
        	createProjectFile(project, monitor, "org/drools/eclipse/wizard/project/ruleflow.rf.template", "src/main/rules", "ruleflow.rf");
        } else if (DroolsRuntime.ID_DROOLS_5_1.equals(runtimeId)) {
        	createProjectFile(project, monitor, "org/drools/eclipse/wizard/project/sample.bpmn.template", "src/main/rules", "sample.bpmn");
        } else {
    		createFolder(project, "src/main/resources/process", monitor);
        	createProjectFile(project, monitor, "org/drools/eclipse/wizard/project/sample.bpmn.template", "src/main/resources/process", "sample.bpmn");
        }
    }

    /**
     * Create the sample RuleFlow launcher file.
     */
    private void createRuleFlowSampleLauncher(IJavaProject project)
            throws JavaModelException, IOException {
        
        String s;
        String runtimeId = runtimePage.getRuntimeId();
        if (DroolsRuntime.ID_DROOLS_4.equals(runtimeId)) {
            s = "org/drools/eclipse/wizard/project/RuleFlowLauncherSample_4.java.template";
        } else if (DroolsRuntime.ID_DROOLS_5.equals(runtimeId)) {
            s = "org/drools/eclipse/wizard/project/RuleFlowLauncherSample.java.template";
        } else if (DroolsRuntime.ID_DROOLS_5_1.equals(runtimeId)) {
            s = "org/drools/eclipse/wizard/project/ProcessLauncherSample_bpmn_5.java.template";
        } else {
            s = "org/drools/eclipse/wizard/project/ProcessLauncherSample_bpmn_6.java.template";
        }
        createProjectJavaFile(project, s, "ProcessTest.java");
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
    
    private InputStream generateKModule() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    	sb.append("<kmodule xmlns=\"http://jboss.org/kie/6.0.0/kmodule\">\n");
    	
        if (mainPage.createJavaRuleFile() || mainPage.createRuleFile()) {
        	sb.append("    <kbase name=\"rules\" packages=\"rules\">\n");
        	sb.append("        <ksession name=\"ksession-rules\"/>\n");
        	sb.append("    </kbase>\n");
        }
        if (mainPage.createDecisionTableFile() || mainPage.createJavaDecisionTableFile()) {
        	sb.append("    <kbase name=\"dtables\" packages=\"dtables\">\n");
        	sb.append("        <ksession name=\"ksession-dtables\"/>\n");
        	sb.append("    </kbase>\n");
        }
        if (mainPage.createRuleFlowFile() || mainPage.createJavaRuleFlowFile()) {
        	sb.append("    <kbase name=\"process\" packages=\"process\">\n");
        	sb.append("        <ksession name=\"ksession-process\"/>\n");
        	sb.append("    </kbase>\n");
        }
        
        sb.append("</kmodule>\n");
    	
        return new ByteArrayInputStream(sb.toString().getBytes());
    }
    
    private InputStream generatePom(String groupId, String artifactId, String version) {
        String pom =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "         xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd\">\n" +
                "  <modelVersion>4.0.0</modelVersion>\n" +
                "\n" +
                "  <groupId>" + groupId + "</groupId>\n" +
                "  <artifactId>" + artifactId + "</artifactId>\n" +
                "  <version>" + version + "</version>\n" +
                "</project>\n";
        return new ByteArrayInputStream(pom.getBytes());
    }
    
	private InputStream generatePomProperties(String groupId,
			String artifactId, String version) {
		String pom = "groupId=" + groupId + "\n" + "artifactId=" + artifactId
				+ "\n" + "version=" + version + "\n";
		return new ByteArrayInputStream(pom.getBytes());
	}

	@Override
	protected AbstractKieProjectMainWizardPage createMainPage(String pageId) {
		if (mainPage==null)
			mainPage = new NewDroolsProjectWizardPage(pageId);
        return mainPage;
	}

	@Override
	protected AbstractKieProjectRuntimeWizardPage createRuntimePage(String pageId) {
		if (runtimePage==null)
			runtimePage = new NewDroolsProjectRuntimeWizardPage(pageId);
        return runtimePage;
	}
}
