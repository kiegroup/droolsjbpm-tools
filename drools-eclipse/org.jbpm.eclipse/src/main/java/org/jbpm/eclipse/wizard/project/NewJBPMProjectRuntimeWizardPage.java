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

import org.eclipse.ui.dialogs.PreferencesUtil;
import org.jbpm.eclipse.preferences.JBPMProjectPreferencePage;
import org.jbpm.eclipse.util.JBPMRuntime;
import org.jbpm.eclipse.util.JBPMRuntimeManager;
import org.kie.eclipse.runtime.IRuntime;
import org.kie.eclipse.runtime.IRuntimeManager;
import org.kie.eclipse.wizard.project.AbstractKieProjectRuntimeWizardPage;

public class NewJBPMProjectRuntimeWizardPage extends AbstractKieProjectRuntimeWizardPage {

    public static final String JBPM5 = "jBPM 5";
    public static final String JBPM6 = "jBPM 6";
	
	public NewJBPMProjectRuntimeWizardPage(String pageId) {
		super(pageId);
		setTitle("jBPM Runtime");
        setDescription("Select a jBPM Runtime");
	}
	
	@Override
	public IRuntimeManager getRuntimeManager() {
		return JBPMRuntimeManager.getDefault();
	}

	@Override
	protected IRuntime createRuntime() {
		return new JBPMRuntime();
	}

	@Override
	protected boolean isComplete() {
		return true;
	}

	@Override
	public int showRuntimePreferenceDialog() {
		return PreferencesUtil.createPreferenceDialogOn(getShell(),
				JBPMProjectPreferencePage.PREF_ID,
				new String[] { JBPMProjectPreferencePage.PROP_ID }, new HashMap()).open();
	}

}
