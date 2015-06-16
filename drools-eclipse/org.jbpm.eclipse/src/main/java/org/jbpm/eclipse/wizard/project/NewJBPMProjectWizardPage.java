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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.kie.eclipse.wizard.project.AbstractKieProjectMainWizardPage;

public class NewJBPMProjectWizardPage extends AbstractKieProjectMainWizardPage {

	private Button simpleProcessButton;
	private Button advancedProcessButton;
	private Button addSampleJUnitTestCodeButton;
	private boolean addSampleJUnit = true;
	private String typeOfExample = "simple";

	public NewJBPMProjectWizardPage(String pageId) {
		super(pageId);
		setTitle("New jBPM Project");
        setDescription("Create a new jBPM Project");
	}

	protected void createSampleFilesProjectControls(Composite parent) {
		Label label = new Label(parent, SWT.LEFT);
		label.setText("I want to create:");
		simpleProcessButton = createRadioButton(parent,
			"a simple hello world process");
		simpleProcessButton.setSelection(true);
		simpleProcessButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				if (((Button) e.widget).getSelection()) {
					typeOfExample = "simple";
				}
			}
		});
		advancedProcessButton = createRadioButton(parent,
			"a more advanced process including human tasks and persistence");
		advancedProcessButton.setSelection(false);
		advancedProcessButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				if (((Button) e.widget).getSelection()) {
					typeOfExample = "advanced";
				}
			}
		});

		addSampleJUnitTestCodeButton = createCheckBox(parent,
			"Add a sample JUnit test for the HelloWorld process.");
		addSampleJUnitTestCodeButton.setSelection(addSampleJUnit);
		addSampleJUnitTestCodeButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				addSampleJUnit = ((Button) e.widget).getSelection();
			}
		});
	}

	private Button createCheckBox(Composite group, String label) {
        Button button = new Button(group, SWT.CHECK | SWT.LEFT);
        button.setText(label);
        GridData data = new GridData();
        button.setLayoutData(data);
        return button;
    }
	
	private Button createRadioButton(Composite group, String label) {
        Button button = new Button(group, SWT.RADIO | SWT.LEFT);
        button.setText(label);
        GridData data = new GridData();
        button.setLayoutData(data);
        return button;
    }
	
	public String getExampleType() {
    	if (getInitialProjectContent() == SAMPLE_FILES_PROJECT)
    		return typeOfExample;
    	return "none";
	}
	
	public boolean createJUnitFile() {
    	if (getInitialProjectContent() == SAMPLE_FILES_PROJECT)
    		return addSampleJUnit;
    	return false;
	}
}
