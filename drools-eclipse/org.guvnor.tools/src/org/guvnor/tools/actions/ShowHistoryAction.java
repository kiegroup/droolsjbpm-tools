package org.guvnor.tools.actions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.webdav.IResponse;
import org.guvnor.tools.Activator;
import org.guvnor.tools.utils.GuvnorMetadataProps;
import org.guvnor.tools.utils.GuvnorMetadataUtils;
import org.guvnor.tools.utils.PlatformUtils;
import org.guvnor.tools.utils.webdav.IWebDavClient;
import org.guvnor.tools.utils.webdav.WebDavClientFactory;
import org.guvnor.tools.utils.webdav.WebDavException;
import org.guvnor.tools.utils.webdav.WebDavServerCache;
import org.guvnor.tools.views.ResourceHistoryView;
import org.guvnor.tools.views.model.TreeObject;

/**
 * Shows the revision history for a given resource.
 * @author jgraham
 */
public class ShowHistoryAction implements IObjectActionDelegate {
	
	private IFile selectedFile;
	private TreeObject selectedNode;
	private GuvnorMetadataProps props;
	
	public ShowHistoryAction() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run(IAction action) {
		String repositoryLoc = null;
		String fullPath = null;
		
		if (selectedNode != null) {
			repositoryLoc = selectedNode.getGuvnorRepository().getLocation();
			fullPath = selectedNode.getFullPath();
		} else {
			if (selectedFile == null
		       || props == null) {
				return;
			}
			repositoryLoc = props.getRepository();
			fullPath = props.getFullpath();
		}
		
		IResponse response = null;
		try {
			IWebDavClient client = WebDavServerCache.getWebDavClient(repositoryLoc);
			if (client == null) {
				client = WebDavClientFactory.createClient(new URL(repositoryLoc));
				WebDavServerCache.cacheWebDavClient(repositoryLoc, client);
			}
			InputStream ins = null;
			try {
				response = client.getResourceVersions(fullPath);
				ins = response.getInputStream();
			} catch (WebDavException wde) {
				if (wde.getErrorCode() != IResponse.SC_UNAUTHORIZED) {
					// If not an authentication failure, we don't know what to do with it
					throw wde;
				}
				boolean retry = PlatformUtils.getInstance().
									authenticateForServer(repositoryLoc, client); 
				if (retry) {
					response = client.getResourceVersions(fullPath);
					ins = response.getInputStream();
				}
			}
			if (ins != null) {
				Properties verProps = new Properties();
				verProps.load(ins);
				ResourceHistoryView view = PlatformUtils.getResourceHistoryView();
				if (view != null) {
					view.setEntries(repositoryLoc, fullPath, verProps);
				}
			}
		} catch (Exception e) {
			Activator.getDefault().displayError(IStatus.ERROR, e.getMessage(), e);
		} finally {
			if (response != null) {
				try {
					response.close();
				} catch (IOException ioe) {
					Activator.getDefault().writeLog(IStatus.ERROR, ioe.getMessage(), ioe);
				}
			}
		}
	}

	/*
	 * 	(non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		// Reset state to default
		selectedFile = null;
		selectedNode = null;
		props = null;
		action.setEnabled(false);
		
		if (!(selection instanceof IStructuredSelection)) {
			return;
		}
		
		IStructuredSelection sel = (IStructuredSelection)selection;
		if (sel.size() != 1) {
			return;
		}
		
		if (sel.getFirstElement() instanceof IFile) {
			try {
				props = GuvnorMetadataUtils.getGuvnorMetadata((IFile)sel.getFirstElement());
				if (props != null) {
					selectedFile = (IFile)sel.getFirstElement();
					action.setEnabled(true);
				}
			} catch (Exception e) {
				Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
			}
		}
		if (sel.getFirstElement() instanceof TreeObject) {
			if (((TreeObject)sel.getFirstElement()).getNodeType() == TreeObject.Type.RESOURCE) {
				selectedNode = (TreeObject)sel.getFirstElement();
				action.setEnabled(true);
			}
		}
	}
}
