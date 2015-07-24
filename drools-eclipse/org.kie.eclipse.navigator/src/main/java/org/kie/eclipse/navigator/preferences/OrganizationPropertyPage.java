package org.kie.eclipse.navigator.preferences;

import org.eclipse.jface.preference.StringFieldEditor;

public class OrganizationPropertyPage extends AbstractKiePropertyPage {
	
	@Override
	protected void createFieldEditors() {
		addField(new ReadonlyStringFieldEditor("Organization Name", getResourceHandler().getName(), getFieldEditorParent()));
		addField(new StringFieldEditor("owner", "Owner", getFieldEditorParent()));
		addField(new StringFieldEditor("defaultGroupId", "Default Group ID", getFieldEditorParent()));
	}
	
}
