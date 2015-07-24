package org.kie.eclipse.navigator.view.actions.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.kie.eclipse.navigator.view.server.IKieProjectHandler;
import org.kie.eclipse.navigator.view.server.IKieRepositoryHandler;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class CreateProjectRequestDialog extends AbstractKieRequestDialog {

	IKieRepositoryHandler repository;
	KieRequestDialogTextField name;
	KieRequestDialogTextField description;
	KieRequestDialogTextField groupId;
	KieRequestDialogTextField version;
	
	boolean startProjectWizard = false;
	
	public CreateProjectRequestDialog(Shell shell, final IKieRepositoryHandler repository) {
		super(shell, "Project", new IKieRequestValidator() {
			@Override
			public String isValid(JsonObject object) {
				JsonValue jv;
				jv = object.get("name");
				String name = jv==null ? null : jv.asString();
				if (name!=null && !name.isEmpty()) {
					try {
						for (IKieProjectHandler p : repository.getProjects()) {
							if (p.getName().equals(name))
								return "Project '"+name+"' already exists in this Repository";
						}
					}
					catch (Exception e) {
					}
				}
				else {
					return "Name is required";
				}
				return null;
			}
        });
		this.repository = repository;
	}
    
	@Override
	protected void createFields(Composite composite) {
        setMessage("Enter the Project details");

		name = new KieRequestDialogTextField(composite, "Name:", "", properties, "name");
		name.setChangeListener(new IKieRequestChangeListener() {
			@Override
			public void objectChanged(JsonObject object) {
				validate();
			}
		});
		description = new KieRequestDialogTextField(composite, "Description:", "", properties, "description");
		groupId = new KieRequestDialogTextField(composite, "Group ID:", "", properties, "groupId");
		version = new KieRequestDialogTextField(composite, "Version:", "", properties, "version");
		
		new Label(composite, SWT.NONE);
		final Button button = new Button(composite, SWT.CHECK);
		button.setText("Start the New Project Wizard when done");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				startProjectWizard = button.getSelection();
			}
		});
	}

	@Override
	public JsonObject getResult() {
		return super.getResult();
	}
	
	public boolean shouldStartProjectWizard() {
		return startProjectWizard;
	}
}
