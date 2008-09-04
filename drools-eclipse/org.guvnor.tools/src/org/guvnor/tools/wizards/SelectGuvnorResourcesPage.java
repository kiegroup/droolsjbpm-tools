package org.guvnor.tools.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.guvnor.tools.Activator;
import org.guvnor.tools.GuvnorRepository;
import org.guvnor.tools.Messages;
import org.guvnor.tools.utils.PlatformUtils;
import org.guvnor.tools.views.RepositoryContentProvider;
import org.guvnor.tools.views.RepositoryLabelProvider;
import org.guvnor.tools.views.model.TreeObject;

/**
 * Wizard page for selecting resources in Guvnor.
 * @author jgraham
 */
public class SelectGuvnorResourcesPage extends WizardPage {
	
	private TreeViewer viewer;
	private String previousSelection;
	
	public SelectGuvnorResourcesPage(String pageName) {
		super(pageName);
	}

	public SelectGuvnorResourcesPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}
	
	public void createControl(Composite parent) {
		Composite composite = PlatformUtils.createComposite(parent, 1);
		new Label(composite, SWT.NONE).setText(Messages.getString("select.resources")); //$NON-NLS-1$
		
		viewer = new TreeViewer(composite, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		viewer.setLabelProvider(new RepositoryLabelProvider());
		viewer.addPostSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateModel();	
			}
		});
		super.setControl(composite);
	}
	
	private void handleRepositoryCreation() {
		// First we'll see if the repository already exists
		GuvWizardModel model = ((IGuvnorWizard)super.getWizard()).getModel();
		if (model.shouldCreateNewRep()
		   && model.getRepLocation() != null) {
			GuvnorRepository theRep = Activator.getLocationManager().
										findRepository(model.getRepLocation());
			if (theRep != null) {
				// The repository already exists, nothing to do
				return;
			}
			try {
				WizardUtils.createGuvnorRepository(model);
			} catch (Exception e) {
				super.setErrorMessage(e.getMessage());
				Activator.getDefault().displayError(IStatus.ERROR, e.getMessage(), e);
			}
		}
	}
	
	@Override
	public void setVisible(boolean visible) {
		if (visible) {
			// Need to filter repository list based on currently selected repository
			// Will also keep track of which filter is applied, so we don't create
			// additional content providers unnecessarily.
			GuvWizardModel model = ((IGuvnorWizard)super.getWizard()).getModel();
			String currentSelection = model.getRepLocation();
			// Not supposed to use this page without setting the target repository in the model
			assert(currentSelection != null);
			// If we had a repository selection before that is different from the current selection
			if (previousSelection != null
			   && !currentSelection.equals(previousSelection)) {
				handleRepositoryCreation();
				RepositoryContentProvider cp = new RepositoryContentProvider();
				cp.setRepositorySelection(currentSelection);
				viewer.setContentProvider(cp);
				viewer.setInput(viewer);
				previousSelection = currentSelection;
			} else {
				// If we didn't have a repository selection before (first time this page is loaded)
				if (previousSelection == null) {
					handleRepositoryCreation();
					RepositoryContentProvider cp = new RepositoryContentProvider();
					cp.setRepositorySelection(currentSelection);
					viewer.setContentProvider(cp);
					viewer.setInput(viewer);
					previousSelection = currentSelection;
				}
			}
		}
		super.setVisible(visible);
	}
	
	@SuppressWarnings("unchecked")
	private void updateModel() {
		IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
		if (!selection.isEmpty()) {
			List<String> resources = new ArrayList<String>();
			List<TreeObject> nodes = selection.toList();
			for (TreeObject o:nodes) {
				resources.add(o.getFullPath());
			}
			GuvWizardModel model = ((IGuvnorWizard)super.getWizard()).getModel();
			model.setResources(resources);
		}
	}
}
