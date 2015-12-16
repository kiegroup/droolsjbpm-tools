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

package org.kie.eclipse.preferences;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.kie.eclipse.runtime.IRuntime;
import org.kie.eclipse.runtime.IRuntimeManager;
import org.kie.eclipse.runtime.IRuntimeManagerListener;

public abstract class AbstractRuntimesPreferencePage extends PreferencePage 
	implements IWorkbenchPreferencePage, IRuntimeManagerListener {

	private IRuntimeManager runtimeManager;
    private AbstractRuntimesBlock runtimesBlock;

    public AbstractRuntimesPreferencePage() {
        super("Installed Runtimes");
    }

    public AbstractRuntimesPreferencePage(String title) {
        super(title);
    }

    public void init(IWorkbench workbench) {
    }

    public void dispose() {
    	runtimeManager.removeListener(this);
    }    
    
    public void createControl(Composite parent){
        this.runtimeManager = getRuntimeManager();
        super.createControl(parent);
        getDefaultsButton().setVisible(false);
        runtimeManager.addListener(this);
    }

    protected Control createContents(Composite ancestor) {
        initializeDialogUnits(ancestor);
        GridLayout layout= new GridLayout();
        layout.numColumns= 1;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        ancestor.setLayout(layout);
        Label l = new Label(ancestor, SWT.WRAP);
        l.setFont(ancestor.getFont());
        l.setText(
            "Add, remove or edit Runtime definitions. " +
            "By default, the checked Runtime is added to the build " +
            "path of newly created projects.");
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 1;
        gd.widthHint = 300;
        l.setLayoutData(gd);
        l = new Label(ancestor, SWT.NONE);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.heightHint = 1;
        l.setLayoutData(gd);
        runtimesBlock = createRuntimesBlock();
        runtimesBlock.createControl(ancestor);
        IRuntime[] runtimes = runtimeManager.getConfiguredRuntimes();
        runtimesBlock.setRuntimes(runtimes);
        if (runtimesBlock.getDefaultRuntime() == null) {
            setErrorMessage("Select a default Runtime");
        }
        Control control = runtimesBlock.getControl();
        GridData data = new GridData(GridData.FILL_BOTH);
        data.horizontalSpan = 1;
        data.widthHint = 450;
        control.setLayoutData(data);

        runtimesBlock.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                IRuntime runtime = runtimesBlock.getDefaultRuntime();
                if (runtime == null) {
                	if (runtimesBlock.getRuntimes().length==0) {
	                    setErrorMessage("Create a default Runtime");
                	}
                	else {
	                    setErrorMessage("Select a default Runtime");
                	}
                } else {
                    setErrorMessage(null);
                }
            }
        });
        applyDialogFont(ancestor);
        return ancestor;
    }

	public boolean performOk() {
        if (runtimeManager.getDefaultRuntime() != null) {
            MessageDialog.openInformation(getShell(), "Warning",
            "You need to restart Eclipse to update the Runtime of existing projects.");
        }
        runtimeManager.setRuntimes(runtimesBlock.getRuntimes());
        return super.performOk();
    }

	public void runtimeAdded(IRuntime rt) {
		runtimesBlock.setRuntimes(runtimeManager.getConfiguredRuntimes());
	}

	public void runtimeRemoved(IRuntime rt) {
		runtimesBlock.setRuntimes(runtimeManager.getConfiguredRuntimes());
	}

	public void runtimesChanged(IRuntime[] newList) {
		Display.getDefault().asyncExec(new Runnable() { 
			public void run() {
				runtimesBlock.setRuntimes(runtimeManager.getConfiguredRuntimes());
			}
		});
	}

    abstract protected IRuntimeManager getRuntimeManager();
    abstract protected AbstractRuntimesBlock createRuntimesBlock();
}
