package org.guvnor.tools.actions;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.eclipse.compare.CompareUI;
import org.eclipse.compare.ITypedElement;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.webdav.IResponse;
import org.guvnor.tools.Activator;
import org.guvnor.tools.utils.GuvnorMetadataProps;
import org.guvnor.tools.utils.GuvnorMetadataUtils;
import org.guvnor.tools.utils.PlatformUtils;
import org.guvnor.tools.utils.VersionChooserDialog;
import org.guvnor.tools.utils.webdav.IWebDavClient;
import org.guvnor.tools.utils.webdav.StreamProcessingUtils;
import org.guvnor.tools.utils.webdav.WebDavClientFactory;
import org.guvnor.tools.utils.webdav.WebDavException;
import org.guvnor.tools.utils.webdav.WebDavServerCache;
import org.guvnor.tools.views.model.ResourceHistoryEntry;

/**
 * Simple text-based compare editor input.
 * @author jgraham
 */
public class CompareVersionAction implements IObjectActionDelegate {
	
	private IFile selectedFile;
	private GuvnorMetadataProps props;
	
	private IWorkbenchPart targetPart;
	
	private IWebDavClient client;
	
	/**
	 * Constructor for Action1.
	 */
	public CompareVersionAction() {
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
	public void run(IAction action) {
		if (selectedFile == null
		   || props == null
		   || targetPart == null) {
			return;
		}
		VersionChooserDialog dialog = 
			new VersionChooserDialog(targetPart.getSite().getShell(), 
					                selectedFile.getName(), 
					                getVersionEntries());
		if (dialog.open() == VersionChooserDialog.OK) {
			compareWithSelectedVersion(dialog.getSelectedEntry());
		}
	}
	
	private void compareWithSelectedVersion(ResourceHistoryEntry revision) {
		IResponse response = null;
		try {
			IWorkbenchPage page = targetPart.getSite().getPage();
			String leftContents = StreamProcessingUtils.getStreamContents(selectedFile.getContents());
			GuvnorResourceEdition left = 
				new GuvnorResourceEdition(selectedFile.getName(), 
						                 ITypedElement.TEXT_TYPE, 
						                 leftContents, selectedFile.getCharset());
			response = client.getResourceVersionInputStream(props.getFullpath(), revision.getRevision());
			String rightContents = StreamProcessingUtils.getStreamContents(response.getInputStream());
			//Assuming UTF-8 for Guvnor resources...
			GuvnorResourceEdition right = 
				new GuvnorResourceEdition(selectedFile.getName() + ", " + revision.getRevision(),
						                 ITypedElement.TEXT_TYPE, 
						                 rightContents, "UTF-8");
			CompareUI.openCompareEditorOnPage(new GuvnorCompareEditorInput(left, right), page);
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
	
	private ResourceHistoryEntry[] getVersionEntries() {
		ResourceHistoryEntry[] entries = new ResourceHistoryEntry[0];
		IResponse response = null;
		try {
			client = WebDavServerCache.getWebDavClient(props.getRepository());
			if (client == null) {
				client = WebDavClientFactory.createClient(new URL(props.getRepository()));
				WebDavServerCache.cacheWebDavClient(props.getRepository(), client);
			}
			InputStream ins = null;
			try {
				response = client.getResourceVersions(props.getFullpath());
				ins = response.getInputStream();
			} catch (WebDavException wde) {
				if (wde.getErrorCode() != IResponse.SC_UNAUTHORIZED) {
					// If not an authentication failure, we don't know what to do with it
					throw wde;
				}
				boolean retry = PlatformUtils.getInstance().
									authenticateForServer(props.getRepository(), client); 
				if (retry) {
					response = client.getResourceVersions(props.getFullpath());
					ins = response.getInputStream();
				}
			}
			if (ins != null) {
				Properties verProps = new Properties();
				verProps.load(ins);
				entries = GuvnorMetadataUtils.parseHistoryProperties(verProps);
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
		return entries;
	}
	
	/*
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged(IAction action, ISelection selection) {
		// Reset state to default
		selectedFile = null;
		props = null;
		action.setEnabled(false);
		// See if we should enable for the selection
		try {
			if (selection instanceof IStructuredSelection) {
				IStructuredSelection sel = (IStructuredSelection)selection;
				if (sel.getFirstElement() instanceof IFile
				   && sel.size() == 1) {
					props = GuvnorMetadataUtils.getGuvnorMetadata((IFile)sel.getFirstElement());
					if (props != null) {
						selectedFile = (IFile)sel.getFirstElement();
						action.setEnabled(true);
					}
				}
			} 
		} catch (Exception e) {
			Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
		}
	}
}
