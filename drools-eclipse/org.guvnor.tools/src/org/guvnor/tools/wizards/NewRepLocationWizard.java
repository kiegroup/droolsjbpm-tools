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

package org.guvnor.tools.wizards;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.guvnor.tools.Activator;
import org.guvnor.tools.Messages;

/**
 * Wizard page for creating a new Guvnor connection.
 */
public class NewRepLocationWizard extends Wizard implements INewWizard, IGuvnorWizard {

    private GuvnorMainConfigPage mainPage;

    private GuvWizardModel model = new GuvWizardModel();

    public GuvWizardModel getModel() {
        return model;
    }

    @Override
    public void addPages() {
        mainPage = new GuvnorMainConfigPage("config_page", Messages.getString("new.guvnor.loc"),  //$NON-NLS-1$ //$NON-NLS-2$
                                        Activator.getImageDescriptor(Activator.IMG_GUVLOCADD));
        mainPage.setDescription(Messages.getString("new.guvnor.loc.desc")); //$NON-NLS-1$
        super.addPage(mainPage);
        super.addPages();
    }

    public boolean canFinish() {
        return mainPage.isPageComplete();
    }

    @Override
    public boolean performFinish() {
        try {
            WizardUtils.createGuvnorRepository(model);
        } catch (Exception e) {
            Activator.getDefault().displayError(IStatus.ERROR, e.getMessage(), e, true);
        }
        return true;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) { }
}
