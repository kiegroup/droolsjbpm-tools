package org.drools.eclipse.rulebuilder.ui;

import org.drools.brms.client.modeldriven.SuggestionCompletionEngine;
import org.drools.brms.client.modeldriven.brxml.ActionAssertFact;
import org.drools.brms.client.modeldriven.brxml.ActionAssertLogicalFact;
import org.drools.brms.client.modeldriven.brxml.ActionSetField;
import org.drools.brms.client.modeldriven.brxml.DSLSentence;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * This provides a popup for new RHS action selection.
 * 
 * @author Anton Arhipov
 * @author Ahti Kitsik
 */
public class AddNewActionDialog extends RuleDialog {

	private final FormToolkit toolkit;

	private RuleModeller modeller;

	public AddNewActionDialog(Shell parent, FormToolkit toolkit,
			RuleModeller modeller) {
		super(parent, "Add a new action",
				"Pick the values from combos and confirm the selection.");
		this.toolkit = toolkit;
		this.modeller = modeller;
	}

	protected Control createDialogArea(final Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		String heading = "Choose...";

		createGlobalVariablesPart(composite, heading);

		String[] facts = getCompletion().getFactTypes();

		createFactAssertionPart(composite, heading, facts);

		createFactLogicalAssertionPart(composite, heading, facts);

		createDslSentences(composite, heading);

		return composite;
	}

	private void createDslSentences(Composite composite, String heading) {
		if (getCompletion().getDSLActions().length > 0) {
			toolkit.createLabel(composite, "Actions");

			final Combo dslCombo = new Combo(composite, SWT.READ_ONLY);
			dslCombo.add(heading);
			for (int i = 0; i < getCompletion().getDSLActions().length; i++) {
				DSLSentence sen = (DSLSentence) getCompletion().getDSLActions()[i];
				dslCombo.add(sen.toString());

				dslCombo.addListener(SWT.Selection, new Listener() {
					public void handleEvent(Event event) {
						if (dslCombo.getSelectionIndex() == 0) {
							return;
						}

						modeller.getModel()
								.addRhsItem(
										(DSLSentence) getCompletion()
												.getDSLActions()[dslCombo
												.getSelectionIndex()]);
						modeller.setDirty(true);
						modeller.reloadRhs();
						close();
					}
				});
			}
		}
	}

	private void createFactLogicalAssertionPart(Composite composite,
			String heading, String[] facts) {
		toolkit.createLabel(composite, "Logically assert a new fact");
		final Combo factsCombo = createFactsCombo(composite, heading, facts);
		factsCombo.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {

				if (factsCombo.getSelectionIndex() == 0) {
					return;
				}

				modeller.getModel().addRhsItem(
						new ActionAssertLogicalFact(factsCombo.getText()));
				modeller.setDirty(true);
				modeller.reloadRhs();
				close();
			}
		});
	}

	private void createFactAssertionPart(Composite composite, String heading,
			String[] facts) {
		toolkit.createLabel(composite, "Assert a new fact");
		final Combo factsCombo = createFactsCombo(composite, heading, facts);
		factsCombo.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (factsCombo.getSelectionIndex() == 0) {
					return;
				}

				modeller.getModel().addRhsItem(
						new ActionAssertFact(factsCombo.getText()));
				modeller.setDirty(true);
				modeller.reloadRhs();
				close();
			}
		});
	}

	private Combo createFactsCombo(Composite composite, String heading,
			String[] facts) {
		Combo factsCombo = new Combo(composite, SWT.READ_ONLY);
		factsCombo.add(heading);
		for (int i = 0; i < facts.length; i++) {
			factsCombo.add(facts[i]);
		}
		factsCombo.select(0);
		return factsCombo;
	}

	private void createGlobalVariablesPart(Composite composite, String heading) {
		toolkit.createLabel(composite, "Set the values of a field on");
		final Combo globalVarsCombo = new Combo(composite, SWT.READ_ONLY);
		globalVarsCombo.add(heading);
		String[] globalVars = getCompletion().getGlobalVariables();
		for (int i = 0; i < globalVars.length; i++) {
			globalVarsCombo.add(globalVars[i]);
		}
		globalVarsCombo.select(0);

		globalVarsCombo.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				if (globalVarsCombo.getSelectionIndex() == 0) {
					return;
				}

				modeller.getModel().addRhsItem(
						new ActionSetField(globalVarsCombo.getText()));
				modeller.setDirty(true);
				modeller.reloadRhs();
				close();
			}
		});
	}

	public SuggestionCompletionEngine getCompletion() {
		return modeller.getSuggestionCompletionEngine();
	}

}
