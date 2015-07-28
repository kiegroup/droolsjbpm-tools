package org.kie.eclipse.navigator.view.actions.dialogs;

import java.io.IOException;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.kie.eclipse.navigator.Activator;
import org.kie.eclipse.server.IKieOrganizationHandler;
import org.kie.eclipse.server.IKieServerHandler;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class CreateOrganizationRequestDialog extends AbstractKieRequestDialog {

	IKieServerHandler server;
	KieRequestDialogTextField name;
	KieRequestDialogTextField description;
	KieRequestDialogTextField owner;
	KieRequestDialogTextField defaultGroupId;
	
	public CreateOrganizationRequestDialog(Shell shell, final IKieServerHandler server) {
		super(shell, "Organizational Unit", new IKieRequestValidator() {
			@Override
			public String isValid(JsonObject object) {
				JsonValue jv;
				jv = object.get("name");
				String name = jv==null ? null : jv.asString();
				jv = object.get("owner");
				String owner = jv==null ? null : jv.asString();
				if (name!=null && !name.isEmpty()) {
					try {
						for (IKieOrganizationHandler org : server.getOrganizations()) {
							if (org.getName().equals(name))
								return "Organizational Unit '"+name+"' already exists";
						}
					}
					catch (IOException e) {
					}
				}
				else {
					return "Name is required";
				}
				if (owner==null || owner.isEmpty())
					return "Owner is required";
				return null;
			}
        });
		setTitleImage(Activator.getImage("icons/wizban/organization.png"));
		this.server = server;
	}
    
	@Override
	protected void createFields(Composite composite) {
        setMessage("Enter the Organizational Unit details");

		name = new KieRequestDialogTextField(composite, "Name:", "", properties, "name");
		name.setChangeListener(new IKieRequestChangeListener() {
			@Override
			public void objectChanged(JsonObject object) {
				validate();
			}
		});
		description = new KieRequestDialogTextField(composite, "Description:", "", properties, "description");
		owner = new KieRequestDialogTextField(composite, "Owner:", "", properties, "owner");
		owner.setChangeListener(new IKieRequestChangeListener() {
			@Override
			public void objectChanged(JsonObject object) {
				validate();
			}
		});
		defaultGroupId = new KieRequestDialogTextField(composite, "Default Group ID:", "", properties, "defaultGroupId");
	}
}
