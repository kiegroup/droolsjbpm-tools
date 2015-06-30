package org.kie.eclipse;

import org.eclipse.ui.IStartup;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.internal.dialogs.WizardCollectionElement;
import org.eclipse.ui.internal.dialogs.WorkbenchWizardElement;
import org.eclipse.ui.internal.wizards.AbstractExtensionWizardRegistry;
import org.eclipse.ui.wizards.IWizardCategory;
import org.eclipse.ui.wizards.IWizardDescriptor;

public class ReconfigureWizardsEarlyStartup implements IStartup, IKieConstants {

	private static String BPMN2_METAMODEL_WIZRD = "org.eclipse.bpmn2.presentation.Bpmn2ModelWizardID";
    private static String[] wizardIdsToRemove = new String[] {
    	BPMN2_METAMODEL_WIZRD,
    	BPMN2_MODELER_JBPM_WIZARD
    };

	public ReconfigureWizardsEarlyStartup() {
	}

	@SuppressWarnings("restriction")
	@Override
	public void earlyStartup() {
        
        // remove BPMN2 category wizards
        try {
	        AbstractExtensionWizardRegistry wizardRegistry = (AbstractExtensionWizardRegistry)WorkbenchPlugin.getDefault().getNewWizardRegistry();
	        IWizardCategory[] categories = wizardRegistry.getRootCategory().getCategories();
	        // find the Drools and jBPM categories, and the BPMN2 Modeler's New jBPM Process wizard
	        IWizardCategory droolsWizardCategory = null;
	        IWizardCategory jbpmWizardCategory = null;
	        IWizardDescriptor bpmn2ModelerWizard = null;
	        for (IWizardCategory category : categories) {
	        	if (DROOLS_WIZARD_CATEGORY_ID.equals(category.getId())) {
	        		droolsWizardCategory = category;
	        	}
	        	if (JBPM_WIZARD_CATEGORY_ID.equals(category.getId())) {
	        		jbpmWizardCategory = category;
	        	}
	            for(IWizardDescriptor wizard : category.getWizards()) {
	            	if (BPMN2_MODELER_JBPM_WIZARD.equals(wizard.getId())) {
	            		bpmn2ModelerWizard = wizard;
	            		break;
	            	}
	            }
	        }
	        
	        // Add the New jBPM Process wizard defined in the BPMN2 Modeler plugin
	        // to both the Drools and jBPM Wizard categories.
	        if (bpmn2ModelerWizard!=null) {
	        	if (droolsWizardCategory!=null)
	        		((WizardCollectionElement) droolsWizardCategory).add(bpmn2ModelerWizard);
	        	if (jbpmWizardCategory!=null)
	        		((WizardCollectionElement) jbpmWizardCategory).add(bpmn2ModelerWizard);
	        }
	        
	        // Remove these wizards - they are duplicates or unnecessary
	        for (IWizardCategory category : categories) {
	            for(IWizardDescriptor wizard : category.getWizards()) {
	            	for (String id : wizardIdsToRemove) {
	            		if (id.equals(wizard.getId())) {
		                	WorkbenchWizardElement wizardElement = (WorkbenchWizardElement) wizard;
		                	wizardRegistry.removeExtension(wizardElement.getConfigurationElement().getDeclaringExtension(), new Object[]{wizardElement});
		                }
		        	}
	        	}
	        }
        }
        catch (Exception ex) {
        	// Ignore all exceptions. They are probably due to eclipse being run
        	// in a headless JUnit test environment.
        }
	}

}
