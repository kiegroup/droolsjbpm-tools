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

/*
 * Created on 11-jan-2005
 */
package org.drools.eclipse.wizard.dsl;

import java.io.IOException;
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
 * A page to create a new Domain Specific Language configuration.
 * There may be additional options here in future.
 */
public class NewDSLFilePage extends WizardNewFileCreationPage {

    private IWorkbench workbench;
    private boolean exampleContent;

    public NewDSLFilePage(IWorkbench workbench, IStructuredSelection selection) {
        super("createDSLFilePage", selection);
        setTitle("New DSL");
        setDescription("Create a new Domain Specific Language configuration");
        this.workbench = workbench;
    }

    public void createControl(Composite parent) {
        super.createControl(parent);
        setPageComplete(true);
    }

    public boolean finish(boolean exampleContent) {
        this.exampleContent = exampleContent;
        String fileName = getFileName();
        if (!fileName.endsWith(".dsl")) {
            setFileName(fileName + ".dsl");
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
        try {
            if (exampleContent) {
                return DroolsEclipsePlugin.getDefault().getBundle().getResource(
                    "org/drools/eclipse/wizard/dsl/template.dsl").openStream();
            } else {
                return null;
            }
        } catch (IOException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

}
