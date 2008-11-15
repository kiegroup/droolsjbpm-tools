package org.drools.eclipse.wizard.dsl;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * A wizard to create a new Domain Specific Language.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 * @author Michael Neale
 */
public class NewDSLFileWizard extends Wizard implements INewWizard {

    private IWorkbench workbench;
    private IStructuredSelection selection;
    private NewDSLFilePage mainPage;
    
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.workbench = workbench;
        this.selection = selection;
        setWindowTitle("New Domain Specific Language configuration");
    	ImageDescriptor desc = DroolsEclipsePlugin.getImageDescriptor("icons/dsl-large.png");
        setDefaultPageImageDescriptor(desc);        
    }
    
    public void addPages() {
        mainPage = new NewDSLFilePage(workbench, selection);
        addPage(mainPage);
     }

    public boolean performFinish() {
        return mainPage.finish();
    }
    


}
