/*
 * Created on 11-jan-2005
 *
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
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 * @author Michael Neale
 */
public class NewDSLFilePage extends WizardNewFileCreationPage {

    private IWorkbench workbench;

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

    public boolean finish() {
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
            return DroolsEclipsePlugin.getDefault().getBundle().getResource(
                "org/drools/eclipse/wizard/dsl/template.dsl").openStream();
        } catch (IOException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

}
