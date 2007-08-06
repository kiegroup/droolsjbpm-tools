package org.drools.eclipse.wizard.decisiontable;

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
 * A page to create a new Decision table scaffolding based on a template.
 *
 * @author Michael Neale
 */
public class NewDTFilePage extends WizardNewFileCreationPage {

    private static final String XLS_EXTENSION = ".xls";
    private IWorkbench workbench;

    public NewDTFilePage(IWorkbench workbench, IStructuredSelection selection) {
        super("createDTFilePage", selection);
        setTitle("New Decision Table");
        setDescription("Create a new Decision Table scaffolding.");
        this.workbench = workbench;
    }

    public void createControl(Composite parent) {
        super.createControl(parent);
        setPageComplete(true);
    }

    public boolean finish() {
        String fileName = getFileName();
        if (!fileName.endsWith(XLS_EXTENSION)) {
            setFileName(fileName + XLS_EXTENSION);
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
                "org/drools/eclipse/wizard/decisiontable/template.xls").openStream();
        } catch (IOException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

}
