package org.guvnor.tools.views;

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.guvnor.tools.views.model.ResourceHistoryEntry;
/**
 * Label provider for the Resource History view.
 * @author jgraham
 *
 */
public class ResourceHistoryLabelProvider implements ITableLabelProvider {

	public Image getColumnImage(Object element, int columnIndex) {
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (!(element instanceof ResourceHistoryEntry)) {
			return element.toString();
		}
		ResourceHistoryEntry entry = (ResourceHistoryEntry) element;
		String res = null;
		switch (columnIndex) {
		case 0:
			res = entry.getRevision();
			break;
		case 1:
			res = entry.getDate();
			break;
		case 2:
			res = entry.getAuthor();
			break;
		case 3:
			res = entry.getComment();
			break;
		default:
			res = entry.toString();
			break;
		}
		return res;

	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}
}
