package org.drools.ide.preferences;

import org.drools.ide.DroolsIDEPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class DroolsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Button buildAllCheckBox;
	private Button collapseEditorCheckBox;
	
	protected Control createContents(Composite parent) {
		buildAllCheckBox = createCheckBox(parent,
			"Automatically reparse all rules if a Java resource is changed.");
		collapseEditorCheckBox = createCheckBox(parent,
			"Use code folding in DRL editor.");

		initializeValues();

		return new Composite(parent, SWT.NULL);
	}

	private Button createCheckBox(Composite group, String label) {
        Button button = new Button(group, SWT.CHECK | SWT.LEFT);
        button.setText(label);
        GridData data = new GridData();
        button.setLayoutData(data);
        return button;
    }
	
	protected IPreferenceStore doGetPreferenceStore() {
        return DroolsIDEPlugin.getDefault().getPreferenceStore();
    }
	
	private void initializeDefaults() {
        IPreferenceStore store = getPreferenceStore();
        buildAllCheckBox.setSelection(store.getDefaultBoolean(IDroolsConstants.BUILD_ALL));
        collapseEditorCheckBox.setSelection(store.getDefaultBoolean(IDroolsConstants.EDITOR_FOLDING));
    }

	private void initializeValues() {
        IPreferenceStore store = getPreferenceStore();
        buildAllCheckBox.setSelection(store.getBoolean(IDroolsConstants.BUILD_ALL));
        collapseEditorCheckBox.setSelection(store.getBoolean(IDroolsConstants.EDITOR_FOLDING));
    }

	protected void performDefaults() {
        super.performDefaults();
        initializeDefaults();
    }

	public boolean performOk() {
        storeValues();
        DroolsIDEPlugin.getDefault().savePluginPreferences();
        return true;
    }
	
	private void storeValues() {
        IPreferenceStore store = getPreferenceStore();
        store.setValue(IDroolsConstants.BUILD_ALL, buildAllCheckBox.getSelection());
        store.setValue(IDroolsConstants.EDITOR_FOLDING, collapseEditorCheckBox.getSelection());
    }

	public void init(IWorkbench workbench) {
		// do nothing
	}
}
