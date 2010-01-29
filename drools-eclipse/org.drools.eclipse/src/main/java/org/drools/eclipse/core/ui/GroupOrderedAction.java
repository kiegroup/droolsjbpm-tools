package org.drools.eclipse.core.ui;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.custom.BusyIndicator;

public class GroupOrderedAction extends Action {
	private StructuredViewer viewer;
	private String viewerId;
	
	public GroupOrderedAction(StructuredViewer viewer, String viewerId) {	
		this.viewer = viewer;
		this.viewerId = viewerId;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		if (true) {
			viewer.getControl().setRedraw(false);
			BusyIndicator.showWhile(viewer.getControl().getDisplay(), new Runnable() {
				public void run() {
					viewer.refresh();
				}
			});
			viewer.getControl().setRedraw(true);
		}
	}

}
