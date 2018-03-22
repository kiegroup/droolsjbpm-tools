package org.kie.eclipse.navigator.view.actions.dialogs;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.kie.eclipse.navigator.Activator;
import org.kie.eclipse.server.IKieOrganizationHandler;
import org.kie.eclipse.server.IKieRepositoryHandler;

public class CreateRepositoryRequestDialog extends AbstractKieRequestDialog {

    IKieOrganizationHandler organization;
    KieRequestDialogTextField name;
    KieRequestDialogTextField description;
    KieRequestDialogTextField version;
    KieRequestDialogTextField groupId;

    public CreateRepositoryRequestDialog(Shell shell, final IKieOrganizationHandler organization) {
        super(shell, "Repository", new IKieRequestValidator() {
            @Override
            public String isValid(JsonObject object) {
                JsonValue jv;
                jv = object.get("name");
                String name = jv == null ? null : jv.asString().trim();
                if (name != null && !name.isEmpty()) {
                    try {
                        for (IKieRepositoryHandler rep : organization.getRepositories()) {
                            if (rep.getName().equals(name)) {
                                return "Repository '" + name + "' already exists in this Organizational Unit";
                            }
                        }
                    } catch (Exception e) {
                    }
                } else {
                    return "Name is required";
                }
                return null;
            }
        });
        setTitleImage(Activator.getImage("icons/wizban/repository.png"));
        this.organization = organization;
    }

    @Override
    protected void createFields(Composite composite) {
        setMessage("Enter the Repository details");

        name = new KieRequestDialogTextField(composite, "Name:", "", properties, "name");
        name.setChangeListener(new IKieRequestChangeListener() {
            @Override
            public void objectChanged(JsonObject object) {
                validate();
            }
        });

        description = new KieRequestDialogTextField(composite, "Description:", "", properties, "description");

        version = new KieRequestDialogTextField(composite, "Version:", "", properties, "version");
        version.setChangeListener(new IKieRequestChangeListener() {
            @Override
            public void objectChanged(JsonObject object) {
                validate();
            }
        });

        groupId = new KieRequestDialogTextField(composite, "Group ID:", "", properties, "groupId");
        groupId.setChangeListener(new IKieRequestChangeListener() {
            @Override
            public void objectChanged(JsonObject object) {
                validate();
            }
        });
    }
}
