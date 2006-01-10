package org.drools.ide.wizard;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * A wizard to create a new .drl file.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class NewJavaDroolsFileWizard extends Wizard implements INewWizard {

    private IWorkbench workbench;
    private IStructuredSelection selection;
    private NewJavaDroolsFilePage mainPage;
    
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.workbench = workbench;
        this.selection = selection;
        setWindowTitle("New Java Drools File");
    }
    
    public void addPages() {
        mainPage = new NewJavaDroolsFilePage(workbench, selection);
        addPage(mainPage);
     }

    public boolean performFinish() {
        return mainPage.finish();
    }

}
