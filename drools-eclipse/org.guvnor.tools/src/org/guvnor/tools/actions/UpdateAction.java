package org.guvnor.tools.actions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
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

public class UpdateAction implements IObjectActionDelegate {
	
	private IStructuredSelection selectedItems;
	
	/**
	 * Constructor for Action1.
	 */
	public UpdateAction() {
		super();
	}

	/*
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) { }

	/*
	 * @throws Exception 
	 * @see IActionDelegate#run(IAction)
	 */
	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		assert(selectedItems != null);
		for (Iterator it = selectedItems.iterator(); it.hasNext();) {
			Object oneItem = it.next();
			if (oneItem instanceof IFile) {
				processUpdate((IFile)oneItem);
			}
		}
		PlatformUtils.updateDecoration();
	}
	
	private void processUpdate(IFile selectedFile) {
		IResponse response = null;
		try {
			GuvnorMetadataProps props = GuvnorMetadataUtils.getGuvnorMetadata(selectedFile);
			IWebDavClient client = WebDavServerCache.getWebDavClient(props.getRepository());
			if (client == null) {
				client = WebDavClientFactory.createClient(new URL(props.getRepository()));
				WebDavServerCache.cacheWebDavClient(props.getRepository(), client);
			}
			InputStream ins = null;
			try {
				response = client.getResourceInputStream(props.getFullpath()); 
				ins = response.getInputStream();
			} catch (WebDavException wde) {
				if (wde.getErrorCode() != IResponse.SC_UNAUTHORIZED) {
					// If not an authentication failure, we don't know what to do with it
					throw wde;
				}
				boolean retry = PlatformUtils.getInstance().
									authenticateForServer(props.getRepository(), client); 
				if (retry) {
					response = client.getResourceInputStream(props.getFullpath());
					ins = response.getInputStream();
				}
			}
			if (ins != null) {
				selectedFile.setContents(ins, true, true, null);
				GuvnorMetadataUtils.markCurrentGuvnorResource(selectedFile);
				ResourceProperties resProps = client.queryProperties(props.getFullpath());
				GuvnorMetadataProps mdProps = GuvnorMetadataUtils.getGuvnorMetadata(selectedFile);
				mdProps.setVersion(resProps.getLastModifiedDate());
				mdProps.setRevision(resProps.getRevision());
				GuvnorMetadataUtils.setGuvnorMetadataProps(selectedFile.getFullPath(), mdProps);
			}
		} catch (Exception e) {
			Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
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
	
	private ResourceProperties getRemoteResourceProps(IFile file, GuvnorMetadataProps localProps) {
		ResourceProperties remoteProps = null;
		try {
			IWebDavClient client = WebDavServerCache.getWebDavClient(localProps.getRepository());
			if (client == null) {
				client = WebDavClientFactory.createClient(new URL(localProps.getRepository()));
				WebDavServerCache.cacheWebDavClient(localProps.getRepository(), client);
			}
			try {
				remoteProps = client.queryProperties(localProps.getFullpath());
			} catch (WebDavException wde) {
				if (wde.getErrorCode() != IResponse.SC_UNAUTHORIZED) {
					// If not an authentication failure, we don't know what to do with it
					throw wde;
				}
				boolean retry = PlatformUtils.getInstance().
									authenticateForServer(localProps.getRepository(), client); 
				if (retry) {
					remoteProps = client.queryProperties(localProps.getFullpath());
				}
			}
		} catch (Exception e) {
			Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
		}
		return remoteProps;
	}
	
	@SuppressWarnings("unchecked")
	private boolean hasChangedRevision(ISelection selection) {
		boolean res = true;
		try {
			if (!(selection instanceof IStructuredSelection)) {
				return false;
			}
			IStructuredSelection sel = (IStructuredSelection)selection;
			for (Iterator<Object> it = sel.iterator(); it.hasNext();) {
				Object oneSelection = it.next();
				if (oneSelection instanceof IFile) {
					GuvnorMetadataProps localProps = 
						GuvnorMetadataUtils.getGuvnorMetadata((IFile)oneSelection);
					if (localProps != null) {
						ResourceProperties remoteProps = 
							getRemoteResourceProps((IFile)oneSelection, localProps);
						if (remoteProps == null) {
							res = false;
							break;
						}
						if (remoteProps.getRevision().equals(localProps.getRevision())) {
							res = false;
							break;
						}
					} else {
						res = false;
						break;
					}
				}
			}
		} catch (Exception e) {
			Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
			res = false;
		}
		return res;
	}
	
	/*
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		boolean validResourceSet = 
			(ActionUtils.checkResourceSet(selection, true) && ActionUtils.areFilesDirty(selection))
			|| hasChangedRevision(selection);
		
		if (validResourceSet) {
			action.setEnabled(true);
			selectedItems = (IStructuredSelection)selection;
		} else {
			action.setEnabled(false);
			selectedItems = null;
		}
	}
}
