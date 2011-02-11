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

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.guvnor.tools.Activator;
import org.guvnor.tools.utils.ActionUtils;
import org.guvnor.tools.utils.PlatformUtils;
import org.guvnor.tools.wizards.AddResourceWizard;

/**
 * Adds a new resource to Guvnor.
 */
public class AddAction implements IObjectActionDelegate {

    private IStructuredSelection selectedItems;
    private IWorkbenchPart targetPart;

    public AddAction() {
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
    public void run(IAction action) {
        assert(targetPart != null && selectedItems != null);
        AddResourceWizard wiz = new AddResourceWizard();
        wiz.init(Activator.getDefault().getWorkbench(), selectedItems);
        WizardDialog dialog = new WizardDialog(targetPart.getSite().getShell(), wiz);
        dialog.create();
        if (dialog.open() == WizardDialog.OK) {
            PlatformUtils.refreshRepositoryView();
        }
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        boolean validResourceSet = ActionUtils.checkResourceSet(selection, false);
        if (validResourceSet) {
            action.setEnabled(true);
            selectedItems = (IStructuredSelection)selection;
        } else {
            action.setEnabled(false);
            selectedItems = null;
        }
    }
}
