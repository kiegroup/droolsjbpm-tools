package org.drools.eclipse.task.preferences;

import org.drools.eclipse.task.Activator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class DroolsTaskPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	private Text ipAddressText;
	private Text portText;
	private Text languageText;
	
	protected Control createContents(Composite parent) {
		ipAddressText = createText(parent, "IP address");
		portText = createText(parent, "Port");
		languageText = createText(parent, "Language");
		initializeValues();
		return new Composite(parent, SWT.NULL);
	}

	private Text createText(Composite group, String labelText) {
		Label label = new Label(group, SWT.NONE);
		label.setText(labelText);
        Text text = new Text(group, SWT.NONE);
        return text;
    }
	
	protected IPreferenceStore doGetPreferenceStore() {
        return Activator.getDefault().getPreferenceStore();
    }
	
	private void initializeDefaults() {
        IPreferenceStore store = getPreferenceStore();
        ipAddressText.setText(store.getDefaultString(DroolsTaskConstants.SERVER_IP_ADDRESS));
        portText.setText(store.getDefaultInt(DroolsTaskConstants.SERVER_PORT) + "");
        languageText.setText(store.getDefaultString(DroolsTaskConstants.LANGUAGE));
    }

	private void initializeValues() {
        IPreferenceStore store = getPreferenceStore();
        ipAddressText.setText(store.getString(DroolsTaskConstants.SERVER_IP_ADDRESS));
        portText.setText(store.getInt(DroolsTaskConstants.SERVER_PORT) + "");
        languageText.setText(store.getString(DroolsTaskConstants.LANGUAGE));
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
	        store.setValue(DroolsTaskConstants.SERVER_IP_ADDRESS, ipAddressText.getText());
	        store.setValue(DroolsTaskConstants.SERVER_PORT, port);
	        store.setValue(DroolsTaskConstants.LANGUAGE, languageText.getText());
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
