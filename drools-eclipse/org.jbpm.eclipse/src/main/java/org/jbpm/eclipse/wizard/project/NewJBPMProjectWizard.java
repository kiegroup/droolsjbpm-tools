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

package org.jbpm.eclipse.wizard.project;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.jbpm.eclipse.JBPMEclipsePlugin;
import org.jbpm.eclipse.preferences.JBPMProjectPreferencePage;
import org.jbpm.eclipse.util.JBPMRuntime;
import org.jbpm.eclipse.util.JBPMRuntimeManager;
import org.kie.eclipse.runtime.IRuntime;
import org.kie.eclipse.runtime.IRuntimeManager;
import org.kie.eclipse.utils.FileUtils;
import org.kie.eclipse.wizard.project.AbstractKieEmptyProjectWizardPage;
import org.kie.eclipse.wizard.project.AbstractKieOnlineExampleProjectWizardPage;
import org.kie.eclipse.wizard.project.AbstractKieProjectStartWizardPage;
import org.kie.eclipse.wizard.project.AbstractKieProjectWizard;
import org.kie.eclipse.wizard.project.IKieProjectWizardPage;
import org.kie.eclipse.wizard.project.IKieSampleFilesProjectWizardPage;
import org.kie.eclipse.runtime.AbstractRuntime.Version;

/**
 * A wizard to create a new jBPM project.
 * 
 */
public class NewJBPMProjectWizard extends AbstractKieProjectWizard {

    private EmptyJBPMProjectWizardPage emptyProjectPage;
    private SampleJBPMProjectWizardPage sampleFilesProjectPage;


    @Override
	protected void initializeDefaultPageImageDescriptor() {
        ImageDescriptor desc = JBPMEclipsePlugin.getImageDescriptor("icons/jbpm-large.png");
        setDefaultPageImageDescriptor(desc);
    }

    @Override
	protected void createOutputLocation(IJavaProject project, IProgressMonitor monitor)
            throws JavaModelException, CoreException {
    	String target = "bin";
    	if (startPage.getInitialProjectContent()==IKieProjectWizardPage.EMPTY_PROJECT)
    		target = emptyProjectPage.shouldCreateMavenProject() ? "target/classes" : "bin";
    	else if (startPage.getInitialProjectContent()==IKieProjectWizardPage.SAMPLE_FILES_PROJECT)
    		target = sampleFilesProjectPage.shouldCreateMavenProject() ? "target/classes" : "bin";
    	
        IFolder folder = project.getProject().getFolder(target);
        FileUtils.createFolder(folder, monitor);
        IPath path = folder.getFullPath();
        project.setOutputLocation(path, null);
    }

    @Override
	protected void setClasspath(IJavaProject project, IProgressMonitor monitor)
            throws JavaModelException, CoreException {
        super.setClasspath(project, monitor);
    	if (startPage.getInitialProjectContent()==IKieProjectWizardPage.EMPTY_PROJECT) {
    		if (!emptyProjectPage.shouldCreateMavenProject())
	        	FileUtils.addJUnitLibrary(project, monitor);
    	}
    	else if (startPage.getInitialProjectContent() == IKieProjectWizardPage.SAMPLE_FILES_PROJECT) {
	        if (!sampleFilesProjectPage.shouldCreateMavenProject() && sampleFilesProjectPage.shouldCreateJUnitFile())
	        	FileUtils.addJUnitLibrary(project, monitor);
        }
    }

    protected void addSourceFolders(IJavaProject project, IProgressMonitor monitor) throws JavaModelException, CoreException {
    	if (startPage.getInitialProjectContent()!=IKieProjectWizardPage.ONLINE_EXAMPLE_PROJECT) {
	        List<IClasspathEntry> list = new ArrayList<IClasspathEntry>();
	        list.addAll(Arrays.asList(project.getRawClasspath()));
	        addSourceFolder(project, list, "src/main/java", monitor);
        	addSourceFolder(project, list, "src/main/resources", monitor);
	        project.setRawClasspath((IClasspathEntry[]) list.toArray(new IClasspathEntry[list.size()]), null);
    	}
    }

    @Override
	protected void createInitialContent(IJavaProject javaProject, IProgressMonitor monitor)
            throws CoreException, JavaModelException, IOException {
    	if (startPage.getInitialProjectContent()==IKieProjectWizardPage.EMPTY_PROJECT) {
    		createDefaultPackages(javaProject, monitor);
    	}
    	else if (startPage.getInitialProjectContent() == IKieProjectWizardPage.SAMPLE_FILES_PROJECT) {
    		String exampleType = sampleFilesProjectPage.getSampleType();
    		createProcess(javaProject, monitor, exampleType);
	    	if (sampleFilesProjectPage.shouldCreateJUnitFile()) {
	    		createProcessSampleJUnit(javaProject, exampleType, monitor);
	    	}
    	}
	    super.createInitialContent(javaProject, monitor);
	    createLoggerFile(javaProject, monitor);
	}

    private void createDefaultPackages(IJavaProject project, IProgressMonitor monitor) throws CoreException {
		IFolder folder = project.getProject().getFolder("src/main/java/com/sample");
		FileUtils.createFolder(folder, monitor);
		folder = project.getProject().getFolder("src/main/resources/com/sample");
		FileUtils.createFolder(folder, monitor);
    }
    
    private void createLoggerFile(IJavaProject project, IProgressMonitor monitor) throws CoreException {
	    String fileName = "org/jbpm/eclipse/wizard/project/logback-test.xml.template";
        IFolder folder = project.getProject().getFolder("src/main/resources");
        FileUtils.createFolder(folder, monitor);
        IFile file = folder.getFile("logback-test.xml");
        InputStream inputstream = getClass().getClassLoader().getResourceAsStream(fileName);
        if (!file.exists()) {
            file.create(inputstream, true, monitor);
        } else {
            file.setContents(inputstream, true, false, monitor);
        }
	}

    /**
     * Create the sample process file.
     * @throws IOException 
     */
	private void createProcess(IJavaProject project, IProgressMonitor monitor, String exampleType)
			throws CoreException, IOException {
		// create the process (sample.bpmn) file
		String fileName = "org/jbpm/eclipse/wizard/project/" + exampleType + ".bpmn.template";
		IFolder folder = null;
		folder = project.getProject().getFolder("src/main/resources/com/sample");
		FileUtils.createFolder(folder, monitor);
		IFile file = folder.getFile("sample.bpmn");
		InputStream inputstream = getClass().getClassLoader().getResourceAsStream(fileName);
		if (!file.exists()) {
			file.create(inputstream, true, monitor);
		} else {
			file.setContents(inputstream, true, false, monitor);
		}

		// create a Java main class to invoke the process
		fileName = "org/jbpm/eclipse/wizard/project/ProcessMain-" + exampleType + ".java";
		IRuntime runtime = startPage.getRuntime();
		if (runtime.getVersion().getMajor() == 5) {
			fileName += ".v5.template";
		} else {
			fileName += ".template";
		}
		
		folder = project.getProject().getFolder("src/main/java");
		IPackageFragmentRoot packageFragmentRoot = project.getPackageFragmentRoot(folder);
		IPackageFragment packageFragment = packageFragmentRoot.createPackageFragment("com.sample", true, monitor);
		inputstream = getClass().getClassLoader().getResourceAsStream(fileName);
		packageFragment.createCompilationUnit("ProcessMain.java", new String(FileUtils.readStream(inputstream)), true,
				monitor);

		
		// create persistence.xml
		if (runtime.getVersion().getMajor() == 5) {
			if ("advanced".equals(exampleType)) {
	        	folder = project.getProject().getFolder("src/main/resources/META-INF");
	    		FileUtils.createFolder(folder, monitor);
	            inputstream = getClass().getClassLoader().getResourceAsStream(
	            	"org/jbpm/eclipse/wizard/project/ProcessLauncher-advanced-persistence.xml.template");
	            file = folder.getFile("persistence.xml");
	            if (!file.exists()) {
	                file.create(inputstream, true, monitor);
	            } else {
	                file.setContents(inputstream, true, false, monitor);
	            }
			}
		}
	}

    /**
     * Create the sample process junit test file.
     */
	private void createProcessSampleJUnit(IJavaProject project, String exampleType, IProgressMonitor monitor)
			throws JavaModelException, IOException {
		String s = "org/jbpm/eclipse/wizard/project/ProcessJUnit-" + exampleType + ".java";
		IRuntime runtime = startPage.getRuntime();
		if (runtime.getVersion().getMajor()==5) {
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
                FileUtils.readStream(inputstream)), true, monitor);
	}
	
    @Override
	protected void createKJarArtifacts(IJavaProject project, IProgressMonitor monitor) {
    	try {
		    String fileName = "org/jbpm/eclipse/wizard/project/kmodule.xml.template";
	        IFolder folder = project.getProject().getFolder("src/main/resources/META-INF");
	        FileUtils.createFolder(folder, monitor);
	        IFile file = folder.getFile("kmodule.xml");
	        InputStream inputstream = getClass().getClassLoader().getResourceAsStream(fileName);
	        if (!file.exists()) {
	            file.create(inputstream, true, monitor);
	        } else {
	            file.setContents(inputstream, true, false, monitor);
	        }
	        
        	String projectName = project.getProject().getName();
            String groupId = "com.sample";
            String artifactId = projectName;
            String version = "1.0.0-SNAPSHOT";
			FileUtils.createProjectFile(project, monitor, FileUtils.generatePomProperties(groupId, artifactId, version), "src/main/resources/META-INF/maven", "pom.properties");
    	}
    	catch (CoreException ex) {
    		ex.printStackTrace();
    	}
    }

	@Override
	protected void createMavenArtifacts(IJavaProject project, IProgressMonitor monitor) {
		try {
        	String projectName = project.getProject().getName();
        	String runtimeVersion = startPage.getRuntime().getVersion().toString();
            String groupId = "com.sample";
            String artifactId = projectName;
            String version = "1.0.0-SNAPSHOT";
        	if (startPage.getInitialProjectContent() == IKieProjectWizardPage.SAMPLE_FILES_PROJECT) {
        		groupId = sampleFilesProjectPage.getPomGroupId();
        		artifactId = sampleFilesProjectPage.getPomArtifactId();
        		version = sampleFilesProjectPage.getPomVersion();
        	}
        	else if (startPage.getInitialProjectContent() == IKieProjectWizardPage.EMPTY_PROJECT) {
        		groupId = emptyProjectPage.getPomGroupId();
        		artifactId = emptyProjectPage.getPomArtifactId();
        		version = emptyProjectPage.getPomVersion();
        	}
        	
			FileUtils.createProjectFile(project, monitor, FileUtils.generatePomProperties(groupId, artifactId, version), "src/main/resources/META-INF/maven", "pom.properties");
			String fileName = "org/jbpm/eclipse/wizard/project/maven-pom.xml.template";
			InputStream inputstream = getClass().getClassLoader().getResourceAsStream(fileName);
            FileUtils.createProjectFile(project, monitor, 
            		FileUtils.generatePom(inputstream, runtimeVersion, groupId, artifactId, version),
            		null, "pom.xml");
		}
		catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/* (non-Javadoc)
	 * @see org.kie.eclipse.wizard.project.AbstractKieProjectWizard#createStartPage(java.lang.String)
	 * Create the Wizard Start Page
	 */
	@Override
	protected IKieProjectWizardPage createStartPage(String pageId) {
		return new AbstractKieProjectStartWizardPage(pageId) {
			@Override
			public String getTitle() {
				return "Create New jBPM Project";
			}
		};
	}
	
	/* (non-Javadoc)
	 * @see org.kie.eclipse.wizard.project.AbstractKieProjectWizard#createEmptyProjectPage(java.lang.String)
	 * Create the Empty Project Wizard Page
	 */
	@Override
	protected IKieProjectWizardPage createEmptyProjectPage(String pageId) {
		emptyProjectPage = new EmptyJBPMProjectWizardPage(EMPTY_PROJECT_PAGE);
		return emptyProjectPage;
	}

	/* (non-Javadoc)
	 * @see org.kie.eclipse.wizard.project.AbstractKieProjectWizard#createSampleFilesProjectPage(java.lang.String)
	 * Create the Sample Files Project Wizard Page
	 */
	@Override
	protected IKieProjectWizardPage createSampleFilesProjectPage(String pageId) {
		sampleFilesProjectPage = new SampleJBPMProjectWizardPage(SAMPLE_FILES_PROJECT_PAGE);
		return sampleFilesProjectPage;
	}

	/* (non-Javadoc)
	 * @see org.kie.eclipse.wizard.project.AbstractKieProjectWizard#createOnlineExampleProjectPage(java.lang.String)
	 * Create the Online Example Project Wizard Page
	 */
	@Override
	protected IKieProjectWizardPage createOnlineExampleProjectPage(String pageId) {
		return new AbstractKieOnlineExampleProjectWizardPage(ONLINE_EXAMPLE_PROJECT_PAGE) {
			@Override
			public String getTitle() {
				return "Create jBPM Projects from Online Examples";
			}
			
			@Override
			public String getDescription() {
				return "Select jBPM Example Projects";
			}

			@Override
			public IRuntimeManager getRuntimeManager() {
				return JBPMRuntimeManager.getDefault();
			}
			
			@Override
			public String getProductId() {
				return "jbpm";
			}
		};
	}
	
	/**
	 * Implementation for the Empty Project Wizard Page
	 */
	class EmptyJBPMProjectWizardPage extends AbstractKieEmptyProjectWizardPage implements IKieSampleFilesProjectWizardPage {
		public EmptyJBPMProjectWizardPage(String pageName) {
			super(pageName);
			setTitle("Create New Empty jBPM Project");
			setDescription("Select the type of jBPM Project");
		}

		@Override
		protected void createControls(Composite parent) {
		}

		@Override
		public IRuntimeManager getRuntimeManager() {
			return JBPMRuntimeManager.getDefault();
		}

		@Override
		protected IRuntime createRuntime() {
			return new JBPMRuntime();
		}

		@Override
		public int showRuntimePreferenceDialog() {
			return PreferencesUtil.createPreferenceDialogOn(getShell(),
					JBPMProjectPreferencePage.PREF_ID,
					new String[] { JBPMProjectPreferencePage.PROP_ID }, new HashMap()).open();
		}

		@Override
		public String getProductName() {
			return "jBPM";
		}
		
		@Override
		public String getProductId() {
			return "jbpm";
		}
	}
	
	class SampleJBPMProjectWizardPage extends EmptyJBPMProjectWizardPage {
		private Button simpleProcessButton;
		private Button advancedProcessButton;
		private Button addSampleJUnitTestCodeButton;
		private boolean addSampleJUnit = false;
		private String sampleType = "simple";

		public SampleJBPMProjectWizardPage(String pageName) {
			super(pageName);
			setTitle("Create New jBPM Project with Sample Files");
			setDescription("Select the samples to be included");
		}

		@Override
		protected void createControls(Composite parent) {
			simpleProcessButton = createRadioButton(parent,
				"Add a simple hello world process");
			simpleProcessButton.setSelection(true);
			simpleProcessButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (((Button) e.widget).getSelection()) {
						sampleType = "simple";
					}
				}
			});
			advancedProcessButton = createRadioButton(parent,
				"Add a more advanced process including human tasks and persistence");
			advancedProcessButton.setSelection(false);
			advancedProcessButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					if (((Button) e.widget).getSelection()) {
						sampleType = "advanced";
					}
				}
			});

			addSampleJUnitTestCodeButton = createCheckBox(parent,
				"Also include a sample JUnit test for the process");
			addSampleJUnitTestCodeButton.setSelection(addSampleJUnit);
			addSampleJUnitTestCodeButton.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					addSampleJUnit = ((Button) e.widget).getSelection();
				}
			});
		}
		
		public String getSampleType() {
    		return sampleType;
		}
		
		public boolean shouldCreateJUnitFile() {
    		return addSampleJUnit;
		}
	}
}
