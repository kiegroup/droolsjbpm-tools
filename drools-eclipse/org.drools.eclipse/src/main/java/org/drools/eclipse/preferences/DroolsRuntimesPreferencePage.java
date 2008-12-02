package org.drools.eclipse.preferences;

import org.drools.eclipse.preferences.DroolsRuntimesBlock.DroolsRuntime;
import org.drools.eclipse.util.DroolsRuntimeManager;
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

public class DroolsRuntimesPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private DroolsRuntimesBlock droolsRuntimesBlock;
	
	public DroolsRuntimesPreferencePage() {
		super("Installed Drools Runtimes");
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
			"Add, remove or edit Drools Runtime definitions. " +
			"By default, the checked Drools Runtime is added to the build " +
			"path of newly created Drools projects.");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.widthHint = 300;
		l.setLayoutData(gd);
		l = new Label(ancestor, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 1;
		l.setLayoutData(gd);
		droolsRuntimesBlock = new DroolsRuntimesBlock();
		droolsRuntimesBlock.createControl(ancestor);
		DroolsRuntime[] runtimes = DroolsRuntimeManager.getDroolsRuntimes();
		droolsRuntimesBlock.setDroolsRuntimes(runtimes);
		for (DroolsRuntime runtime: runtimes) {
			if (runtime.isDefault()) {
				droolsRuntimesBlock.setDefaultDroolsRuntime(runtime);
				break;
			}
		}
		if (droolsRuntimesBlock.getDefaultDroolsRuntime() == null) {
			setValid(false);
			setErrorMessage("Select a default Drools Runtime");
		}
		Control control = droolsRuntimesBlock.getControl();
		GridData data = new GridData(GridData.FILL_BOTH);
		data.horizontalSpan = 1;
		data.widthHint = 450;
		control.setLayoutData(data);

		droolsRuntimesBlock.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				DroolsRuntime runtime = droolsRuntimesBlock.getDefaultDroolsRuntime();
				if (runtime == null) {
					setValid(false);
					setErrorMessage("Select a default Drools Runtime"); 
				} else {
					setValid(true);
					setErrorMessage(null);
				}
			}
		});
		applyDialogFont(ancestor);
		return ancestor;
	}
	
	public boolean performOk() {
		if (DroolsRuntimeManager.getDefaultDroolsRuntime() != null) {
			MessageDialog.openInformation(getShell(), "Warning",
			"You need to restart Eclipse to update the Drools Runtime of existing projects.");
		}
		DroolsRuntimeManager.setDroolsRuntimes(droolsRuntimesBlock.getDroolsRuntimes());
		return super.performOk();
	}
	
}
