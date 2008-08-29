package org.guvnor.tools.views;

import java.util.List;

import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.progress.DeferredTreeContentManager;
import org.guvnor.tools.Activator;
import org.guvnor.tools.GuvnorRepository;
import org.guvnor.tools.utils.webdav.ResourceProperties;
import org.guvnor.tools.views.model.TreeObject;
import org.guvnor.tools.views.model.TreeParent;

public class RepositoryContentProvider implements IStructuredContentProvider, 
                                                  ITreeContentProvider {
	private TreeParent invisibleRoot;
	private DeferredTreeContentManager manager;
	private AbstractTreeViewer viewer;
	
	private String repUrl;
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		if (v instanceof AbstractTreeViewer) {
			viewer = (AbstractTreeViewer)v;
		    manager = new DeferredTreeContentManager(this, viewer);
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object parent) {
		if (parent.equals(viewer)) {
			if (invisibleRoot == null) initialize();
			return getChildren(invisibleRoot);
		}
		return getChildren(parent);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object child) {
		if (child instanceof TreeObject) {
			return ((TreeObject)child).getParent();
		}
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object [] getChildren(Object parent) {
		if (parent == invisibleRoot) {
			return ((TreeParent)invisibleRoot).getChildren();
		} else if (parent instanceof TreeParent) {
			return manager.getChildren(parent);
		}
		return new Object[0];
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object parent) {
		if (parent instanceof TreeParent) {
			return manager.mayHaveChildren(parent);
		}
		return false;
	}

	private void initialize() {
		boolean shouldAdd = true;
		invisibleRoot = new TreeParent("", TreeObject.Type.NONE); //$NON-NLS-1$
		List<GuvnorRepository> reps = Activator.getLocationManager().getRepositories();
		for (int i = 0; i < reps.size(); i++) {
			if (repUrl != null) {
				if (repUrl.equals(reps.get(i).getLocation())) {
					shouldAdd = true;
				} else {
					shouldAdd = false;
				}
			} else {
				shouldAdd = true;
			}
			if (shouldAdd) {
				TreeParent p = new TreeParent(reps.get(i).getLocation(), 
						                     TreeObject.Type.REPOSITORY);
				p.setGuvnorRepository(reps.get(i));
				ResourceProperties props = new ResourceProperties();
				props.setBase(""); //$NON-NLS-1$
				p.setResourceProps(props);
				invisibleRoot.addChild(p);
			}
		}
	}
	
	public void setRepositorySelection(String repUrl) {
		this.repUrl = repUrl;
	}
}
