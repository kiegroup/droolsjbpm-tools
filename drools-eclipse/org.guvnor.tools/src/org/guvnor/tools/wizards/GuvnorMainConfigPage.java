package org.guvnor.tools.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class GuvnorMainConfigPage extends WizardPage {
	
	private Text locationField;
	private Text unField;
	private Text pwField;
	
	public GuvnorMainConfigPage(String pageName) {
		super(pageName);
	}

	public GuvnorMainConfigPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	public void createControl(Composite parent) {
		
		Composite composite = createComposite(parent, 2);
		new Label(composite, SWT.NONE).setText("Location: ");
		locationField = new Text(composite, SWT.SINGLE | SWT.BORDER);
		locationField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		locationField.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				if (locationField.getText().trim().length() > 0) {
					GuvnorMainConfigPage.super.setPageComplete(true);
				} else {
					GuvnorMainConfigPage.super.setPageComplete(false);
				}
				updateModel();
			}
			
		});
		
		new Label(composite, SWT.NONE).setText("User Name: ");
		unField = new Text(composite, SWT.SINGLE | SWT.BORDER);
		unField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		unField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateModel();
			}
		});
		
		new Label(composite, SWT.NONE).setText("Password: ");
		pwField = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
		pwField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		pwField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateModel();
			}
		});

		new Label(composite, SWT.NONE).setText("NOTE: ") ;
		new Label(composite, SWT.WRAP).setText("Saved passwords are stored on your computer in a file that is difficult, but not impossible, for an intruder to read.");
		
		super.setControl(composite);
	}

	private Composite createComposite(Composite parent, int numColumns) {
		Composite composite = new Composite(parent, SWT.NULL);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return composite;
	}
	
	private void updateModel() {
		GuvWizardModel model = ((IGuvnorWizard)super.getWizard()).getModel();
		model.setRepLocation(locationField.getText());
		model.setUsername(unField.getText());
		model.setPassword(pwField.getText());
		model.setCreateNewRep(true);
	}
}
