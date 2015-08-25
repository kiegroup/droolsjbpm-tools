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

import java.util.HashMap;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.dialogs.PreferencesUtil;
import org.jbpm.eclipse.preferences.JBPMProjectPreferencePage;
import org.jbpm.eclipse.util.JBPMRuntime;
import org.jbpm.eclipse.util.JBPMRuntimeManager;

public class NewJBPMProjectRuntimeWizardPage extends WizardPage {

    public static final String JBPM5 = "jBPM 5";
    public static final String JBPM6 = "jBPM 6";

    private boolean isDefaultRuntime = true;
	private String selectedRuntime;
    private String generationType = JBPM6;
	private Button projectSpecificRuntime;
	private Combo jBPMRuntimeCombo;
    private Combo jbpmGenerateCombo;
	
	public NewJBPMProjectRuntimeWizardPage() {
		super("extendedNewProjectRuntimePage");
		setTitle("jBPM Runtime");
        setDescription("Select a jBPM Runtime");
	}
	
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        composite.setLayout(gridLayout);
        
        JBPMRuntime defaultRuntime = JBPMRuntimeManager.getDefaultJBPMRuntime();
		projectSpecificRuntime = createCheckBox(composite,
			"Use default jBPM Runtime (currently "
				+ (defaultRuntime == null ? "undefined)" : defaultRuntime.getName() + ")"));
		projectSpecificRuntime.setSelection(true);
		projectSpecificRuntime.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// do nothing
			}
			public void widgetSelected(SelectionEvent e) {
				isDefaultRuntime = ((Button) e.widget).getSelection();
				jBPMRuntimeCombo.setEnabled(!isDefaultRuntime);
			}
		});
		GridData gridData = new GridData();
		gridData.horizontalSpan = 2;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        projectSpecificRuntime.setLayoutData(gridData);
        
        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("jBPM Runtime: ");
        jBPMRuntimeCombo = new Combo(composite, SWT.READ_ONLY);
        jBPMRuntimeCombo.setEnabled(false);
        jBPMRuntimeCombo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				selectedRuntime = jBPMRuntimeCombo.getText();
			}
			public void widgetSelected(SelectionEvent e) {
				selectedRuntime = jBPMRuntimeCombo.getText();
			}
        });
        JBPMRuntime[] runtimes = JBPMRuntimeManager.getJBPMRuntimes();
        if (runtimes.length == 0) {
        	setErrorMessage("No jBPM Runtimes have been defined, configure workspace settings first");
        } else {
	        setErrorMessage(null);
	        for (int i = 0; i < runtimes.length; i++) {
	        	jBPMRuntimeCombo.add(runtimes[i].getName());
	        }
	        jBPMRuntimeCombo.select(0);
	        selectedRuntime = jBPMRuntimeCombo.getText();
        }
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        jBPMRuntimeCombo.setLayoutData(gridData);
        Link changeWorkspaceSettingsLink = createLink(composite, "Configure Workspace Settings...");
        changeWorkspaceSettingsLink.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

		Composite subPanel = new Composite(composite, SWT.NONE);
		gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        subPanel.setLayout(gridLayout);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.horizontalSpan = 2;
        subPanel.setLayoutData(gridData);
        
        Label generateLabel = new Label(subPanel, SWT.NONE);
        generateLabel.setText("Generate code compatible with: ");
        jbpmGenerateCombo = new Combo(subPanel, SWT.READ_ONLY);
        jbpmGenerateCombo.add("jBPM 5");
        jbpmGenerateCombo.add("jBPM 6 or above");
        jbpmGenerateCombo.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                generationType = jbpmGenerateCombo.getText();
            }
            public void widgetSelected(SelectionEvent e) {
                generationType = jbpmGenerateCombo.getText();
            }
        });
        jbpmGenerateCombo.select(1);
        generationType = JBPM6;
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        jbpmGenerateCombo.setLayoutData(gridData);
        
        setMessage(null);
        setPageComplete(runtimes.length > 0);
        setControl(composite);
	}
	
	private Button createCheckBox(Composite group, String label) {
        Button button = new Button(group, SWT.CHECK | SWT.LEFT);
        button.setText(label);
        GridData data = new GridData();
        button.setLayoutData(data);
        return button;
    }
	
	private Link createLink(Composite composite, String text) {
		Link link= new Link(composite, SWT.NONE);
		link.setFont(composite.getFont());
		link.setText("<A>" + text + "</A>");  //$NON-NLS-1$//$NON-NLS-2$
		link.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				openLink();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
				openLink();
			}
		});
		return link;
	}
	
	private void openLink() {
		PreferencesUtil.createPreferenceDialogOn(getShell(),
			JBPMProjectPreferencePage.PREF_ID,
			new String[] { JBPMProjectPreferencePage.PROP_ID }, new HashMap()).open();
		jBPMRuntimeCombo.removeAll();
		JBPMRuntime[] runtimes = JBPMRuntimeManager.getJBPMRuntimes();
        if (runtimes.length == 0) {
        	setPageComplete(false);
        	setErrorMessage("No jBPM Runtimes have been defined, please do this first");
        } else {
        	setPageComplete(true);
        	setErrorMessage(null);
	        for (int i = 0; i < runtimes.length; i++) {
	        	jBPMRuntimeCombo.add(runtimes[i].getName());
	        }
	        jBPMRuntimeCombo.select(0);
			selectedRuntime = jBPMRuntimeCombo.getText();
        }
        JBPMRuntime defaultRuntime = JBPMRuntimeManager.getDefaultJBPMRuntime();
		projectSpecificRuntime.setText("Use default jBPM Runtime (currently "
			+ (defaultRuntime == null ? "undefined)" : defaultRuntime.getName() + ")"));
	}
	
	public JBPMRuntime getJBPMRuntime() {
		if (isDefaultRuntime) {
			return null;
		}
		return JBPMRuntimeManager.getJBPMRuntime(selectedRuntime);
	}

	public String getGenerationType() {
        return generationType;
    }

}
