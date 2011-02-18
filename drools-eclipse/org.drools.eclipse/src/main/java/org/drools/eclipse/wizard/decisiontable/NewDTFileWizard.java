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

package org.drools.eclipse.wizard.decisiontable;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * A wizard to create a Decision Table scaffold.
 */
public class NewDTFileWizard extends Wizard implements INewWizard {

    private IWorkbench workbench;
    private IStructuredSelection selection;
    private NewDTFilePage mainPage;
    
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.workbench = workbench;
        this.selection = selection;
        setWindowTitle("New Decision Table");
        ImageDescriptor desc = DroolsEclipsePlugin.getImageDescriptor("icons/dt-large.gif");
        setDefaultPageImageDescriptor(desc);
    }
    
    public void addPages() {
        mainPage = new NewDTFilePage(workbench, selection);
        addPage(mainPage);
     }

    public boolean performFinish() {
        return mainPage.finish();
    }
    


}
