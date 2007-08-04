package org.drools.eclipse.rulebuilder.ui;

import org.drools.brms.client.modeldriven.SuggestionCompletionEngine;
import org.drools.brms.client.modeldriven.brl.ActionInsertFact;
import org.drools.brms.client.modeldriven.brl.ActionFieldValue;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;

/**
 * 
 * @author Anton Arhipov
 * @author Ahti Kitsik
 * 
 */
public class AddNewInsertedFactFieldDialog extends RuleDialog {

	private RuleModeller modeller;

	private final ActionInsertFact fact;

	public AddNewInsertedFactFieldDialog(Shell parent, RuleModeller modeller,
			ActionInsertFact fact) {
		super(parent, "Add new condition to the rule",
				"Pick the values from combos and confirm the selection.");
		this.modeller = modeller;
		this.fact = fact;
	}

	protected Control createDialogArea(final Composite parent) {

		Composite composite = (Composite) super.createDialogArea(parent);

		createLabel(composite, "Field:");

		final Combo factsCombo = new Combo(composite, SWT.READ_ONLY);

		String[] fields = getCompletion().getFieldCompletions(fact.factType);
		factsCombo.add("...");
		for (int i = 0; i < fields.length; i++) {
			factsCombo.add(fields[i]);
		}
		factsCombo.select(0);

		factsCombo.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {

				if (factsCombo.getSelectionIndex() == 0) {
					return;
				}

				String fieldType = modeller.getSuggestionCompletionEngine()
						.getFieldType(fact.factType, factsCombo.getText());
				fact.addFieldValue(new ActionFieldValue(factsCombo.getText(),
						"", fieldType));

				modeller.setDirty(true);
				modeller.reloadRhs();
				close();
			}
		});

		return composite;
	}

	public SuggestionCompletionEngine getCompletion() {
		return modeller.getSuggestionCompletionEngine();
	}

}
