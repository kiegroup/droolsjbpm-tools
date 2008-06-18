package org.guvnor.tools.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;

public class SelectResourceVersionPage extends WizardPage {
	
	private List versions;
	
	public SelectResourceVersionPage(String pageName) {
		super(pageName);
	}

	public SelectResourceVersionPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}
	
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1);
		new Label(composite, SWT.NONE).setText("Select version:");
		
		versions = new List(composite, SWT.BORDER | SWT.MULTI);
		versions.setLayoutData(new GridData(GridData.FILL_BOTH));
		populateVersions();
		
		super.setControl(composite);
	}
	
	private void populateVersions() {
		versions.add("v20080424");
		versions.add("v20080516");
		versions.add("v20080522");
		versions.add("v20080705");
		versions.setSelection(0);
	}
	
	private Composite createComposite(Composite parent, int numColumns) {
		Composite composite = new Composite(parent, SWT.NULL);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return composite;
	}
}
