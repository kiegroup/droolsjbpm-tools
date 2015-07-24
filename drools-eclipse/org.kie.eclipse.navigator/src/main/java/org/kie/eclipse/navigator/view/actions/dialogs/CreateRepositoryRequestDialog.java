package org.kie.eclipse.navigator.view.actions.dialogs;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.kie.eclipse.navigator.view.server.IKieOrganizationHandler;
import org.kie.eclipse.navigator.view.server.IKieRepositoryHandler;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class CreateRepositoryRequestDialog extends AbstractKieRequestDialog {

	IKieOrganizationHandler organization;
	KieRequestDialogTextField name;
	KieRequestDialogTextField description;
	KieRequestDialogTextField userName;
	KieRequestDialogPasswordField password;
	KieRequestDialogSelectionField requestType;
	KieRequestDialogTextField gitURL;
	
	public CreateRepositoryRequestDialog(Shell shell, final IKieOrganizationHandler organization) {
		super(shell, "Repository", new IKieRequestValidator() {
			@Override
			public String isValid(JsonObject object) {
				JsonValue jv;
				jv = object.get("name");
				String name = jv==null ? null : jv.asString();
				jv = object.get("gitURL");
				String gitURL = jv==null ? null : jv.asString();
				if (name!=null && !name.isEmpty()) {
					try {
						for (IKieRepositoryHandler rep : organization.getRepositories()) {
							if (rep.getName().equals(name))
								return "Repository '"+name+"' already exists in this Organizational Unit";
						}
					}
					catch (Exception e) {
					}
				}
				else {
					return "Name is required";
				}
				if (gitURL==null || gitURL.isEmpty()) {
					jv = object.get("requestType");
					if (jv!=null && "clone".equals(jv.asString())) {
						return "Git URL of origin is required";
					}
				}
				else {
					try {
						new URI(gitURL);
					}
					catch (URISyntaxException e) {
						return "Git URL is invalid";
					}
				}
				return null;
			}
        });
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
		userName = new KieRequestDialogTextField(composite, "Username:", "", properties, "userName");
		password = new KieRequestDialogPasswordField(composite, "Password:", "", properties, "password");
		requestType = new KieRequestDialogSelectionField(composite, "Request Type:",
				new String[] {"Create New Repository", "Clone an existing Repository"},
				new String[] {"new", "clone"},
				SWT.RADIO, properties, "requestType"
		);
		// "requestType" is a required field, initialize to "new"
		properties.set("requestType", "new");
		
		gitURL = new KieRequestDialogTextField(composite, "URL of a Repository to clone:", "", properties, "gitURL");
		gitURL.getControl().setEnabled(false);
		gitURL.setChangeListener(new IKieRequestChangeListener() {
			@Override
			public void objectChanged(JsonObject object) {
				validate();
			}
		});
		requestType.setChangeListener(new IKieRequestChangeListener() {
			@Override
			public void objectChanged(JsonObject object) {
				JsonValue jv = object.get("requestType");
				if (jv!=null) {
					gitURL.getControl().setEnabled("clone".equals(jv.asString()));
				}
				validate();
			}
		});
	}

	@Override
	public JsonObject getResult() {
		// append the Organization Unit name field since this will not change
		properties.set("organizationalUnitName", organization.getName());
		return super.getResult();
	}
	
	
}
