package org.guvnor.tools.wizards;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
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

public class SelectTargetLocationPage extends WizardPage {
	
	private Button createProj;
	private Button existingProj;
	private List   candidateProjs;
	
	public SelectTargetLocationPage(String pageName) {
		super(pageName);
	}

	public SelectTargetLocationPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}
	
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1);
		new Label(composite, SWT.NONE).setText("Check out files to");
		
		createProj = new Button(composite, SWT.RADIO);
		createProj.setText("Create a new project");
		createProj.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createProj.setSelection(false);
		createProj.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				candidateProjs.setEnabled(existingProj.getSelection());
			}
		});
		
		existingProj = new Button(composite, SWT.RADIO);
		existingProj.setText("Use an existing project");
		existingProj.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		existingProj.setSelection(true);
		existingProj.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				candidateProjs.setEnabled(existingProj.getSelection());
			}
		});
		
		candidateProjs = new List(composite, SWT.BORDER | SWT.MULTI);
		candidateProjs.setLayoutData(new GridData(GridData.FILL_BOTH));
		addCandidateProjList();
		super.setControl(composite);
	}
	
	private void addCandidateProjList() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IProject[] projects = workspace.getRoot().getProjects();
		for (int i = 0; i < projects.length; i++) {
//			if (RepositoryProvider.getProvider(
//							projects[i],"GuvnorRepProvider") != null) {
				candidateProjs.add(projects[i].getName());
//			}
		}
		if (candidateProjs.getItemCount() > 0) {
			candidateProjs.setSelection(0);
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
		if (createProj.getSelection()) {
			return getWizard().getPage("new_project_page");
		} else {
			return getWizard().getPage("select_version_page");
		}
	}
}
