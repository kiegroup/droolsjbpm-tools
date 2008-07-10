package org.guvnor.tools.views;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.guvnor.tools.views.model.ResourceHistoryEntry;
/**
 * Structured content provider for resource history tables, etc.
 * @author jgraham
 *
 */
public class ResourceHistoryContentProvider implements IStructuredContentProvider {
	private ResourceHistoryEntry[] entries;
	
	public ResourceHistoryContentProvider(ResourceHistoryEntry[] entries) {
		this.entries = entries;
	}
	public void inputChanged(Viewer v, Object oldInput, Object newInput) {
	}
	public void dispose() {
	}
	public Object[] getElements(Object parent) {
		return entries;
	}
}
