/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.guvnor.tools.Activator;
import org.guvnor.tools.utils.ActionUtils;
import org.guvnor.tools.utils.GuvnorMetadataUtils;
import org.guvnor.tools.utils.PlatformUtils;

/**
 * Disconnects a local resource from its association with Guvnor.
 */
public class DisconnectAction implements IObjectActionDelegate {

    private IStructuredSelection selectedItems;

    public DisconnectAction() {
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
        if (selectedItems == null) {
            return;
        }
        disconnect(selectedItems);
        PlatformUtils.updateDecoration();
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
        } catch (CoreException e) {
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
