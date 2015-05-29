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

package org.kie.eclipse.wizard.project;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.kie.eclipse.runtime.IRuntime;
import org.kie.eclipse.runtime.IRuntimeManager;

public abstract class AbstractKieProjectRuntimeWizardPage extends WizardPage {

    private List<IRuntime> runtimes = new ArrayList<IRuntime>();
    private boolean isDefaultRuntime = true;
    private IRuntime selectedRuntime;
    private String runtimeId;
    private Button projectSpecificRuntime;
    private Combo runtimesCombo;
    private Combo versionsCombo;
    
    private Composite mavenPanel;
    private String groupId = "";
    private String artifactId = "";
    private String version = "";

    protected IRuntimeManager runtimeManager;
    private String[] runtimeNames;
    private String[] runtimeIds;
    
    public AbstractKieProjectRuntimeWizardPage(String pageName) {
        super(pageName);
        runtimeManager = getRuntimeManager();
        runtimeNames = runtimeManager.getAllRuntimeNames();
        runtimeIds = runtimeManager.getAllRuntimeIds();

        setTitle("Runtime");
        setDescription("Select a Runtime");
    }

    abstract public IRuntimeManager getRuntimeManager();
    abstract protected IRuntime createRuntime();
    
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        composite.setLayout(gridLayout);
        
        IRuntime defaultRuntime = runtimeManager.getDefaultRuntime();
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
                runtimesCombo.setEnabled(!isDefaultRuntime);
            }
        });
        GridData gridData = new GridData();
        gridData.horizontalSpan = 2;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        projectSpecificRuntime.setLayoutData(gridData);
        
        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("Drools Runtime:");
        runtimesCombo = new Combo(composite, SWT.READ_ONLY);
        runtimesCombo.setEnabled(false);
        runtimesCombo.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
            	widgetSelected(e);
            }
            public void widgetSelected(SelectionEvent e) {
            	Integer key = runtimesCombo.getSelectionIndex();
                selectedRuntime = (IRuntime) runtimesCombo.getData(key.toString());
            }
        });
        
        fillRuntimesCombo();

        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        runtimesCombo.setLayoutData(gridData);
        Link changeWorkspaceSettingsLink = createLink(composite, "Configure Drools Runtimes...");
        changeWorkspaceSettingsLink.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));

        /*
        Button createButton = new Button(composite, SWT.PUSH | SWT.LEFT);
        String name = runtimeManager.getBundleRuntimeName();
        createButton.setText("Create a new " + name + "...");
        gridData = new GridData();
        gridData.horizontalSpan = 2;
        createButton.setLayoutData(gridData);
        createButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                createRuntime();
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        */
        
        
        Composite subPanel = new Composite(composite, SWT.NONE);
        gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        subPanel.setLayout(gridLayout);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.horizontalSpan = 2;
        subPanel.setLayoutData(gridData);

        Label versionsLabel = new Label(subPanel, SWT.NONE);
        versionsLabel.setText("Generate code compatible with:");
        versionsCombo = new Combo(subPanel, SWT.READ_ONLY);
        for (int i=0; i<runtimeIds.length; ++i) {
        	versionsCombo.add(runtimeNames[i]);
            versionsCombo.select(i);
        }
        versionsCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	int index = versionsCombo.getSelectionIndex();
            	if (index>=0 && index<runtimeIds.length) {
	                runtimeId = runtimeIds[index];
	            	mavenPanel.setVisible(runtimeManager.isMavenized(runtimeId));
	            	setComplete();
            	}
            }
        });
        setPageComplete(false);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        versionsCombo.setLayoutData(gridData);
        
        mavenPanel = new Composite(composite, SWT.NONE);
        gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        mavenPanel.setLayout(gridLayout);
        gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.horizontalSpan = 2;
        mavenPanel.setLayoutData(gridData);
        
        Label groupLabel = new Label(mavenPanel, SWT.NONE);
        groupLabel.setText("GroupId: ");
        final Text groupField = new Text(mavenPanel, SWT.BORDER);
        groupField.addListener(SWT.Modify, new Listener() {
        	public void handleEvent(Event event) {
        		groupId = groupField.getText();
        		setComplete();
        	}       	
        });
        Label artifactLabel = new Label(mavenPanel, SWT.NONE);
        artifactLabel.setText("ArtifactId: ");
        final Text artifactField = new Text(mavenPanel, SWT.BORDER);
        artifactField.addListener(SWT.Modify, new Listener() {
        	public void handleEvent(Event event) {
        		artifactId = artifactField.getText();
        		setComplete();
        	}       	
        });
        Label versionLabel = new Label(mavenPanel, SWT.NONE);
        versionLabel.setText("Version: ");
        final Text versionField = new Text(mavenPanel, SWT.BORDER);
        versionField.addListener(SWT.Modify, new Listener() {
        	public void handleEvent(Event event) {
        		version = versionField.getText();
        		setComplete();
        	}       	
        });

        setMessage(null);
        setPageComplete(runtimes.size() > 0 && isComplete());
        setControl(composite);
    }

    private void fillRuntimesCombo() {
    	runtimes.clear();
    	for (IRuntime rt : runtimeManager.getConfiguredRuntimes())
    		runtimes.add(rt);

    	if (runtimeManager.getDefaultRuntime()==null) {
    		isDefaultRuntime = false;
			projectSpecificRuntime.setSelection(false);
			projectSpecificRuntime.setEnabled(false);
    	}
    	else
			projectSpecificRuntime.setEnabled(true);


    	if (runtimes.size()==0) {
	    	IRuntime rt = createRuntime();
	    	rt.setName(runtimeManager.getBundleRuntimeName());
	    	runtimes.add(rt);
	    	
	    	setControlVisible(projectSpecificRuntime, false);
	    	setControlVisible(runtimesCombo, true);
	    	runtimesCombo.setEnabled(true);
    	}
    	else {
        	if (runtimes.size()==1) {
    	    	setControlVisible(projectSpecificRuntime, false);
    	    	runtimesCombo.setEnabled(true);
        	}
        	else {
        		setControlVisible(projectSpecificRuntime, true);
    	    	runtimesCombo.setEnabled(!isDefaultRuntime);
        	}
	    	setControlVisible(runtimesCombo, true);
    	}
    	
        setErrorMessage(null);
        runtimesCombo.removeAll();
        Integer key = 0;
        for (IRuntime rt : runtimes) {
        	String name = rt.getName();
        	if (rt.getPath()==null) {
        		name += " (will be created)";
        	}
            runtimesCombo.add(name);
            runtimesCombo.setData(key.toString(), rt);
            ++key;
        }
        
        key = 0;
        runtimesCombo.select(key);
        selectedRuntime = (IRuntime) runtimesCombo.getData(key.toString());

        IRuntime defaultRuntime = runtimeManager.getDefaultRuntime();
        projectSpecificRuntime.setText("Use default Runtime (currently " +
        		(defaultRuntime == null ? "undefined)" : defaultRuntime.getName() + ")"));
    }

    private void setControlVisible(Control control, boolean visible) {
    	Object ld = control.getLayoutData();
    	if (ld instanceof GridData) {
    		((GridData)ld).exclude = !visible;
    	}
    	control.setVisible(visible);
    	control.getParent().layout();
    }
    
    private void setComplete() {
        setPageComplete(isComplete());
    }
    
    abstract protected boolean isComplete();

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

    abstract public int showRuntimePreferenceDialog();
    
    private void openLink() {
    	if (showRuntimePreferenceDialog() == Window.OK)
    		fillRuntimesCombo();
    }

    public IRuntime getRuntime() {
    	return runtimeManager.getEffectiveRuntime(selectedRuntime, isDefaultRuntime);
    }

    public String getRuntimeId() {
        return runtimeId;
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

	@Override
	public IWizardPage getPreviousPage() {
//		selectedRuntime = null;
		return super.getPreviousPage();
	}
}
