package org.guvnor.tools.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.guvnor.tools.Activator;
import org.guvnor.tools.GuvnorRepository;

public class SelectGuvnorRepPage extends WizardPage {
	
	private Button createRep;
	private Button existingRep;
	private List   repLocations;
	
	public SelectGuvnorRepPage(String pageName) {
		super(pageName);
	}

	public SelectGuvnorRepPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}
	
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1);
		new Label(composite, SWT.NONE).setText("This wizard allow you to check out resource from a Guvnor repository");
		
		createRep = new Button(composite, SWT.RADIO);
		createRep.setText("Create a new Guvnor repository location");
		createRep.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createRep.setSelection(false);
		createRep.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				repLocations.setEnabled(existingRep.getSelection());
			}
		});
		
		existingRep = new Button(composite, SWT.RADIO);
		existingRep.setText("Use an existing Guvnor repository location");
		existingRep.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		existingRep.setSelection(true);
		existingRep.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				repLocations.setEnabled(existingRep.getSelection());
			}
		});
		
		repLocations = new List(composite, SWT.BORDER | SWT.MULTI);
		repLocations.setLayoutData(new GridData(GridData.FILL_BOTH));
		addRepositoryList();
		super.setControl(composite);
	}
	
	private void addRepositoryList() {
		java.util.List<GuvnorRepository> reps = Activator.getLocationManager().getRepositories();
		for (int i = 0; i < reps.size(); i++) {
			repLocations.add(reps.get(i).getLocation());
		}
		if (repLocations.getItemCount() > 0) {
			repLocations.setSelection(0);
		}
	}
	
	private Composite createComposite(Composite parent, int numColumns) {
		Composite composite = new Composite(parent, SWT.NULL);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return composite;
	}

	@Override
	public IWizardPage getNextPage() {
		if (createRep.getSelection()) {
			return getWizard().getPage("config_page");
		} else {
			return getWizard().getPage("select_res_page");
		}
	}
}
