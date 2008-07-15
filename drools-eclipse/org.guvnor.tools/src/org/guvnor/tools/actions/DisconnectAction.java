package org.guvnor.tools.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.guvnor.tools.Activator;
import org.guvnor.tools.utils.GuvnorMetadataProps;
import org.guvnor.tools.utils.GuvnorMetadataUtils;
import org.guvnor.tools.utils.PlatformUtils;

public class DisconnectAction implements IObjectActionDelegate {
	
	private IStructuredSelection selectedItems;
	
	/**
	 * Constructor for Action1.
	 */
	public DisconnectAction() {
		super();
	}

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run(IAction action) {
		if (selectedItems == null) {
			return;
		}
		disconnect(selectedItems);
	}
	
	@SuppressWarnings("unchecked")
	public void disconnect(IStructuredSelection files) {
		List<IFile> toDelete = new ArrayList<IFile>();
		for (Iterator<Object> it = files.iterator(); it.hasNext();) {
			Object oneSelection = it.next();
			if (oneSelection instanceof IFile) {
				IFile mdFile = GuvnorMetadataUtils.findGuvnorMetadata((IFile)oneSelection);
				if (mdFile != null) {
					toDelete.add(mdFile);
				}
			}
		}
		try {
			IFile[] mdFiles = new IFile[toDelete.size()];
			toDelete.toArray(mdFiles);
			IWorkspace ws = Activator.getDefault().getWorkspace();
			ws.delete(mdFiles, true, null);
			PlatformUtils.updateDecoration();
		} catch (CoreException e) {
			Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
		}
	}
	
	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	@SuppressWarnings("unchecked")
	public void selectionChanged(IAction action, ISelection selection) {
		// Reset state to default
		selectedItems = null;
		action.setEnabled(true);
		// See if we should enable for the selection
		try {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection sel = (IStructuredSelection)selection;
				for (Iterator<Object> it = sel.iterator(); it.hasNext();) {
					Object oneSelection = it.next();
					if (oneSelection instanceof IFile) {
						GuvnorMetadataProps props = GuvnorMetadataUtils.
														getGuvnorMetadata((IFile)oneSelection);
						if (props == null) {
							action.setEnabled(false);
							break;
						}
					}
				}
				if (action.isEnabled()) {
					selectedItems = sel;
				}
			} else {
				action.setEnabled(false);
			}
		} catch (Exception e) {
			Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
		}
	}
}
