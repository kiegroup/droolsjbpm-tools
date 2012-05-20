package org.drools.eclipse.wizard.knowledgebase;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.wizard.rule.NewRulePackagePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class NewKnowledgeBaseWizard extends Wizard implements INewWizard {
    
    private IWorkbench workbench;
    private IStructuredSelection selection;
    private NewKnowledgeBaseWizardPage mainPage;
    
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        this.workbench = workbench;
        this.selection = selection;
        setWindowTitle("New Rule Package...");
        
        ImageDescriptor desc = DroolsEclipsePlugin.getImageDescriptor("icons/drools-large.PNG");
        setDefaultPageImageDescriptor(desc);
    }
    
    public void addPages() {
        mainPage = new NewKnowledgeBaseWizardPage(workbench, selection);
        addPage(mainPage);
     }

    public boolean performFinish() {
        return mainPage.finish();
    }
}
