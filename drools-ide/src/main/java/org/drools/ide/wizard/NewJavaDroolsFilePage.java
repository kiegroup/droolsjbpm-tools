/*
 * Created on 11-jan-2005
 *
 */
package org.drools.ide.wizard;

import java.io.IOException;
import java.io.InputStream;


import org.drools.ide.DroolsIDEPlugin;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.WizardNewFileCreationPage;
import org.eclipse.ui.ide.IDE;

/**
 * A page to create a new .drl file.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class NewJavaDroolsFilePage extends WizardNewFileCreationPage {

    private IWorkbench workbench;

    public NewJavaDroolsFilePage(IWorkbench workbench, IStructuredSelection selection) {
        super("createJavaDroolsFilePage", selection);
        setTitle("New Java Drools File");
        setDescription("Create a new Java Drools file");
        this.workbench = workbench;
    }

    public void createControl(Composite parent) {
        super.createControl(parent);
        setPageComplete(true);
    }

    public boolean finish() {
        String fileName = getFileName();
        if (!fileName.endsWith(".java.drl")) {
            setFileName(fileName + ".java.drl");
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
            DroolsIDEPlugin.log(e);
            return false;
        }
        return true;
    }
    
    protected InputStream getInitialContents() {
        try {
            return DroolsIDEPlugin.getDefault().getBundle().getResource("new.java.drl").openStream();
        } catch (IOException e) {
            return null;
        } catch (NullPointerException e) {
            return null;
        }
    }

}
