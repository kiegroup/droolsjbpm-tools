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

package org.drools.eclipse.rulebuilder.wizards;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;


/**
 * A page to create a new rule using the Guided Editor.
 */
public class NewBrlFileWizardPage extends WizardNewFileCreationPage {

    private static final String BRL_EXTENSION = ".brl";
    private IWorkbench workbench;

    public NewBrlFileWizardPage(IWorkbench workbench, IStructuredSelection selection) {
        super("createGuidedRuleFilePage", selection);
        setTitle( "RuleBuilder Editor File" );
        setDescription( "This wizard creates a new file with *.brl extension that can be opened by a multi-page editor." );
        this.workbench = workbench;
    }

    public void createControl(Composite parent) {
        super.createControl(parent);
        setPageComplete(true);
    }

    public boolean finish() {
        String fileName = getFileName();
        if (!fileName.endsWith(BRL_EXTENSION)) {
            setFileName(fileName + BRL_EXTENSION);
        }
        org.eclipse.core.resources.IFile newFile = createNewFile();
        if (newFile == null)
            return false;
        try {
            IWorkbenchWindow dwindow = workbench.getActiveWorkbenchWindow();
            org.eclipse.ui.IWorkbenchPage page = dwindow.getActivePage();
            if (page != null)
                IDE.openEditor(page, newFile, true);
        } catch (PartInitException e) {
            DroolsEclipsePlugin.log(e);
            return false;
        }
        return true;
    }

    protected InputStream getInitialContents() {
//            return DroolsEclipsePlugin.getDefault().getBundle().getResource(
//                "org/drools/eclipse/rulebuilder/wizards/template.brl").openStream();
            String contents = "";
            return new ByteArrayInputStream( contents.getBytes() );
    }

}
