package org.drools.eclipse.debug.actions;

import org.drools.eclipse.debug.AuditView;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.ui.part.ViewPart;

public class FileAuditDropAdapter extends ViewerDropAdapter implements DropTargetListener {
	
	private AuditView view = null;
	
	public FileAuditDropAdapter(Viewer viewer, AuditView view) {
		super(viewer);
		this.view = view;
	}

	@Override
	public boolean performDrop(Object data) {
		String[] toDrop = (String[])data;
		if (toDrop.length>0) {
			view.setLogFile (toDrop[0]);
			return true;
		}
		return false;
	}
	@Override
	public boolean validateDrop(Object target, int operation,
			TransferData transferType) {
		return FileTransfer.getInstance().isSupportedType(transferType);

	}

}
