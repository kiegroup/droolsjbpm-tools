/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.guvnor.tools.actions;

import java.net.URL;
import java.text.MessageFormat;
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
import org.guvnor.tools.Messages;
import org.guvnor.tools.utils.ActionUtils;
import org.guvnor.tools.utils.GuvnorMetadataProps;
import org.guvnor.tools.utils.GuvnorMetadataUtils;
import org.guvnor.tools.utils.PlatformUtils;
import org.guvnor.tools.utils.webdav.IWebDavClient;
import org.guvnor.tools.utils.webdav.WebDavClientFactory;
import org.guvnor.tools.utils.webdav.WebDavException;
import org.guvnor.tools.utils.webdav.WebDavServerCache;

/**
 * Deletes a resource in Guvnor.
 * @author jgraham
 */
public class DeleteAction implements IObjectActionDelegate {

    private IStructuredSelection selectedItems;

    private IWorkbenchPart targetPart;

    public DeleteAction() {
        super();
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
        this.targetPart = targetPart;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    @SuppressWarnings("unchecked")
    public void run(IAction action) {
        if (selectedItems == null) {
            return;
        }
        String msg = null;
        if (selectedItems.size() == 1) {
            msg = MessageFormat.format(Messages.getString("delete.singlefile.confirmation"), //$NON-NLS-1$
                                      new Object[] { ((IFile)selectedItems.getFirstElement()).getName() });
        } else {
            msg = MessageFormat.format(Messages.getString("delete.multifile.confirmation"), //$NON-NLS-1$
                                      new Object[] { String.valueOf(selectedItems.size()) });
        }
        if (!MessageDialog.openConfirm(targetPart.getSite().getShell(),
                                      Messages.getString("delete.confirmation.dialog.caption"), msg)) { //$NON-NLS-1$
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
            Activator.getDefault().displayError(IStatus.ERROR, e.getMessage(), e, true);
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
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
