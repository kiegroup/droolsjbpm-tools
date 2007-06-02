package org.drools.eclipse.rulebuilder.ui;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;

public class RuleAttributesDialog extends RuleDialog {

	private final FormToolkit toolkit;

    private RuleModeller      modeller;
	
	public RuleAttributesDialog(Shell parent, FormToolkit toolkit, RuleModeller modeller) {
		super(parent, "Add new option to the rule", "Pick the value from combo and confirm the selection."); // TODO: set title and hint
		
		this.toolkit = toolkit;
		this.modeller = modeller;
	}
	

}
