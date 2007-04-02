package org.drools.eclipse.preferences;

import org.drools.eclipse.DroolsEclipsePlugin;
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
	private Button cacheParsedRulesCheckBox;
	
	protected Control createContents(Composite parent) {
		buildAllCheckBox = createCheckBox(parent,
			"Automatically reparse all rules if a Java resource is changed.");
		collapseEditorCheckBox = createCheckBox(parent,
			"Use code folding in DRL editor.");
		cacheParsedRulesCheckBox = createCheckBox(parent,
			"When parsing rules, always cache the result for future use. Warning: when disabled, debugging of rules will not work.");

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
        return DroolsEclipsePlugin.getDefault().getPreferenceStore();
    }
	
	private void initializeDefaults() {
        IPreferenceStore store = getPreferenceStore();
        buildAllCheckBox.setSelection(store.getDefaultBoolean(IDroolsConstants.BUILD_ALL));
        collapseEditorCheckBox.setSelection(store.getDefaultBoolean(IDroolsConstants.EDITOR_FOLDING));
        cacheParsedRulesCheckBox.setSelection(store.getDefaultBoolean(IDroolsConstants.CACHE_PARSED_RULES));
    }

	private void initializeValues() {
        IPreferenceStore store = getPreferenceStore();
        buildAllCheckBox.setSelection(store.getBoolean(IDroolsConstants.BUILD_ALL));
        collapseEditorCheckBox.setSelection(store.getBoolean(IDroolsConstants.EDITOR_FOLDING));
        cacheParsedRulesCheckBox.setSelection(store.getBoolean(IDroolsConstants.CACHE_PARSED_RULES));
    }

	protected void performDefaults() {
        super.performDefaults();
        initializeDefaults();
    }

	public boolean performOk() {
        storeValues();
        DroolsEclipsePlugin.getDefault().savePluginPreferences();
        return true;
    }
	
	private void storeValues() {
        IPreferenceStore store = getPreferenceStore();
        store.setValue(IDroolsConstants.BUILD_ALL, buildAllCheckBox.getSelection());
        store.setValue(IDroolsConstants.EDITOR_FOLDING, collapseEditorCheckBox.getSelection());
        store.setValue(IDroolsConstants.CACHE_PARSED_RULES, cacheParsedRulesCheckBox.getSelection());
    }

	public void init(IWorkbench workbench) {
		// do nothing
	}
}
