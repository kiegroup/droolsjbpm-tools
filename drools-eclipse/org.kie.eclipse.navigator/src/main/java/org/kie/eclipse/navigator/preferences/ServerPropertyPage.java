package org.kie.eclipse.navigator.preferences;

import java.io.File;

import org.eclipse.egit.ui.UIUtils;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.server.core.IServer;
import org.kie.eclipse.navigator.IKieNavigatorConstants;

public class ServerPropertyPage extends FieldEditorPropertyPage implements IKieNavigatorConstants {

	private MyDirectoryFieldEditor gitPathEditor;
	private BooleanFieldEditor useDefaultGitPathEditor;
	
	public ServerPropertyPage() {
		super(GRID);
	}

	@Override
	protected void createFieldEditors() {
		IServer server = getResourceHandler().getServer();
		addField(new ReadonlyStringFieldEditor("Server Name:", server.getName(), getFieldEditorParent()));
		addField(new ReadonlyStringFieldEditor("Host Name:", server.getHost(), getFieldEditorParent()));
		
		StringFieldEditor stringEditor;
		PasswordFieldEditor passwordEditor;
		IntegerFieldEditor intEditor;
		BooleanFieldEditor boolEditor;
		
		
		stringEditor = new StringFieldEditor(PREF_SERVER_USERNAME, "Username:", getFieldEditorParent());
		addField(stringEditor);
		
		passwordEditor = new PasswordFieldEditor(PREF_SERVER_PASSWORD, "Password:", getFieldEditorParent());
		addField(passwordEditor);
		
		boolEditor = new MessageDialogToggleFieldEditor(PREF_SERVER_TRUSTED_CONNECTION, "Trust connections to this Server", getFieldEditorParent());
		addField(boolEditor);
		
		stringEditor = new StringFieldEditor(PREF_SERVER_KIE_APPLICATION_NAME, "KIE Application Name:", getFieldEditorParent());
		addField(stringEditor);

		intEditor = new IntegerFieldEditor(PREF_SERVER_HTTP_PORT, "HTTP Port:", getFieldEditorParent());
		addField(intEditor);
		
		intEditor = new IntegerFieldEditor(PREF_SERVER_GIT_PORT, "Git Port:", getFieldEditorParent());
		addField(intEditor);

		useDefaultGitPathEditor = new BooleanFieldEditor(PREF_USE_DEFAULT_GIT_PATH, "Use default Git Repository Path", getFieldEditorParent());
		addField(useDefaultGitPathEditor);
		
		gitPathEditor = new MyDirectoryFieldEditor(PREF_GIT_REPO_PATH, "Git Repository Path", getFieldEditorParent());
		addField(gitPathEditor);
	}
	
	private void updateControls() {
		Button checkBox = (Button) useDefaultGitPathEditor.getDescriptionControl(getFieldEditorParent());
		boolean checked = checkBox.getSelection();
		gitPathEditor.setEnabled(!checked, getFieldEditorParent());
		gitPathEditor.setEmptyStringAllowed(checked);
		
		if (!checked && gitPathEditor.getStringValue().isEmpty()) {
			String defaultRepoPath = UIUtils.getDefaultRepositoryDir();
			String repoPath = getResourceHandler().getPreferenceName(null).replace(IKieNavigatorConstants.PREF_PATH_SEPARATOR.charAt(0), File.separator.charAt(0));
			if (defaultRepoPath!=null)
				defaultRepoPath += File.separator + repoPath;
			else
				defaultRepoPath = File.separator + repoPath;
			
			gitPathEditor.setStringValue(defaultRepoPath);
		}
		
		gitPathEditor.refreshValidState();
		checkState();
	}
	
	@Override
	protected void initialize() {
		super.initialize();

		updateControls();
		
		final Button checkBox = (Button) useDefaultGitPathEditor.getDescriptionControl(getFieldEditorParent());
		checkBox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateControls();
			}
		});
	}
	
	private static class MyDirectoryFieldEditor extends DirectoryFieldEditor {

		public MyDirectoryFieldEditor(String prefGitRepoPath, String string, Composite fieldEditorParent) {
			super(prefGitRepoPath, string, fieldEditorParent);
		}
	
		public void refreshValidState() {
			super.refreshValidState();
		}
	}
	
	private static class MessageDialogToggleFieldEditor extends BooleanFieldEditor {

		private Composite parent;
		
		public MessageDialogToggleFieldEditor(String name, String label, Composite parent) {
			super(name, label, parent);
			this.parent = parent;
		}
		
		@Override
		protected void doLoad() {
			Button checkBox = getChangeControl(parent);
			if (checkBox != null) {
				String value = getPreferenceStore().getString(getPreferenceName());
				checkBox.setSelection(MessageDialogWithToggle.ALWAYS.equals(value));
			}
		}

		/*
		 * (non-Javadoc) Method declared on FieldEditor. Loads the default value
		 * from the preference store and sets it to the check box.
		 */
		@Override
		protected void doLoadDefault() {
			Button checkBox = getChangeControl(parent);
			if (checkBox != null) {
				String value = getPreferenceStore().getDefaultString(getPreferenceName());
				checkBox.setSelection(MessageDialogWithToggle.ALWAYS.equals(value));
			}
		}

		/*
		 * (non-Javadoc) Method declared on FieldEditor.
		 */
		@Override
		protected void doStore() {
			Button checkBox = getChangeControl(parent);
			getPreferenceStore().setValue(
					getPreferenceName(),
					checkBox.getSelection() ? MessageDialogWithToggle.ALWAYS : MessageDialogWithToggle.NEVER);
		}

	}
}
