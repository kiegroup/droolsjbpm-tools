/**
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

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class NewJBPMProjectWizardPage extends WizardPage {

	private Button addSampleProcessButton;
	private Button addSampleJavaProcessCodeButton;
	private boolean addSampleJavaProcessCode = true;
	private boolean addSampleProcess = true;
	
	public NewJBPMProjectWizardPage() {
		super("extendedNewProjectPage");
		setTitle("New jBPM Project");
        setDescription("Create a new jBPM Project");
	}
	
	public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setFont(parent.getFont());
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        createControls(composite);
        setPageComplete(true);
        // Show description on opening
        setErrorMessage(null);
        setMessage(null);
        setControl(composite);
	}
	
	private void createControls(Composite parent) {
		addSampleProcessButton = createCheckBox(parent,
			"Add a sample HelloWorld process file to this project.");
		addSampleProcessButton.setSelection(addSampleProcess);
		addSampleProcessButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				addSampleProcess = ((Button) e.widget).getSelection();
			}
		});
		addSampleJavaProcessCodeButton = createCheckBox(parent,
			"Add a sample Java class for loading and executing the HelloWorld process.");
		addSampleJavaProcessCodeButton.setSelection(addSampleJavaProcessCode);
		addSampleJavaProcessCodeButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				addSampleJavaProcessCode = ((Button) e.widget).getSelection();
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
	
	public boolean createProcessFile() {
		return addSampleProcess;
	}
	
	public boolean createJavaProcessFile() {
		return addSampleJavaProcessCode;
	}
}
