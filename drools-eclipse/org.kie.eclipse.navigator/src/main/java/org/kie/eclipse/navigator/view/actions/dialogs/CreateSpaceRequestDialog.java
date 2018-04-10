package org.kie.eclipse.navigator.view.actions.dialogs;

import java.io.IOException;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.kie.eclipse.navigator.Activator;
import org.kie.eclipse.server.IKieServerHandler;
import org.kie.eclipse.server.IKieSpaceHandler;

public class CreateSpaceRequestDialog extends AbstractKieRequestDialog {

	IKieServerHandler server;
	KieRequestDialogTextField name;
	KieRequestDialogTextField description;
	KieRequestDialogTextField owner;
	KieRequestDialogTextField defaultGroupId;
	
	public CreateSpaceRequestDialog(Shell shell, final IKieServerHandler server) {
		super(shell, "Space", new IKieRequestValidator() {
			@Override
			public String isValid(JsonObject object) {
				JsonValue jv;
				jv = object.get("name");
				String name = jv==null ? null : jv.asString().trim();
				jv = object.get("owner");
				String owner = jv==null ? null : jv.asString().trim();
				if (name!=null && !name.isEmpty()) {
					try {
						for (IKieSpaceHandler space : server.getSpaces()) {
							if (space.getName().equals(name))
								return "Space '"+name+"' already exists";
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
		setTitleImage(Activator.getImage("icons/wizban/space.png"));
		this.server = server;
	}
    
	@Override
	protected void createFields(Composite composite) {
        setMessage("Enter the Space details");

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
