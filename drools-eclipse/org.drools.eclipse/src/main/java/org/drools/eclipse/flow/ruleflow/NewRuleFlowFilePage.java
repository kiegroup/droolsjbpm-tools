package org.drools.eclipse.flow.ruleflow;
/*
 * Copyright 2005 JBoss Inc
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

import java.io.InputStream;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;

/**
 * Page for creating a new RuleFlow file.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class NewRuleFlowFilePage extends WizardNewFileCreationPage {

    private IWorkbench workbench;
    private String targetRuntime;

    public NewRuleFlowFilePage(IWorkbench workbench, IStructuredSelection selection) {
        super("createRuleFlowPage", selection);
        setTitle("Create Flow File");
        setDescription("Create a new Flow file");
        this.workbench = workbench;
    }

    public void createControl(Composite parent) {
        super.createControl(parent);
        setPageComplete(true);
    }
    
    public void setTargetRuntime(String targetRuntime) {
    	this.targetRuntime = targetRuntime;
    }

    public boolean finish() {
        String fileName = getFileName();
        if (NewRuleFlowFileRuntimePage.DROOLS5.equals(targetRuntime)) {
	        if (!fileName.endsWith(".rf")) {
	            setFileName(fileName + ".rf");
	        }
        } else if (NewRuleFlowFileRuntimePage.DROOLS5_1.equals(targetRuntime)) {
	        if (!fileName.endsWith(".bpmn")) {
	            setFileName(fileName + ".bpmn");
	        }
        }
        org.eclipse.core.resources.IFile newFile = createNewFile();
        if (newFile == null)
            return false;
        try {
            IWorkbenchWindow dwindow = workbench.getActiveWorkbenchWindow();
            org.eclipse.ui.IWorkbenchPage page = dwindow.getActivePage();
            if (page != null)
                IDE.openEditor(page, newFile, true);
        } catch (PartInitException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
    
    protected InputStream getInitialContents() {
        if (NewRuleFlowFileRuntimePage.DROOLS5.equals(targetRuntime)) {
	        String s = "org/drools/eclipse/flow/ruleflow/SampleRuleFlow.rf.template";
	        return getClass().getClassLoader().getResourceAsStream(s);
        } else if (NewRuleFlowFileRuntimePage.DROOLS5_1.equals(targetRuntime)) {
            String s = "org/drools/eclipse/flow/bpmn2/Sample.bpmn.template";
	        return getClass().getClassLoader().getResourceAsStream(s);
        } else {
        	throw new IllegalArgumentException("Unknown target runtime " + targetRuntime);
        }
    }
}
