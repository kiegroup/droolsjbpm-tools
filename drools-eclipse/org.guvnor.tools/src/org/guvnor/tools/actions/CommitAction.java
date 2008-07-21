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
import org.guvnor.tools.utils.webdav.ResourceProperties;
import org.guvnor.tools.utils.webdav.WebDavClientFactory;
import org.guvnor.tools.utils.webdav.WebDavException;
import org.guvnor.tools.utils.webdav.WebDavServerCache;

public class CommitAction implements IObjectActionDelegate {
	
	private IStructuredSelection selectedItems;
	private IWorkbenchPart targetPart;
	
	/**
	 * Constructor for Action1.
	 */
	public CommitAction() {
		super();
	}

	/**
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
		assert(selectedItems != null);
		for (Iterator it = selectedItems.iterator(); it.hasNext();) {
			Object oneSelection = it.next();
			if (oneSelection instanceof IFile) {
				processCommit((IFile)oneSelection);
			}
		}
		PlatformUtils.updateDecoration();
	}

	private void processCommit(IFile selectedFile) {
		try {
			GuvnorMetadataProps props = GuvnorMetadataUtils.getGuvnorMetadata(selectedFile);
			IWebDavClient client = WebDavServerCache.getWebDavClient(props.getRepository());
			if (client == null) {
				client = WebDavClientFactory.createClient(new URL(props.getRepository()));
				WebDavServerCache.cacheWebDavClient(props.getRepository(), client);
			}
			ResourceProperties remoteProps = null;
			try {
				remoteProps = client.queryProperties(props.getFullpath());
			} catch (WebDavException wde) {
				if (wde.getErrorCode() != IResponse.SC_UNAUTHORIZED) {
					// If not an authentication failure, we don't know what to do with it
					throw wde;
				}
				boolean retry = PlatformUtils.getInstance().
									authenticateForServer(props.getRepository(), client); 
				if (retry) {
					remoteProps = client.queryProperties(props.getFullpath());
				}
			}
			if (remoteProps == null) {
				throw new Exception("Could not retrieve server version of " + props.getFullpath());
			}
			// Check to make sure that the version in the repository is the same as the base
			// version for the local copy
			boolean proceed = true;
			if (!props.getRevision().equals(remoteProps.getRevision())) {
				String msg = "The Guvnor revision for " + selectedFile.getName() + " is " +
				             remoteProps.getRevision() + ", but the local copy is based on revision " +
				             props.getRevision() + 
				             ".\r\nOverwrite Guvnor repository revision with current local file contents?";
				proceed = MessageDialog.openQuestion(targetPart.getSite().getShell(), 
						                            "Revision Conflict", msg);
						
			}
			if (proceed) {
				client.putResource(props.getFullpath(), selectedFile.getContents());
				GuvnorMetadataUtils.markCurrentGuvnorResource(selectedFile);
				ResourceProperties resProps = client.queryProperties(props.getFullpath());
				GuvnorMetadataProps mdProps = GuvnorMetadataUtils.getGuvnorMetadata(selectedFile);
				mdProps.setVersion(resProps.getLastModifiedDate());
				mdProps.setRevision(resProps.getRevision());
				GuvnorMetadataUtils.setGuvnorMetadataProps(selectedFile.getFullPath(), mdProps);
			}
		} catch (Exception e) {
			Activator.getDefault().displayError(IStatus.ERROR, e.getMessage(), e);
		}
	}
	
	/*
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		boolean validResourceSet = ActionUtils.checkResourceSet(selection, true) 
		                           && ActionUtils.areFilesDirty(selection);
		if (validResourceSet) {
			action.setEnabled(true);
			selectedItems = (IStructuredSelection)selection;
		} else {
			action.setEnabled(false);
			selectedItems = null;
		}
	}
}
