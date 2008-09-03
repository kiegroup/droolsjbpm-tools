package org.guvnor.tools.utils;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.guvnor.tools.views.model.ResourceHistoryEntry;

/**
 * Sorts resource versions based on revision number.
 * @author jgraham
 */
public class ResourceHistorySorter extends ViewerSorter {

	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (e1 instanceof ResourceHistoryEntry
		   && e2 instanceof ResourceHistoryEntry) {
			ResourceHistoryEntry entry1 = (ResourceHistoryEntry)e1;
			ResourceHistoryEntry entry2 = (ResourceHistoryEntry)e2;
			return Integer.parseInt(entry2.getRevision()) - Integer.parseInt(entry1.getRevision());
		} else {
			return super.compare(viewer, e1, e2);
		}
	}
	
}
