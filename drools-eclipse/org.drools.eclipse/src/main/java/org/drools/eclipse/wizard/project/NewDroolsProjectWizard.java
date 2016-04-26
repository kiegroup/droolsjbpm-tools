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

package org.drools.eclipse.wizard.project;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.preferences.DroolsProjectPreferencePage;
import org.drools.eclipse.util.DroolsRuntime;
import org.drools.eclipse.util.DroolsRuntimeManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.kie.eclipse.runtime.IRuntime;
import org.kie.eclipse.runtime.IRuntimeManager;
import org.kie.eclipse.utils.FileUtils;
import org.kie.eclipse.wizard.project.AbstractKieEmptyProjectWizardPage;
import org.kie.eclipse.wizard.project.AbstractKieOnlineExampleProjectWizardPage;
import org.kie.eclipse.wizard.project.AbstractKieProjectStartWizardPage;
import org.kie.eclipse.wizard.project.AbstractKieProjectWizard;
import org.kie.eclipse.wizard.project.IKieProjectWizardPage;
import org.kie.eclipse.wizard.project.IKieSampleFilesProjectWizardPage;
import org.osgi.framework.Bundle;

/**
 * A wizard to create a new Drools project.
 */
public class NewDroolsProjectWizard extends AbstractKieProjectWizard {

    private EmptyDroolsProjectWizardPage emptyProjectPage;
    private SampleDroolsProjectWizardPage sampleFilesProjectPage;

    @Override
	protected void initializeDefaultPageImageDescriptor() {
        ImageDescriptor desc = DroolsEclipsePlugin.getImageDescriptor("icons/drools-large.PNG");
        setDefaultPageImageDescriptor(desc);
    }

    @Override
	protected void createOutputLocation(IJavaProject project, IProgressMonitor monitor)
            throws JavaModelException, CoreException {
        IFolder folder = FileUtils.createFolder(project, "target/classes", monitor);
        IPath path = folder.getFullPath();
        project.setOutputLocation(path, null);
    }

    @Override
	protected void createInitialContent(IJavaProject javaProject, IProgressMonitor monitor)
            throws CoreException, JavaModelException, IOException {
    	if (startPage.getInitialProjectContent() == IKieProjectWizardPage.SAMPLE_FILES_PROJECT) {
            if (sampleFilesProjectPage.shouldCreateJavaRuleFile()) {
                createRuleSampleLauncher(javaProject);
            }
            if (sampleFilesProjectPage.shouldCreateRuleFile()) {
                createRule(javaProject, monitor);
            }
            if (sampleFilesProjectPage.shouldCreateDecisionTableFile()) {
                createDecisionTable(javaProject, monitor);
            }
            if (sampleFilesProjectPage.shouldCreateJavaDecisionTableFile()) {
                createDecisionTableSampleLauncher(javaProject);
            }
            if (sampleFilesProjectPage.shouldCreateRuleFlowFile()) {
                createRuleFlow(javaProject, monitor);
            }
            if (sampleFilesProjectPage.shouldCreateJavaRuleFlowFile()) {
                createRuleFlowSampleLauncher(javaProject);
            }
            sampleFilesProjectPage.shouldCreateKJarProject();
			createKJarArtifacts(javaProject, monitor);
			createMavenArtifacts(javaProject, monitor);
    	}
    	else if (startPage.getInitialProjectContent() == IKieProjectWizardPage.EMPTY_PROJECT) {
			createKJarArtifacts(javaProject, monitor);
			createMavenArtifacts(javaProject, monitor);
    	}
    	else {
    		super.createInitialContent(javaProject, monitor);
    	}
	    createLoggerFile(javaProject, monitor);
    }

    private void createLoggerFile(IJavaProject project, IProgressMonitor monitor) throws CoreException {
	    String fileName = "org/drools/eclipse/wizard/project/logback-test.xml.template";
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
    
    @Override
	protected void createMavenArtifacts(IJavaProject project, IProgressMonitor monitor) {
        try {
        	String projectName = project.getProject().getName();
        	String runtimeVersion = DroolsEclipsePlugin.getDefault().getBundle().getVersion().toString().replace("qualifier", "Final");
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
			String fileName = "org/drools/eclipse/wizard/project/maven-pom.xml.template";
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
    
    @Override
	protected void createKJarArtifacts(IJavaProject project, IProgressMonitor monitor) {
        try {
	        if (startPage.getRuntime().getVersion().startsWith("6") || startPage.getRuntime().getVersion().startsWith("7")) {
	        	FileUtils.createProjectFile(project, monitor, generateKModule(), "src/main/resources/META-INF", "kmodule.xml");
	        }
		}
		catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    private InputStream generateKModule() {
    	StringBuilder sb = new StringBuilder();
    	sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
    	sb.append("<kmodule xmlns=\"http://jboss.org/kie/6.0.0/kmodule\">\n");
    	
        if (sampleFilesProjectPage.shouldCreateRuleFile() || sampleFilesProjectPage.shouldCreateJavaRuleFile()) {
        	sb.append("    <kbase name=\"rules\" packages=\"rules\">\n");
        	sb.append("        <ksession name=\"ksession-rules\"/>\n");
        	sb.append("    </kbase>\n");
        }
        if (sampleFilesProjectPage.shouldCreateDecisionTableFile() || sampleFilesProjectPage.shouldCreateJavaDecisionTableFile()) {
        	sb.append("    <kbase name=\"dtables\" packages=\"dtables\">\n");
        	sb.append("        <ksession name=\"ksession-dtables\"/>\n");
        	sb.append("    </kbase>\n");
        }
        if (sampleFilesProjectPage.shouldCreateRuleFlowFile() || sampleFilesProjectPage.shouldCreateJavaRuleFlowFile()) {
        	sb.append("    <kbase name=\"process\" packages=\"process\">\n");
        	sb.append("        <ksession name=\"ksession-process\"/>\n");
        	sb.append("    </kbase>\n");
        }
        
        sb.append("</kmodule>\n");
    	
        return new ByteArrayInputStream(sb.toString().getBytes());
    }
    
    /**
     * Create the sample rule launcher file.
     */
    private void createRuleSampleLauncher(IJavaProject project)
            throws JavaModelException, IOException {

        String version = startPage.getRuntime().getVersion();
        if (version.startsWith("4")) {
            createProjectJavaFile(project, "org/drools/eclipse/wizard/project/RuleLauncherSample_4.java.template", "DroolsTest.java");
        } else if (version.startsWith("5")) {
            createProjectJavaFile(project, "org/drools/eclipse/wizard/project/RuleLauncherSample_5.java.template", "DroolsTest.java");
        } else if (version.startsWith("6") || version.startsWith("7")) {
            createProjectJavaFile(project, "org/drools/eclipse/wizard/project/RuleLauncherSample_6.java.template", "DroolsTest.java");
        }
    }

    /**
     * Create the sample decision table launcher file.
     */
    private void createDecisionTableSampleLauncher(IJavaProject project)
            throws JavaModelException, IOException {
        
        String version = startPage.getRuntime().getVersion();
        if (version.startsWith("4")) {
            createProjectJavaFile(project, "org/drools/eclipse/wizard/project/DecisionTableLauncherSample_4.java.template", "DecisionTableTest.java");
        } else if (version.startsWith("5")) {
            createProjectJavaFile(project, "org/drools/eclipse/wizard/project/DecisionTableLauncherSample_5.java.template", "DecisionTableTest.java");
        } else if (version.startsWith("6") || version.startsWith("7")) {
            createProjectJavaFile(project, "org/drools/eclipse/wizard/project/DecisionTableLauncherSample_6.java.template", "DecisionTableTest.java");
        }
    }

	private void createProjectJavaFile(IJavaProject project, String templateFile, String javaFile) throws JavaModelException, IOException {
		IFolder folder = project.getProject().getFolder("src/main/java");
        IPackageFragmentRoot packageFragmentRoot = project.getPackageFragmentRoot(folder);
        IPackageFragment packageFragment = packageFragmentRoot.createPackageFragment("com.sample", true, null);
        InputStream inputstream = getClass().getClassLoader().getResourceAsStream(templateFile);
        packageFragment.createCompilationUnit(javaFile, new String(FileUtils.readStream(inputstream)), true, null);
	}
    
	private void createProjectFile(IJavaProject project, IProgressMonitor monitor, String templateFile, String folderName, String fileName) throws CoreException {
        InputStream inputstream = getClass().getClassLoader().getResourceAsStream(templateFile);
        FileUtils.createProjectFile(project, monitor, inputstream, folderName, fileName);
	}

	/**
     * Create the sample rule file.
     */
    private void createRule(IJavaProject project, IProgressMonitor monitor) throws CoreException {
        if (startPage.getRuntime().getVersion().startsWith("6") || startPage.getRuntime().getVersion().startsWith("7")) {
        	FileUtils.createFolder(project, "src/main/resources/rules", monitor);
            createProjectFile(project, monitor, "org/drools/eclipse/wizard/project/Sample.drl.template", "src/main/resources/rules", "Sample.drl");
        } else {
            createProjectFile(project, monitor, "org/drools/eclipse/wizard/project/Sample.drl.template", "src/main/rules", "Sample.drl");
        }
    }

    /**
     * Create the sample decision table file.
     */
    private void createDecisionTable(IJavaProject project, IProgressMonitor monitor) throws CoreException {
        if (startPage.getRuntime().getVersion().startsWith("6") || startPage.getRuntime().getVersion().startsWith("7")) {
        	FileUtils.createFolder(project, "src/main/resources/dtables", monitor);
        	createProjectFile(project, monitor, "org/drools/eclipse/wizard/project/Sample.xls.template", "src/main/resources/dtables", "Sample.xls");
    	} else {
        	createProjectFile(project, monitor, "org/drools/eclipse/wizard/project/Sample.xls.template", "src/main/rules", "Sample.xls");
    	}
    }

    /**
     * Create the sample RuleFlow file.
     */
    private void createRuleFlow(IJavaProject project, IProgressMonitor monitor) throws CoreException {

        String version = startPage.getRuntime().getVersion();
        if (version.startsWith("4")) {
        	createProjectFile(project, monitor, "org/drools/eclipse/wizard/project/ruleflow_4.rf.template", "src/main/rules", "ruleflow.rf");
        	createProjectFile(project, monitor, "org/drools/eclipse/wizard/project/ruleflow_4.rfm.template", "src/main/rules", "ruleflow.rfm");
        	createProjectFile(project, monitor, "org/drools/eclipse/wizard/project/ruleflow_4.drl.template", "src/main/rules", "ruleflow.drl");
        } else if (version.startsWith("5.0")) {
        	createProjectFile(project, monitor, "org/drools/eclipse/wizard/project/ruleflow.rf.template", "src/main/rules", "ruleflow.rf");
        } else if (version.startsWith("5")) {
        	createProjectFile(project, monitor, "org/drools/eclipse/wizard/project/sample.bpmn.template", "src/main/rules", "sample.bpmn");
        } else {
        	FileUtils.createFolder(project, "src/main/resources/process", monitor);
        	createProjectFile(project, monitor, "org/drools/eclipse/wizard/project/sample.bpmn.template", "src/main/resources/process", "sample.bpmn");
        }
    }

    /**
     * Create the sample RuleFlow launcher file.
     */
    private void createRuleFlowSampleLauncher(IJavaProject project)
            throws JavaModelException, IOException {
        
        String s;
        String version = startPage.getRuntime().getVersion();
        if (version.startsWith("4")) {
            s = "org/drools/eclipse/wizard/project/RuleFlowLauncherSample_4.java.template";
        } else if (version.startsWith("5.0")) {
            s = "org/drools/eclipse/wizard/project/RuleFlowLauncherSample.java.template";
        } else if (version.startsWith("5")) {
            s = "org/drools/eclipse/wizard/project/ProcessLauncherSample_bpmn_5.java.template";
        } else {
            s = "org/drools/eclipse/wizard/project/ProcessLauncherSample_bpmn_6.java.template";
        }
        createProjectJavaFile(project, s, "ProcessTest.java");
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
				return "Create New Drools Project";
			}
		};
	}
	
	/* (non-Javadoc)
	 * @see org.kie.eclipse.wizard.project.AbstractKieProjectWizard#createEmptyProjectPage(java.lang.String)
	 * Create the Empty Project Wizard Page
	 */
	@Override
	protected IKieProjectWizardPage createEmptyProjectPage(String pageId) {
		emptyProjectPage = new EmptyDroolsProjectWizardPage(EMPTY_PROJECT_PAGE);
		return emptyProjectPage;
	}

	/* (non-Javadoc)
	 * @see org.kie.eclipse.wizard.project.AbstractKieProjectWizard#createSampleFilesProjectPage(java.lang.String)
	 * Create the Sample Files Project Wizard Page
	 */
	@Override
	protected IKieProjectWizardPage createSampleFilesProjectPage(String pageId) {
		sampleFilesProjectPage = new SampleDroolsProjectWizardPage(SAMPLE_FILES_PROJECT_PAGE);
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
				return "Create Drools Projects from Online Examples";
			}
			
			@Override
			public String getDescription() {
				return "Select Drools Example Projects";
			}

			@Override
			public IRuntimeManager getRuntimeManager() {
				return DroolsRuntimeManager.getDefault();
			}
		
			@Override
			public String getProductId() {
				return "drools";
			}
		};
	}
	
	/**
	 * Implementation for the Empty Project Wizard Page
	 */
	class EmptyDroolsProjectWizardPage extends AbstractKieEmptyProjectWizardPage implements IKieSampleFilesProjectWizardPage {
		public EmptyDroolsProjectWizardPage(String pageName) {
			super(pageName);
			setTitle("Create New Empty Drools Project");
			setDescription("Select the type of Drools Project");
		}

		@Override
		protected void createControls(Composite parent) {
		}
		
		@Override
		public IRuntimeManager getRuntimeManager() {
			return DroolsRuntimeManager.getDefault();
		}

		@Override
		protected IRuntime createRuntime() {
			return new DroolsRuntime();
		}

		@Override
		public int showRuntimePreferenceDialog() {
	        return PreferencesUtil.createPreferenceDialogOn(getShell(),
	                DroolsProjectPreferencePage.PREF_ID,
	                new String[] { DroolsProjectPreferencePage.PROP_ID }, null).open();
		}

		@Override
		public String getProductName() {
			return "Drools";
		}

		@Override
		public String getProductId() {
			return "drools";
		}
	}
	
	/**
	 * Implementation for the Sample Files Project Wizard Page
	 */
	class SampleDroolsProjectWizardPage extends EmptyDroolsProjectWizardPage {
	    private boolean addSampleRule = true;
	    private boolean addSampleDecisionTableCode = true;
	    private boolean addSampleRuleFlow = true;

		public SampleDroolsProjectWizardPage(String pageName) {
			super(pageName);
			setTitle("Create New Drools Project with Sample Files");
			setDescription("Select the sample files to be included");
		}

		@Override
		protected void createControls(Composite parent) {
			Composite composite = new Composite(parent, SWT.NONE);
	        composite.setLayout(new GridLayout(1, false));
	        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
	        gd.verticalIndent = 10;
	        gd.horizontalIndent = 10;
	        composite.setLayoutData(gd);
	        composite.setLayoutData(gd);

	        final Button addSampleRuleButton = createCheckBox(composite,
	            "Add a sample HelloWorld rule file to this project.");
	        addSampleRuleButton.setSelection(addSampleRule);
	        addSampleRuleButton.addSelectionListener(new SelectionAdapter() {
	            @Override
				public void widgetSelected(SelectionEvent e) {
	                addSampleRule = ((Button) e.widget).getSelection();
	            }
	        });
	        
	        final Button addSampleDecisionTableCodeButton = createCheckBox(composite,
	            "Add a sample HelloWorld decision table file to this project.");
	        addSampleDecisionTableCodeButton.setSelection(addSampleDecisionTableCode);
	        addSampleDecisionTableCodeButton.addSelectionListener(new SelectionAdapter() {
	            @Override
				public void widgetSelected(SelectionEvent e) {
	                addSampleDecisionTableCode = ((Button) e.widget).getSelection();
	            }
	        });
	        
	        final Button addSampleRuleFlowButton = createCheckBox(composite,
	            "Add a sample HelloWorld process file to this project.");
	        addSampleRuleFlowButton.setSelection(addSampleRuleFlow);
	        addSampleRuleFlowButton.addSelectionListener(new SelectionAdapter() {
	            @Override
				public void widgetSelected(SelectionEvent e) {
	                addSampleRuleFlow = ((Button) e.widget).getSelection();
	            }
	        });
		}

	    public boolean shouldCreateRuleFile() {
	        return addSampleRule;
	    }

	    public boolean shouldCreateJavaRuleFile() {
	        return addSampleRule; //addSampleJavaRuleCode;
	    }

	    public boolean shouldCreateDecisionTableFile() {
	        return addSampleDecisionTableCode;
	    }

	    public boolean shouldCreateJavaDecisionTableFile() {
	        return addSampleDecisionTableCode; //addSampleJavaDecisionTableCode;
	    }

	    public boolean shouldCreateRuleFlowFile() {
	        return addSampleRuleFlow;
	    }

	    public boolean shouldCreateJavaRuleFlowFile() {
	        return addSampleRuleFlow; //addSampleJavaRuleFlowCode;
	    }
	}
}
