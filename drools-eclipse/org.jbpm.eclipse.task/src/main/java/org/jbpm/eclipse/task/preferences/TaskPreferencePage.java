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

package org.jbpm.eclipse.task.preferences;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.jbpm.eclipse.task.Activator;

public class TaskPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

    private Text ipAddressText;
    private Text portText;
    private Text languageText;
    private Combo transportType;

    protected Control createContents(Composite parent) {
        ipAddressText = createText(parent, "IP address");
        portText = createText(parent, "Port");
        languageText = createText(parent, "Language");
        transportType = createCombo(parent, "Transport");
        initializeValues();
        return new Composite(parent, SWT.NULL);
    }

    private Text createText(Composite group, String labelText) {
        Label label = new Label(group, SWT.NONE);
        label.setText(labelText);
        Text text = new Text(group, SWT.NONE);
        return text;
    }
    
    private Combo createCombo(Composite group, String labelText) {
        Label label = new Label(group, SWT.NONE);
        label.setText(labelText);
        Combo combo = new Combo(group, SWT.NONE);
        combo.add("mina", 0);
        combo.add("hornetq", 1);
        return combo;
    }

    protected IPreferenceStore doGetPreferenceStore() {
        return Activator.getDefault().getPreferenceStore();
    }

    private void initializeDefaults() {
        IPreferenceStore store = getPreferenceStore();
        ipAddressText.setText(store.getDefaultString(TaskConstants.SERVER_IP_ADDRESS));
        portText.setText(store.getDefaultInt(TaskConstants.SERVER_PORT) + "");
        languageText.setText(store.getDefaultString(TaskConstants.LANGUAGE));
        transportType.select(store.getInt(TaskConstants.TRANSPORT));
    }

    private void initializeValues() {
        IPreferenceStore store = getPreferenceStore();
        ipAddressText.setText(store.getString(TaskConstants.SERVER_IP_ADDRESS));
        portText.setText(store.getInt(TaskConstants.SERVER_PORT) + "");
        languageText.setText(store.getString(TaskConstants.LANGUAGE));
        transportType.select(store.getInt(TaskConstants.TRANSPORT));
    }

    protected void performDefaults() {
        super.performDefaults();
        initializeDefaults();
    }

    public boolean performOk() {
        storeValues();
        Activator.getDefault().savePluginPreferences();
        return true;
    }

    private void storeValues() {
        try {
            Integer port = new Integer(portText.getText());
            IPreferenceStore store = getPreferenceStore();
            store.setValue(TaskConstants.SERVER_IP_ADDRESS, ipAddressText.getText());
            store.setValue(TaskConstants.SERVER_PORT, port);
            store.setValue(TaskConstants.LANGUAGE, languageText.getText());
            store.setValue(TaskConstants.TRANSPORT, transportType.getSelectionIndex());
        } catch (NumberFormatException e) {
            showMessage("Could not convert port, should be an integer value.");
        }
    }

    public void init(IWorkbench workbench) {
        // do nothing
    }

    private void showMessage(String message) {
        MessageDialog.openInformation(
            getControl().getShell(), "Task View", message);
    }

}
