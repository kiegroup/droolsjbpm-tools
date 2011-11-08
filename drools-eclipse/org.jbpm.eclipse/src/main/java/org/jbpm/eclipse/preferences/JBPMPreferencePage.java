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

package org.jbpm.eclipse.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.jbpm.eclipse.JBPMEclipsePlugin;

public class JBPMPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	protected Control createContents(Composite parent) {
		final Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        composite.setLayout(gridLayout);
		initializeValues();
		return composite;
	}

	protected IPreferenceStore doGetPreferenceStore() {
        return JBPMEclipsePlugin.getDefault().getPreferenceStore();
    }
	
	private void initializeDefaults() {
	}

	private void initializeValues() {
    }

	protected void performDefaults() {
        super.performDefaults();
        initializeDefaults();
    }

	public boolean performOk() {
        storeValues();
        JBPMEclipsePlugin.getDefault().savePluginPreferences();
        return true;
    }
	
	private void storeValues() {
    }

	public void init(IWorkbench workbench) {
		// do nothing
	}
}
