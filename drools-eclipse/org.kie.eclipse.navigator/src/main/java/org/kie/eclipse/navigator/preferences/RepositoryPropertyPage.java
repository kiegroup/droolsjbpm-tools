package org.kie.eclipse.navigator.preferences;

import org.eclipse.jface.preference.StringFieldEditor;
import org.kie.eclipse.navigator.view.server.IKieRepositoryHandler;

public class RepositoryPropertyPage extends AbstractKiePropertyPage {
	
	@Override
	protected void createFieldEditors() {
		IKieRepositoryHandler repository = (IKieRepositoryHandler) getResourceHandler();
		addField(new ReadonlyStringFieldEditor("Repository Name", repository.getName(), getFieldEditorParent()));
		addField(new ReadonlyStringFieldEditor("Organizational Unit", repository.getParent().getName(), getFieldEditorParent()));
		addField(new ReadonlyStringFieldEditor("Remote Git URL", PreferencesUtils.getRepoURI(repository).toString(), getFieldEditorParent()));
		addField(new ReadonlyStringFieldEditor("Local Git Directory", PreferencesUtils.getRepoPath(repository), getFieldEditorParent()));

		addField(new StringFieldEditor("description", "Description", getFieldEditorParent()));
		addField(new StringFieldEditor("userName", "User Name", getFieldEditorParent()));
		addField(new PasswordFieldEditor("password", "Password", getFieldEditorParent()));
	}
	
}
