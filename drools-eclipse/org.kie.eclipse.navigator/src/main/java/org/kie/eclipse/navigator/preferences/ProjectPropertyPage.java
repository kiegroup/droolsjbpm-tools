package org.kie.eclipse.navigator.preferences;

import org.eclipse.jface.preference.StringFieldEditor;
import org.kie.eclipse.server.IKieRepositoryHandler;
import org.kie.eclipse.server.IKieResourceHandler;

public class ProjectPropertyPage extends AbstractKiePropertyPage {
	
	@Override
	protected void createFieldEditors() {
		addField(new ReadonlyStringFieldEditor("Project Name", getResourceHandler().getName(), getFieldEditorParent()));
		IKieResourceHandler container = getResourceHandler().getParent();
		if (container instanceof IKieRepositoryHandler) {
			addField(new ReadonlyStringFieldEditor("Repository", container.getName(), getFieldEditorParent()));
		}
		addField(new StringFieldEditor("description", "Description", getFieldEditorParent()));
		addField(new StringFieldEditor("groupId", "Group ID", getFieldEditorParent()));
		addField(new StringFieldEditor("version", "Version", getFieldEditorParent()));
	}
	
}
