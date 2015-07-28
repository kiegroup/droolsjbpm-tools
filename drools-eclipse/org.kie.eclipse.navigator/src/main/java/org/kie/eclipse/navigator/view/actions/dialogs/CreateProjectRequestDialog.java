package org.kie.eclipse.navigator.view.actions.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.kie.eclipse.navigator.Activator;
import org.kie.eclipse.server.IKieProjectHandler;
import org.kie.eclipse.server.IKieRepositoryHandler;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public class CreateProjectRequestDialog extends AbstractKieRequestDialog {

	private final static String DESCRIPTION = "description";
	private final static String ARTIFACT_ID = "artifactId";
	private final static String GROUP_ID = "groupId";
	private final static String VERSION = "version";
	
	IKieRepositoryHandler repository;
	KieRequestDialogTextField name;
	KieRequestDialogTextField description;
	KieRequestDialogTextField groupId;
	KieRequestDialogTextField artifactId;
	KieRequestDialogTextField version;

	String artifactIdValue = null;
	boolean importProject = true;
	boolean createArtifacts = true;
	
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
		setTitleImage(Activator.getImage("icons/wizban/project.png"));
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
		description = new KieRequestDialogTextField(composite, "Description:", "", properties, DESCRIPTION);
		groupId = new KieRequestDialogTextField(composite, "Group ID:", "", properties, GROUP_ID);
		artifactId = new KieRequestDialogTextField(composite, "Artifact ID:", "", properties, ARTIFACT_ID);
		version = new KieRequestDialogTextField(composite, "Version:", "", properties, VERSION);
		
		new Label(composite, SWT.NONE);
		final Button importProjectButton = new Button(composite, SWT.CHECK);
		importProjectButton.setText("Import the Project into my Workspace when done");
		importProjectButton.setSelection(importProject);
		new Label(composite, SWT.NONE);
		
		new Label(composite, SWT.NONE);
		final Button createArtifactsButton = new Button(composite, SWT.CHECK);
		createArtifactsButton.setText("Create Maven and KJar artifacts");
		createArtifactsButton.setSelection(createArtifacts);
		new Label(composite, SWT.NONE);
		
		createArtifactsButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				createArtifacts = createArtifactsButton.getSelection();
			}
		});
		importProjectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				importProject = importProjectButton.getSelection();
				createArtifactsButton.setEnabled(importProject);
			}
		});
	}

	@Override
	public int open() {
		int rtn = super.open();
		JsonObject object = super.getResult();
		JsonValue jv = object.get(ARTIFACT_ID);
		if (jv!=null)
			artifactIdValue = jv.asString();
		object.remove(ARTIFACT_ID);
		return rtn;
	}
	
	public boolean shouldImportProject() {
		return importProject;
	}
	
	public boolean shouldCreateArtifacts() {
		return createArtifacts;
	}
	
	public String getArtifactId() {
		return artifactIdValue;
	}
}
