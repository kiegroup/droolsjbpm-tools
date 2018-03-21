package org.kie.eclipse.navigator.preferences;

import org.eclipse.jface.preference.StringFieldEditor;
import org.kie.eclipse.server.IKieRepositoryHandler;
import org.kie.eclipse.server.IKieResourceHandler;
import org.kie.eclipse.server.KieRepositoryHandler;
import org.kie.eclipse.utils.PreferencesUtils;

public class RepositoryPropertyPage extends AbstractKieJsonPropertyPage {
	
	@Override
	protected void createFieldEditors() {
		IKieRepositoryHandler repositoryHandler = (IKieRepositoryHandler) getResourceHandler();
		addField(new ReadonlyStringFieldEditor("Repository Name", repositoryHandler.getName(), getFieldEditorParent()));
		addField(new ReadonlyStringFieldEditor("Organizational Unit", repositoryHandler.getParent().getName(), getFieldEditorParent()));
		addField(new ReadonlyStringFieldEditor("Remote Git URL", PreferencesUtils.getRepoURI(repositoryHandler).toString(), getFieldEditorParent()));
		addField(new ReadonlyStringFieldEditor("Local Git Directory", PreferencesUtils.getRepoPath(repositoryHandler), getFieldEditorParent()));

		addField(new StringFieldEditor("description", "Description", getFieldEditorParent()));
		addField(new StringFieldEditor("userName", "User Name", getFieldEditorParent()));
		addField(new PasswordFieldEditor("password", "Password", getFieldEditorParent()));
	}

	@Override
	protected Class<? extends IKieResourceHandler> getResourceHandlerType() {
		return KieRepositoryHandler.class;
	}
}
