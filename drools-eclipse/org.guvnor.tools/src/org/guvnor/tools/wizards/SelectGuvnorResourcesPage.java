package org.guvnor.tools.wizards;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.progress.DeferredTreeContentManager;
import org.guvnor.tools.views.RepositoryLabelProvider;
import org.guvnor.tools.views.model.TreeObject;
import org.guvnor.tools.views.model.TreeParent;

public class SelectGuvnorResourcesPage extends WizardPage {
	
	class ResourcesContentProvider implements IStructuredContentProvider, 
										   ITreeContentProvider {
		private TreeParent invisibleRoot;
		private DeferredTreeContentManager manager;
		
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
			if (resourceTree instanceof AbstractTreeViewer) {
			    manager = new DeferredTreeContentManager(this, (AbstractTreeViewer)resourceTree);
			  }
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			if (parent.equals(SelectGuvnorResourcesPage.this)) {
				if (invisibleRoot == null) initialize();
				return getChildren(invisibleRoot);
			}
			return getChildren(parent);
		}
		public Object getParent(Object child) {
			if (child instanceof TreeObject) {
				return ((TreeObject)child).getParent();
			}
			return null;
		}
		public Object [] getChildren(Object parent) {
			if (parent == invisibleRoot) {
				return ((TreeParent)invisibleRoot).getChildren();
			} else if (parent instanceof TreeParent) {
				return manager.getChildren(parent);
			}
			return new Object[0];
		}
		public boolean hasChildren(Object parent) {
			if (parent instanceof TreeParent) {
				return manager.mayHaveChildren(parent);
			}
			return false;
		}

		private void initialize() {
			invisibleRoot = new TreeParent("", TreeObject.Type.NONE);
			for (int i = 0; i < 4; i++) {
				TreeParent p = new TreeParent("Package" + String.valueOf(i + 1), 
											TreeObject.Type.PACKAGE);
				invisibleRoot.addChild(p);
			}
		}
	}
	
	private TreeViewer resourceTree;
	
	public SelectGuvnorResourcesPage(String pageName) {
		super(pageName);
	}

	public SelectGuvnorResourcesPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}
	
	public void createControl(Composite parent) {
		Composite composite = createComposite(parent, 1);
		new Label(composite, SWT.NONE).setText("Select resources:");
		
		resourceTree = new TreeViewer(composite, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		resourceTree.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		resourceTree.setContentProvider(new ResourcesContentProvider());
		resourceTree.setLabelProvider(new RepositoryLabelProvider());
		resourceTree.setInput(this);
		
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
}
