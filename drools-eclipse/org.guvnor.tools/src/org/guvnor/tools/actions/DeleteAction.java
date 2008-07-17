package org.guvnor.tools.actions;

import java.net.URL;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.webdav.IResponse;
import org.guvnor.tools.Activator;
import org.guvnor.tools.utils.ActionUtils;
import org.guvnor.tools.utils.GuvnorMetadataProps;
import org.guvnor.tools.utils.GuvnorMetadataUtils;
import org.guvnor.tools.utils.PlatformUtils;
import org.guvnor.tools.utils.webdav.IWebDavClient;
import org.guvnor.tools.utils.webdav.WebDavClientFactory;
import org.guvnor.tools.utils.webdav.WebDavException;
import org.guvnor.tools.utils.webdav.WebDavServerCache;

public class DeleteAction implements IObjectActionDelegate {
	
	private IStructuredSelection selectedItems;
	
	private IWorkbenchPart targetPart;
	
	/**
	 * Constructor for Action1.
	 */
	public DeleteAction() {
		super();
	}

	/*
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
		this.targetPart = targetPart;
	}

	/*
	 * @see IActionDelegate#run(IAction)
	 */
	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		if (selectedItems == null) {
			return;
		}
		String msg = null;
		if (selectedItems.size() == 1) {
			msg = "Delete " + 
			      ((IFile)selectedItems.getFirstElement()).getName() + " in Guvnor?";
		} else {
			msg = "Delete these " + selectedItems.size() + " resources in Guvnor?";
		}
		if (!MessageDialog.openConfirm(targetPart.getSite().getShell(), 
				                      "Confirm Delete", msg)) {
			return;
		}
		for (Iterator it = selectedItems.iterator(); it.hasNext();) {
			Object oneItem = it.next();
			if (oneItem instanceof IFile) {
				processDelete((IFile)oneItem);
			}
		}
		DisconnectAction dsAction = new DisconnectAction();
		dsAction.disconnect(selectedItems);
		PlatformUtils.updateDecoration();
		PlatformUtils.refreshRepositoryView();
	}

	private void processDelete(IFile selectedFile) {
		try {
			GuvnorMetadataProps props = GuvnorMetadataUtils.getGuvnorMetadata(selectedFile);
			assert(props != null);
			IWebDavClient client = WebDavServerCache.getWebDavClient(props.getRepository());
			if (client == null) {
				client = WebDavClientFactory.createClient(new URL(props.getRepository()));
				WebDavServerCache.cacheWebDavClient(props.getRepository(), client);
			}
			try {
				client.deleteResource(props.getFullpath());
			} catch (WebDavException wde) {
				if (wde.getErrorCode() != IResponse.SC_UNAUTHORIZED) {
					// If not an authentication failure, we don't know what to do with it
					throw wde;
				}
				boolean retry = PlatformUtils.getInstance().
									authenticateForServer(props.getRepository(), client); 
				if (retry) {
					client.deleteResource(props.getFullpath());
				}
			}
			
		} catch (Exception e) {
			Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
		}
	}
	
	/*
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		boolean validResourceSet = ActionUtils.checkResourceSet(selection, true);
		if (validResourceSet) {
			action.setEnabled(true);
			selectedItems = (IStructuredSelection)selection;
		} else {
			action.setEnabled(false);
			selectedItems = null;
		}
	}
}
