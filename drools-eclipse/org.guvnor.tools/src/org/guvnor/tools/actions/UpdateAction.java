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
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.webdav.IResponse;
import org.guvnor.tools.Activator;
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

	/**
	 * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart(IAction action, IWorkbenchPart targetPart) {
	}

	/**
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
				PlatformUtils.updateDecoration();
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
