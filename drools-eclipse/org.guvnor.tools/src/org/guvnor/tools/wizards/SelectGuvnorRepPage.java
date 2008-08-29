package org.guvnor.tools.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;
import org.guvnor.tools.Activator;
import org.guvnor.tools.GuvnorRepository;
import org.guvnor.tools.Messages;
import org.guvnor.tools.utils.PlatformUtils;

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
		Composite composite = PlatformUtils.createComposite(parent, 1);		
		createRep = new Button(composite, SWT.RADIO);
		createRep.setText(Messages.getString("select.rep.guvnor.loc")); //$NON-NLS-1$
		createRep.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createRep.setSelection(false);
		createRep.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				repLocations.setEnabled(existingRep.getSelection());
				updateModel();
			}
		});
		
		existingRep = new Button(composite, SWT.RADIO);
		existingRep.setText(Messages.getString("select.rep.guvnor.loc.desc")); //$NON-NLS-1$
		existingRep.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		existingRep.setSelection(true);
		existingRep.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				repLocations.setEnabled(existingRep.getSelection());
				updateModel();
			}
		});
		
		repLocations = new List(composite, SWT.BORDER | SWT.MULTI);
		repLocations.setLayoutData(new GridData(GridData.FILL_BOTH));
		repLocations.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				updateModel();
			}
		});
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
			updateModel();
		}
	}
	
	private void updateModel() {
		GuvWizardModel model = ((IGuvnorWizard)super.getWizard()).getModel();
		if (createRep.getSelection()) {
			model.setCreateNewRep(true);
			model.setRepLocation(null);
		} else {
			model.setCreateNewRep(false);
			String selected = null;
			if (repLocations.getSelection().length > 0) {
				selected = repLocations.getSelection()[0];
			}
			if (selected != null) {
				model.setRepLocation(repLocations.getSelection()[0]);
			}
		}
	}
}
