package org.drools.eclipse.rulebuilder.ui;

import org.drools.guvnor.client.modeldriven.SuggestionCompletionEngine;
import org.drools.guvnor.client.modeldriven.brl.ActionFieldValue;
import org.drools.guvnor.client.modeldriven.brl.ActionSetField;
import org.drools.guvnor.client.modeldriven.brl.ActionUpdateField;
import org.drools.guvnor.client.modeldriven.brl.FactPattern;
import org.drools.guvnor.client.modeldriven.brl.RuleModel;
import org.drools.eclipse.rulebuilder.modeldriven.HumanReadable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

/**
 * @author Anton Arhipov
 * @author Ahti Kitsik
 *
 */
public class ActionSetFieldWidget extends Widget {

	private ActionSetField set;

	final private String[] fieldCompletions;

	private boolean isBoundFact = false;

	private String variableClass;

	public ActionSetFieldWidget(FormToolkit toolkit, Composite parent,
			RuleModeller mod, RuleModel rule, ActionSetField set, int index) {

		super(parent, toolkit, mod, index);

		this.set = set;

		if (getCompletion().isGlobalVariable(set.variable)) {
			this.fieldCompletions = getCompletion()
					.getFieldCompletionsForGlobalVariable(set.variable);
			this.variableClass = (String) getCompletion().getGlobalVariable(set.variable);
		} else {
			FactPattern pattern = rule.getBoundFact(set.variable);
			this.fieldCompletions = getCompletion().getFieldCompletions(
					pattern.factType);
			this.isBoundFact = true;
			this.variableClass = pattern.factType;
		}

		GridLayout l = new GridLayout();
		l.numColumns = 6;
		l.marginBottom = 0;
		l.marginHeight = 0;
		l.marginLeft = 0;
		l.marginRight = 0;
		l.marginTop = 0;
		l.marginWidth = 0;
		l.verticalSpacing = 0;
		parent.setLayout(l);

		create();

	}

	private void create() {
		String modifyType = "set";
		if (this.set instanceof ActionUpdateField) {
			modifyType = "modify";
		}

		toolkit.createLabel(parent, HumanReadable
				.getActionDisplayName(modifyType)
				+ " [" + this.set.variable + "]");
		addDeleteRHSAction();
		addMoreOptionsAction();
		addRows();
	}

	private void addRows() {
		Composite constraintComposite = toolkit.createComposite(parent);
		GridLayout constraintLayout = new GridLayout();
		constraintLayout.numColumns = 3;
		constraintComposite.setLayout(constraintLayout);

		for (int i = 0; i < set.fieldValues.length; i++) {
			ActionFieldValue val = set.fieldValues[i];
			toolkit.createLabel(constraintComposite, val.field);
			valueEditor(constraintComposite, val);
			addRemoveFieldAction(constraintComposite, i);
		}

		toolkit.paintBordersFor(constraintComposite);
	}

	private void addMoreOptionsAction() {
		// ImageHyperlink link = addImage(parent,
		// "icons/add_field_to_fact.gif");
		ImageHyperlink link = addImage(parent, "icons/new_item.gif");

		link.addHyperlinkListener(new IHyperlinkListener() {
			public void linkActivated(HyperlinkEvent e) {
				RuleDialog popup = new ActionSetFieldDialog(parent.getShell(),
						getModeller(), set, fieldCompletions, variableClass);
				popup.open();

			}

			public void linkEntered(HyperlinkEvent e) {
			}

			public void linkExited(HyperlinkEvent e) {
			}
		});
		link.setToolTipText("Add a field");
	}

	private void addRemoveFieldAction(Composite constraintComposite,
			final int currentRow) {
		ImageHyperlink delLink = addImage(constraintComposite,
				"icons/delete_item_small.gif");
		delLink.setToolTipText("Remove this field action");
		delLink.addHyperlinkListener(new IHyperlinkListener() {
			public void linkActivated(HyperlinkEvent e) {
				MessageBox dialog = new MessageBox(Display.getCurrent()
						.getActiveShell(), SWT.YES | SWT.NO | SWT.ICON_WARNING);
				dialog.setMessage("Remove this item?");
				dialog.setText("Remove this item?");
				if (dialog.open() == SWT.YES) {
					set.removeField(currentRow);
					getModeller().setDirty(true);
					getModeller().reloadRhs();
				}
			}

			public void linkEntered(HyperlinkEvent e) {
			}

			public void linkExited(HyperlinkEvent e) {
			}
		});
	}

	private void valueEditor(Composite parent, final ActionFieldValue val) {
		final Text box = toolkit.createText(parent, "");

		if (val.value != null) {
			box.setText(val.value);
		}

		GridData gd = new GridData( GridData.FILL_HORIZONTAL );
        gd.grabExcessHorizontalSpace = true;
        gd.minimumWidth = 100;
        box.setLayoutData( gd );

		box.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				val.value = box.getText();
				getModeller().setDirty(true);
			}
		});

		if (val.type.equals(SuggestionCompletionEngine.TYPE_NUMERIC)) {
			new NumericKeyFilter(box);
		}

	}

	private SuggestionCompletionEngine getCompletion() {
		return getModeller().getSuggestionCompletionEngine();
	}

}
