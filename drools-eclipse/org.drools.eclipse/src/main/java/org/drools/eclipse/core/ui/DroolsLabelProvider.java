package org.drools.eclipse.core.ui;

import org.drools.eclipse.DroolsPluginImages;
import org.drools.eclipse.core.DroolsElement;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;

public class DroolsLabelProvider extends LabelProvider {

	private static final String[] ICONS = new String[] {
		DroolsPluginImages.PACKAGE,	 // ruleset
		DroolsPluginImages.PACKAGE,  // package
		DroolsPluginImages.DROOLS, 	 // rule
		DroolsPluginImages.DROOLS, 	 // query
		DroolsPluginImages.METHOD, 	 // function
		DroolsPluginImages.CLASS, 	 // template
		DroolsPluginImages.DSL, 	 // expander
		DroolsPluginImages.GLOBAL, 	 // global
		DroolsPluginImages.IMPORT, 	 // import
		DroolsPluginImages.DROOLS, 	 // rule attribute
        DroolsPluginImages.RULEFLOW, // process
        DroolsPluginImages.DEFAULTRULEGROUP,   // Default Rule Group
        DroolsPluginImages.RULEGROUP,   // Activation Group
        DroolsPluginImages.RULEGROUP,   // Agenda Group
        DroolsPluginImages.RULEGROUP,   // RuleFlow Group
	};
	
    public Image getImage(Object element) {
    	if (element instanceof DroolsElement) {
    		String icon = ICONS[((DroolsElement) element).getType()];
    		return DroolsPluginImages.getImageRegistry().get(icon);
    	}
        return null;
    }
    
}
