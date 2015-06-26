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

package org.kie.eclipse.preferences;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.kie.eclipse.runtime.IRuntime;
import org.kie.eclipse.runtime.IRuntimeManager;

public abstract class AbstractProjectPreferencePage extends PropertyAndPreferencePage {

	private IRuntimeManager runtimeManager;
    private Combo runtimeCombo;

    public AbstractProjectPreferencePage() {
        setTitle("Project Preferences");
        runtimeManager = getRuntimeManager();
    }

    protected Control createPreferenceContent(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        composite.setLayout(gridLayout);
        
        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("Runtime:");
        runtimeCombo = new Combo(composite, SWT.READ_ONLY);
        IRuntime[] runtimes = runtimeManager.getConfiguredRuntimes();
        int selection = -1;
        IRuntime currentRuntime = runtimeManager.getRuntime(getProject());
        for (int i = 0; i < runtimes.length; i++) {
            runtimeCombo.add(runtimes[i].getName());
            if (runtimes[i].equals(currentRuntime)) {
                selection = i;
            }
        }
        if (selection != -1) {
            runtimeCombo.select(selection);
        } else if (runtimes.length > 0) {
            runtimeCombo.select(0);
        }
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        runtimeCombo.setLayoutData(gridData);
        return composite;
    }

    protected boolean hasProjectSpecificOptions(IProject project) {
    	String path = ".settings/" + runtimeManager.getSettingsFilename();
        return project.getFile(path).exists();
    }

	public boolean performOk() {
        try {
        	String path = ".settings/" + runtimeManager.getSettingsFilename();
            IFile file = getProject().getFile(path);
            if (useProjectSettings()) {
                String runtime = "<runtime>"
                    + runtimeCombo.getItem(runtimeCombo.getSelectionIndex())
                    + "</runtime>";
                if (!file.exists()) {
                    IFolder folder = getProject().getFolder(".settings");
                    if (!folder.exists()) {
                        folder.create(true, true, null);
                    }
                    file.create(new ByteArrayInputStream(runtime.getBytes()), true, null);
                } else {
                    file.setContents(new ByteArrayInputStream(runtime.getBytes()), true, false, null);
                }
            } else {
                if (file.exists()) {
                    file.delete(true, null);
                }
            }
            getProject().close(null);
            getProject().open(null);
        } catch (Throwable t) {
        	t.printStackTrace();
            return false;
        }
        return super.performOk();
    }


    abstract protected IRuntimeManager getRuntimeManager();
    abstract protected String getPreferencePageID();
    abstract protected String getPropertyPageID();
}
