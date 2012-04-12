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

package org.drools.eclipse.preferences;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.preferences.IDroolsConstants.InternalApiChoice;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class DroolsPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private Button buildAllCheckBox;
    private Button crossBuildCheckBox;
    private Button collapseEditorCheckBox;
    private Button cacheParsedRulesCheckBox;
    private Combo processSkinCombo;
    private Button allowNodeCustomizationCheckBox;
    private Combo internalAPICombo;

    protected Control createContents(Composite parent) {
        final Composite composite = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        composite.setLayout(gridLayout);
        
        buildAllCheckBox = createCheckBox(composite,
            "Automatically reparse all rules if a Java resource is changed.");
        crossBuildCheckBox = createCheckBox(composite,
            "Allow cross references in DRL files.");
        collapseEditorCheckBox = createCheckBox(composite,
            "Use code folding in DRL editor.");
        cacheParsedRulesCheckBox = createCheckBox(composite,
            "When parsing rules, always cache the result for future use. Warning: when disabled, debugging of rules will not work.");
        Label label = new Label(composite, SWT.NONE);
        label.setText("Preferred process skin: ");
        processSkinCombo = new Combo(composite, SWT.LEFT | SWT.READ_ONLY );
        processSkinCombo.add("default");
        processSkinCombo.add("BPMN");
        processSkinCombo.add("BPMN2");
        allowNodeCustomizationCheckBox = createCheckBox(composite,
            "Allow the customization of process nodes.");
        label = new Label(composite, SWT.NONE);
        label.setText("Internal Drools classes are: ");

        internalAPICombo = new Combo(composite, SWT.LEFT);
        for (InternalApiChoice choice : InternalApiChoice.values()) {
            internalAPICombo.add(choice.toString());
        }
        initializeValues();

        return composite;
    }

    private Button createCheckBox(Composite group, String label) {
        Button button = new Button(group, SWT.CHECK | SWT.LEFT);
        button.setText(label);
        GridData data = new GridData();
        data.horizontalSpan = 2;
        button.setLayoutData(data);
        return button;
    }

    protected IPreferenceStore doGetPreferenceStore() {
        return DroolsEclipsePlugin.getDefault().getPreferenceStore();
    }

    private void initializeDefaults() {
        IPreferenceStore store = getPreferenceStore();
        buildAllCheckBox.setSelection(store.getDefaultBoolean(IDroolsConstants.BUILD_ALL));
        crossBuildCheckBox.setSelection(store.getDefaultBoolean(IDroolsConstants.CROSS_BUILD));
        collapseEditorCheckBox.setSelection(store.getDefaultBoolean(IDroolsConstants.EDITOR_FOLDING));
        cacheParsedRulesCheckBox.setSelection(store.getDefaultBoolean(IDroolsConstants.CACHE_PARSED_RULES));
        String skin = store.getDefaultString(IDroolsConstants.SKIN);
        int index = 0;
        if ("BPMN".equals(skin)) {
            index = 1;
        }
        processSkinCombo.select(index);
        allowNodeCustomizationCheckBox.setSelection(store.getDefaultBoolean(IDroolsConstants.ALLOW_NODE_CUSTOMIZATION));
        internalAPICombo.select(store.getDefaultInt(IDroolsConstants.ALLOW_NODE_CUSTOMIZATION));
    }

    private void initializeValues() {
        IPreferenceStore store = getPreferenceStore();
        buildAllCheckBox.setSelection(store.getBoolean(IDroolsConstants.BUILD_ALL));
        crossBuildCheckBox.setSelection(store.getBoolean(IDroolsConstants.CROSS_BUILD));
        collapseEditorCheckBox.setSelection(store.getBoolean(IDroolsConstants.EDITOR_FOLDING));
        cacheParsedRulesCheckBox.setSelection(store.getBoolean(IDroolsConstants.CACHE_PARSED_RULES));
        String skin = store.getString(IDroolsConstants.SKIN);
        int index = 0;
        if ("BPMN".equals(skin)) {
            index = 1;
        }
        if ("BPMN2".equals(skin)) {
            index = 2;
        }
        processSkinCombo.select(index);
        allowNodeCustomizationCheckBox.setSelection(store.getBoolean(IDroolsConstants.ALLOW_NODE_CUSTOMIZATION));
        internalAPICombo.select(store.getInt(IDroolsConstants.INTERNAL_API));
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
        store.setValue(IDroolsConstants.CROSS_BUILD, crossBuildCheckBox.getSelection());
        store.setValue(IDroolsConstants.EDITOR_FOLDING, collapseEditorCheckBox.getSelection());
        store.setValue(IDroolsConstants.CACHE_PARSED_RULES, cacheParsedRulesCheckBox.getSelection());
        store.setValue(IDroolsConstants.SKIN,
            (String) processSkinCombo.getItem(processSkinCombo.getSelectionIndex()));
        store.setValue(IDroolsConstants.ALLOW_NODE_CUSTOMIZATION, allowNodeCustomizationCheckBox.getSelection());
        store.setValue(IDroolsConstants.INTERNAL_API, internalAPICombo.getSelectionIndex());
    }

    public void init(IWorkbench workbench) {
        // do nothing
    }
}
