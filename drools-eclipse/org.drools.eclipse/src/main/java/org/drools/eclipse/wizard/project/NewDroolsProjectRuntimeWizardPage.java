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

import java.util.HashMap;

import org.drools.eclipse.preferences.DroolsProjectPreferencePage;
import org.drools.eclipse.util.DroolsRuntime;
import org.drools.eclipse.util.DroolsRuntimeManager;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PreferencesUtil;

public class NewDroolsProjectRuntimeWizardPage extends WizardPage {

    public static final String DROOLS4 = "Drools 4.x";
    public static final String DROOLS5 = "Drools 5.0.x";
    public static final String DROOLS5_1 = "Drools 5.1.x or above";
    public static final String DROOLS6 = "Drools 6.0.x";

    private boolean isDefaultRuntime = true;
    private String selectedRuntime;
    private String generationType = DROOLS6;
    private Button projectSpecificRuntime;
    private Combo droolsRuntimeCombo;
    private Combo droolsGenerateCombo;
    
    private Composite gavPanel;
    private String groupId = "";
    private String artifactId = "";
    private String version = "";

    public NewDroolsProjectRuntimeWizardPage() {
        super("extendedNewProjectRuntimePage");
        setTitle("Drools Runtime");
        setDescription("Select a Drools Runtime");
    }

    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        composite.setLayout(gridLayout);
        
        DroolsRuntime defaultRuntime = DroolsRuntimeManager.getDefaultDroolsRuntime();
        projectSpecificRuntime = createCheckBox(composite,
            "Use default Drools Runtime (currently "
                + (defaultRuntime == null ? "undefined)" : defaultRuntime.getName() + ")"));
        projectSpecificRuntime.setSelection(true);
        projectSpecificRuntime.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                // do nothing
            }
            public void widgetSelected(SelectionEvent e) {
                isDefaultRuntime = ((Button) e.widget).getSelection();
                droolsRuntimeCombo.setEnabled(!isDefaultRuntime);
            }
        });
        GridData gridData = new GridData();
        gridData.horizontalSpan = 2;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        projectSpecificRuntime.setLayoutData(gridData);
        
        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("Drools Runtime:");
        droolsRuntimeCombo = new Combo(composite, SWT.READ_ONLY);
        droolsRuntimeCombo.setEnabled(false);
        droolsRuntimeCombo.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                selectedRuntime = droolsRuntimeCombo.getText();
            }
            public void widgetSelected(SelectionEvent e) {
                selectedRuntime = droolsRuntimeCombo.getText();
            }
        });
        DroolsRuntime[] runtimes = DroolsRuntimeManager.getDroolsRuntimes();
        if (runtimes.length == 0) {
            setErrorMessage("No Drools Runtimes have been defined, configure workspace settings first");
        } else {
            setErrorMessage(null);
            for (int i = 0; i < runtimes.length; i++) {
                droolsRuntimeCombo.add(runtimes[i].getName());
            }
            droolsRuntimeCombo.select(0);
            selectedRuntime = droolsRuntimeCombo.getText();
        }
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        droolsRuntimeCombo.setLayoutData(gridData);
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
        generateLabel.setText("Generate code compatible with:");
        droolsGenerateCombo = new Combo(subPanel, SWT.READ_ONLY);
        droolsGenerateCombo.add(DROOLS4);
        droolsGenerateCombo.add(DROOLS5);
        droolsGenerateCombo.add(DROOLS5_1);
        droolsGenerateCombo.add(DROOLS6);
        droolsGenerateCombo.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                generationType = droolsGenerateCombo.getText();
            	gavPanel.setVisible(getGenerationType().equals(DROOLS6));
            	setComplete();
            }
            public void widgetSelected(SelectionEvent e) {
                generationType = droolsGenerateCombo.getText();
            	gavPanel.setVisible(getGenerationType().equals(DROOLS6));
            	setComplete();
            }
        });
        droolsGenerateCombo.select(3);
        setPageComplete(false);
        generationType = DROOLS6;
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        droolsGenerateCombo.setLayoutData(gridData);
        
        gavPanel = new Composite(composite, SWT.NONE);
        gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gavPanel.setLayout(gridLayout);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.horizontalSpan = 2;
        gavPanel.setLayoutData(gridData);
        
        Label groupLabel = new Label(gavPanel, SWT.NONE);
        groupLabel.setText("GroupId:");
        final Text groupField = new Text(gavPanel, SWT.BORDER);
        groupField.addListener(SWT.Modify, new Listener() {
        	public void handleEvent(Event event) {
        		groupId = groupField.getText();
        		setComplete();
        	}       	
        });
        Label artifactLabel = new Label(gavPanel, SWT.NONE);
        artifactLabel.setText("ArtifactId:");
        final Text artifactField = new Text(gavPanel, SWT.BORDER);
        artifactField.addListener(SWT.Modify, new Listener() {
        	public void handleEvent(Event event) {
        		artifactId = artifactField.getText();
        		setComplete();
        	}       	
        });
        Label versionLabel = new Label(gavPanel, SWT.NONE);
        versionLabel.setText("Version:");
        final Text versionField = new Text(gavPanel, SWT.BORDER);
        versionField.addListener(SWT.Modify, new Listener() {
        	public void handleEvent(Event event) {
        		version = versionField.getText();
        		setComplete();
        	}       	
        });

        setMessage(null);
        setPageComplete(runtimes.length > 0 && isComplete());
        setControl(composite);
    }
    
    private void setComplete() {
        setPageComplete(isComplete());
    }
    
    private boolean isComplete() {
    	return !getGenerationType().equals(DROOLS6) || (getGroupId().length() > 0 && getArtifactId().length() > 0 && getVersion().length() > 0);
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
            DroolsProjectPreferencePage.PREF_ID,
            new String[] { DroolsProjectPreferencePage.PROP_ID }, new HashMap()).open();
        droolsRuntimeCombo.removeAll();
        DroolsRuntime[] runtimes = DroolsRuntimeManager.getDroolsRuntimes();
        if (runtimes.length == 0) {
            setPageComplete(false);
            setErrorMessage("No Drools Runtimes have been defined, please do this first");
        } else {
            setPageComplete(true);
            setErrorMessage(null);
            for (int i = 0; i < runtimes.length; i++) {
                droolsRuntimeCombo.add(runtimes[i].getName());
            }
            droolsRuntimeCombo.select(0);
            selectedRuntime = droolsRuntimeCombo.getText();
        }
        DroolsRuntime defaultRuntime = DroolsRuntimeManager.getDefaultDroolsRuntime();
        projectSpecificRuntime.setText("Use default Drools Runtime (currently "
            + (defaultRuntime == null ? "undefined)" : defaultRuntime.getName() + ")"));
    }

    public DroolsRuntime getDroolsRuntime() {
        if (isDefaultRuntime) {
            return null;
        }
        return DroolsRuntimeManager.getDroolsRuntime(selectedRuntime);
    }

    public String getGenerationType() {
        return generationType;
    }

    public String getGroupId() {
    	return groupId;
    }

    public String getArtifactId() {
    	return artifactId;
    }

    public String getVersion() {
    	return version;
    }
}
