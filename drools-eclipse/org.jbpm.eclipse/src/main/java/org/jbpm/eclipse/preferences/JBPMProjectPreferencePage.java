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

package org.jbpm.eclipse.preferences;

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
import org.jbpm.eclipse.JBPMEclipsePlugin;
import org.jbpm.eclipse.util.JBPMRuntime;
import org.jbpm.eclipse.util.JBPMRuntimeManager;

public class JBPMProjectPreferencePage extends PropertyAndPreferencePage {

	public static final String PREF_ID= "org.jbpm.eclipse.preferences.JBPMRuntimesPreferencePage";
	public static final String PROP_ID= "org.jbpm.eclipse.preferences.JBPMProjectPreferencePage";

	private Combo jBPMRuntimeCombo;
	
	public JBPMProjectPreferencePage() {
		setTitle("jBPM Project Preferences");
	}
	
	protected Control createPreferenceContent(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        composite.setLayout(gridLayout);
        
        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("jBPM Runtime: ");
        jBPMRuntimeCombo = new Combo(composite, SWT.LEFT);
        JBPMRuntime[] runtimes = JBPMRuntimeManager.getJBPMRuntimes();
        int selection = -1;
        String currentRuntime = JBPMRuntimeManager.getJBPMRuntime(getProject());
        for (int i = 0; i < runtimes.length; i++) {
        	jBPMRuntimeCombo.add(runtimes[i].getName());
        	if (runtimes[i].getName().equals(currentRuntime)) {
        		selection = i;
        	}
        }
        if (selection != -1) {
        	jBPMRuntimeCombo.select(selection);
        } else if (runtimes.length > 0) {
            jBPMRuntimeCombo.select(0);
        }
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        jBPMRuntimeCombo.setLayoutData(gridData);
		return composite;
	}
	
	protected String getPreferencePageID() {
		return PREF_ID;
	}

	protected String getPropertyPageID() {
		return PROP_ID;
	}

	protected boolean hasProjectSpecificOptions(IProject project) {
		return project.getFile(".settings/.jbpm.runtime").exists();
	}

	public boolean performOk() {
		try {
			IFile file = getProject().getFile(".settings/.jbpm.runtime");
			if (useProjectSettings()) {
				String runtime = "<runtime>"
					+ jBPMRuntimeCombo.getItem(jBPMRuntimeCombo.getSelectionIndex())
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
			JBPMEclipsePlugin.log(t);
			return false;
		}
		return super.performOk();
	}
	
}
