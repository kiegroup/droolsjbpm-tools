package org.kie.eclipse.navigator.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.kie.eclipse.server.IKieResourceHandler;
import org.kie.eclipse.server.KieSpaceHandler;

public class SpacePropertyPage extends AbstractKieJsonPropertyPage {

    @Override
    protected void createFieldEditors() {
        if (getResourceHandler() == null) {
            // special case: If a Repository is the current selection, it does not have to be contained in an Space
            // so we would not have any Space info to display in this property page
            addField(new LabelFieldEditor("The selected Repository is not contained in any Space", getFieldEditorParent()));
        } else {
            addField(new ReadonlyStringFieldEditor("Space Name", getResourceHandler().getName(), getFieldEditorParent()));
            addField(new ReadonlyStringFieldEditor("owner", "Owner", getFieldEditorParent()));
            addField(new ReadonlyStringFieldEditor("defaultGroupId", "Default Group ID", getFieldEditorParent()));
        }
    }

    @Override
    public IPreferenceStore getPreferenceStore() {
        if (getResourceHandler() == null) {
            return null;
        }
        return super.getPreferenceStore();
    }

    @Override
    protected Class<? extends IKieResourceHandler> getResourceHandlerType() {
        return KieSpaceHandler.class;
    }
}
