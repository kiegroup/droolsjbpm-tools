package org.drools.ide.wizard.rule;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * This is a wizard to create a new .drl file (for example a rule or a whole rule package).
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 * @author Michael Neale
 */
public class NewRulePackageWizard extends Wizard implements INewWizard {

    private IWorkbench workbench;
    private IStructuredSelection selection;
    private NewRulePackagePage mainPage;
    
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.workbench = workbench;
        this.selection = selection;
        setWindowTitle("New Rule Package...");
    }
    
    public void addPages() {
        mainPage = new NewRulePackagePage(workbench, selection);
        addPage(mainPage);
     }

    public boolean performFinish() {
        return mainPage.finish();
    }

}
