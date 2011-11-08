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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.jbpm.eclipse.util.JBPMRuntime;
import org.jbpm.eclipse.util.JBPMRuntimeManager;

public class JBPMRuntimesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private JBPMRuntimesBlock jBPMRuntimesBlock;
	
	public JBPMRuntimesPreferencePage() {
		super("Installed jBPM Runtimes");
	}

	public void init(IWorkbench workbench) {
	}

	protected Control createContents(Composite ancestor) {
		initializeDialogUnits(ancestor);
		noDefaultAndApplyButton();
		GridLayout layout= new GridLayout();
		layout.numColumns= 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		ancestor.setLayout(layout);
		Label l = new Label(ancestor, SWT.WRAP);
		l.setFont(ancestor.getFont());
		l.setText(
			"Add, remove or edit jBPM Runtime definitions. " +
			"By default, the checked jBPM Runtime is added to the build " +
			"path of newly created jBPM projects.");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.widthHint = 300;
		l.setLayoutData(gd);
		l = new Label(ancestor, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 1;
		l.setLayoutData(gd);
		jBPMRuntimesBlock = new JBPMRuntimesBlock();
		jBPMRuntimesBlock.createControl(ancestor);
		JBPMRuntime[] runtimes = JBPMRuntimeManager.getJBPMRuntimes();
		jBPMRuntimesBlock.setJBPMRuntimes(runtimes);
		for (JBPMRuntime runtime: runtimes) {
			if (runtime.isDefault()) {
				jBPMRuntimesBlock.setDefaultJBPMRuntime(runtime);
				break;
			}
		}
		if (jBPMRuntimesBlock.getDefaultJBPMRuntime() == null) {
			setErrorMessage("Select a default jBPM Runtime");
		}
		Control control = jBPMRuntimesBlock.getControl();
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 1;
		data.widthHint = 450;
		control.setLayoutData(data);

		jBPMRuntimesBlock.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				JBPMRuntime runtime = jBPMRuntimesBlock.getDefaultJBPMRuntime();
				if (runtime == null) {
					setErrorMessage("Select a default jBPM Runtime"); 
				} else {
					setErrorMessage(null);
				}
			}
		});
		applyDialogFont(ancestor);
		return ancestor;
	}
	
	public boolean performOk() {
		if (JBPMRuntimeManager.getDefaultJBPMRuntime() != null) {
			MessageDialog.openInformation(getShell(), "Warning",
			"You need to restart Eclipse to update the jBPM Runtime of existing projects.");
		}
		JBPMRuntimeManager.setJBPMRuntimes(jBPMRuntimesBlock.getJBPMRuntimes());
		return super.performOk();
	}
	
}
