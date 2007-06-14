package org.drools.eclipse.rulebuilder.ui;

import java.util.ArrayList;
import java.util.List;

import org.drools.brms.client.modeldriven.SuggestionCompletionEngine;
import org.drools.brms.client.modeldriven.brxml.CompositeFactPattern;
import org.drools.brms.client.modeldriven.brxml.CompositeFieldConstraint;
import org.drools.brms.client.modeldriven.brxml.ConnectiveConstraint;
import org.drools.brms.client.modeldriven.brxml.FactPattern;
import org.drools.brms.client.modeldriven.brxml.FieldConstraint;
import org.drools.brms.client.modeldriven.brxml.ISingleFieldConstraint;
import org.drools.brms.client.modeldriven.brxml.SingleFieldConstraint;
import org.drools.eclipse.rulebuilder.modeldriven.HumanReadable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;

/**
 * This is the new smart widget that works off the model.
 * 
 * @author Michael Neale
 * @author Ahti Kitsik
 * @author Anton Arhipov
 * 
 */
public class FactPatternWidget extends Widget {

	private final CompositeFactPattern parentPattern;

	private final FactPattern pattern;

	public FactPatternWidget(FormToolkit toolkit, Composite parent,
			RuleModeller mod, FactPattern factPattern,
			CompositeFactPattern parentPattern, int idx) {

		super(parent, toolkit, mod, idx);

		this.pattern = factPattern;
		this.parentPattern = parentPattern;

		GridLayout l = new GridLayout();
		l.numColumns = 4;
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
		toolkit.createLabel(parent, getPatternLabel());
		addDeleteAction();
		addMoreOptionsAction();
		Composite constraintComposite = toolkit.createComposite(parent);
		GridLayout constraintLayout = new GridLayout();
		constraintLayout.numColumns = 6;
		constraintComposite.setLayout(constraintLayout);
		renderFieldConstraints(constraintComposite, true);
		toolkit.paintBordersFor(constraintComposite);
	}

	private void addMoreOptionsAction() {
		ImageHyperlink link = addImage(parent, "icons/new_item.gif");

		link.addHyperlinkListener(new IHyperlinkListener() {
			public void linkActivated(HyperlinkEvent e) {
				RuleDialog popup = new AddNewFieldConstraintDialog(parent
						.getShell(), toolkit, getModeller(), pattern,
						parentPattern != null);
				popup.open();
			}

			public void linkEntered(HyperlinkEvent e) {
			}

			public void linkExited(HyperlinkEvent e) {
			}
		});
		link.setToolTipText("Add a field to this condition, or bind a varible to this fact.");
	}

	private void addDeleteAction() {
		ImageHyperlink delWholeLink = addImage(parent, "icons/delete_obj.gif");
		delWholeLink.addHyperlinkListener(new IHyperlinkListener() {
			public void linkActivated(HyperlinkEvent e) {
				MessageBox dialog = new MessageBox(Display.getCurrent()
						.getActiveShell(), SWT.YES | SWT.NO | SWT.ICON_WARNING);
				dialog.setMessage("Remove this ENTIRE condition, "
						+ "and all the field constraints that belong to it.");
				dialog.setText("Remove this entire condition?");
				if (dialog.open() == SWT.YES) {
					if (parentPattern == null) {
						if (getModeller().getModel().removeLhsItem(index)) {
							getModeller().reloadLhs();
						} else {
							showMessage("Can't remove that item as it is used in the action part of the rule.");
						}
					} else {
						deleteBindedFact();
					}
					getModeller().setDirty(true);
				}
			}

			public void linkEntered(HyperlinkEvent e) {
			}

			public void linkExited(HyperlinkEvent e) {
			}
		});
		delWholeLink.setToolTipText("Remove this condition.");
	}

	private void renderFieldConstraints(Composite constraintComposite, boolean showBinding) {
		for (int row = 0; row < pattern.getFieldConstraints().length; row++) {
			FieldConstraint constraint = pattern.getFieldConstraints()[row];

			if (constraint instanceof SingleFieldConstraint) {
				renderSingleFieldConstraint(constraintComposite, row, constraint, showBinding);
			} else if (constraint instanceof CompositeFieldConstraint) {
				//TODO:
				// compositeFieldConstraintEditor
			}
		}
	}

	private void renderSingleFieldConstraint(Composite constraintComposite,
			int row, FieldConstraint constraint, boolean showBinding) {
		final SingleFieldConstraint c = (SingleFieldConstraint) constraint;
		if (c.constraintValueType != SingleFieldConstraint.TYPE_PREDICATE) {
			createConstraintRow(constraintComposite, row, c);
		} else {
			createPredicateConstraintRow(constraintComposite, row, c);
		}
	}

	private void createConstraintRow(Composite constraintComposite, int row,
			final SingleFieldConstraint c) {
		toolkit.createLabel(constraintComposite, c.fieldName);
		if (c.connectives == null || c.connectives.length == 0) {
			addRemoveFieldAction(constraintComposite, row);
		} else {
			toolkit.createLabel(constraintComposite, "");
		}
		operatorDropDown(constraintComposite, c);
		constraintValueEditor(constraintComposite, c);
		createConnectives(constraintComposite, c);
		addConnectiveAction(constraintComposite, c);
	}

	private void createPredicateConstraintRow(Composite constraintComposite,
			int row, final SingleFieldConstraint c) {
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 4;
		addImage(constraintComposite, "icons/function_assets.gif");
		formulaValueEditor(constraintComposite, c, gd);
		addRemoveFieldAction(constraintComposite, row);
	}

	private void createConnectives(Composite parent, SingleFieldConstraint c) {
		if (c.connectives != null && c.connectives.length > 0) {
			for (int i = 0; i < c.connectives.length; i++) {

				toolkit.createLabel(parent, ""); // dummy
				toolkit.createLabel(parent, ""); // dummy
				// toolkit.createLabel(parent, ""); // dummy

				ConnectiveConstraint con = c.connectives[i];
				addRemoveConstraintAction(parent, c, con);
				connectiveOperatorDropDown(parent, con, c.fieldName);
				constraintValueEditor(parent, con);

			}
		}
	}

	private void addConnectiveAction(Composite constraintComposite,
			final SingleFieldConstraint c) {
		ImageHyperlink link = addImage(constraintComposite,
				"icons/add_connective.gif");
		link.setToolTipText("Add more options to this fields values.");
		link.addHyperlinkListener(new IHyperlinkListener() {
			public void linkActivated(HyperlinkEvent e) {
				c.addNewConnective();
				getModeller().reloadLhs();
				getModeller().setDirty(true);
			}

			public void linkEntered(HyperlinkEvent e) {
			}

			public void linkExited(HyperlinkEvent e) {
			}
		});
	}

	private void addRemoveFieldAction(Composite constraintComposite,
			final int currentRow) {
		ImageHyperlink delLink = addImage(constraintComposite,
				"icons/delete_item_small.gif");
		delLink.setToolTipText("Remove this fieldconstraint");
		delLink.addHyperlinkListener(new IHyperlinkListener() {
			public void linkActivated(HyperlinkEvent e) {
				MessageBox dialog = new MessageBox(Display.getCurrent()
						.getActiveShell(), SWT.YES | SWT.NO | SWT.ICON_WARNING);
				dialog.setMessage("Remove this item?");
				dialog.setText("Remove this item?");
				if (dialog.open() == SWT.YES) {
					pattern.removeConstraint(currentRow);
					getModeller().reloadLhs();
					getModeller().setDirty(true);
				}
			}

			public void linkEntered(HyperlinkEvent e) {
			}

			public void linkExited(HyperlinkEvent e) {
			}
		});
		delLink.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_END));
	}

	private void addRemoveConstraintAction(Composite composite,
			final SingleFieldConstraint constraint,
			final ConnectiveConstraint connConstraint) {
		ImageHyperlink delLink = addImage(composite,
				"icons/delete_item_small.gif");
		delLink.setToolTipText("Remove this field constraint");
		delLink.addHyperlinkListener(new IHyperlinkListener() {
			public void linkActivated(HyperlinkEvent e) {
				MessageBox dialog = new MessageBox(Display.getCurrent()
						.getActiveShell(), SWT.YES | SWT.NO | SWT.ICON_WARNING);
				dialog.setMessage("Remove this item?");
				dialog.setText("Remove this item?");
				if (dialog.open() == SWT.YES) {
					ConnectiveConstraint[] connectives = constraint.connectives;
					List nConnectives = new ArrayList();
					for (int i = 0; i < connectives.length; i++) {
						if (connectives[i] != connConstraint) {
							nConnectives.add(connectives[i]);
						}
					}
					constraint.connectives = (ConnectiveConstraint[]) nConnectives
							.toArray(new ConnectiveConstraint[nConnectives
									.size()]);

					getModeller().reloadLhs();
					getModeller().setDirty(true);
				}
			}

			public void linkEntered(HyperlinkEvent e) {
			}

			public void linkExited(HyperlinkEvent e) {
			}
		});
		delLink.setLayoutData(new GridData(GridData.FILL_HORIZONTAL
				| GridData.HORIZONTAL_ALIGN_END));
	}

	/**
	 * This returns the pattern label.
	 */
	private String getPatternLabel() {
		if (pattern.boundName != null) {
			return pattern.factType + " [" + pattern.boundName + "]";
		}
		return pattern.factType;
	}

	private void operatorDropDown(Composite parent, final SingleFieldConstraint c) {
		String[] ops = getCompletions().getOperatorCompletions(
				pattern.factType, c.fieldName);
		final Combo box = new Combo(parent, SWT.SIMPLE | SWT.DROP_DOWN
				| SWT.READ_ONLY);
		for (int i = 0; i < ops.length; i++) {
			String op = ops[i];
			box.add(HumanReadable.getOperatorDisplayName(op));
			if (op.equals(c.operator)) {
				box.select(i);
			}
		}
		box.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		box.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				c.operator = HumanReadable.getOperatorName(box.getText());
				getModeller().setDirty(true);
			}
		});
	}

	private void connectiveOperatorDropDown(Composite parent,
			final ConnectiveConstraint con, String fieldName) {
		String[] ops = getCompletions().getConnectiveOperatorCompletions(
				pattern.factType, fieldName);
		final Combo box = new Combo(parent, SWT.SIMPLE | SWT.DROP_DOWN
				| SWT.READ_ONLY);
		for (int i = 0; i < ops.length; i++) {
			String op = ops[i];
			box.add(HumanReadable.getOperatorDisplayName(op));
			if (op.equals(con.operator)) {
				box.select(i);
			}
		}
		box.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		box.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				con.operator = HumanReadable.getOperatorName(box.getText());
				getModeller().setDirty(true);
			}
		});
	}

	// from org.drools.brms.client.modeldriven.ui.ConstraintValueEditor
	private void constraintValueEditor(final Composite parent, final ISingleFieldConstraint c) {
		if (c.constraintValueType == SingleFieldConstraint.TYPE_UNDEFINED) {
			ImageHyperlink link = addImage(parent, "icons/edit.gif");
			link.setToolTipText("Choose value editor type");
			link.addHyperlinkListener(new IHyperlinkListener() {
				public void linkActivated(HyperlinkEvent e) {
					RuleDialog popup = new ValueEditorTypeSelectionDialog(
							parent.getShell(), toolkit, getModeller(), c);
					popup.open();
				}

				public void linkEntered(HyperlinkEvent e) {
				}

				public void linkExited(HyperlinkEvent e) {
				}
			});

			toolkit.createLabel(parent, "");// dummy
		} else {
			switch (c.constraintValueType) {
			case SingleFieldConstraint.TYPE_LITERAL:
				literalValueEditor(parent, c, new GridData(GridData.FILL_HORIZONTAL));
				break;
			case SingleFieldConstraint.TYPE_RET_VALUE:
				addImage(parent, "icons/function_assets.gif");
				formulaValueEditor(parent, c, new GridData(GridData.FILL_HORIZONTAL));
				break;
			case SingleFieldConstraint.TYPE_VARIABLE:
				variableEditor(parent, c);
				break;
			default:
				break;
			}
		}
	}

	private void variableEditor(Composite composite, final ISingleFieldConstraint c) {
		List vars = getModeller().getModel().getBoundVariablesInScope(c);

		final Combo combo = new Combo(composite, SWT.READ_ONLY);

		if (c.value == null) {
			combo.add("Choose ...");
		}

		for (int i = 0; i < vars.size(); i++) {
			String var = (String) vars.get(i);
			if (c.value != null && c.value.equals(var)) {
				combo.select(i);
			}
			combo.add(var);
		}

		combo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				c.value = combo.getText();
			}
		});

	}

	private void literalValueEditor(Composite parent, final ISingleFieldConstraint c,
			GridData gd) {
		final Text box = toolkit.createText(parent, "");

		if (c.value != null) {
			box.setText(c.value);
		}

		gd.horizontalSpan = 2;
		box.setLayoutData(gd);

		box.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				c.value = box.getText();
				getModeller().setDirty(true);
			}
		});
	}

	private void formulaValueEditor(Composite parent, final ISingleFieldConstraint c,
			GridData gd) {

		final Text box = toolkit.createText(parent, "");

		if (c.value != null) {
			box.setText(c.value);
		}

		box.setLayoutData(gd);

		box.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				c.value = box.getText();
				getModeller().setDirty(true);
			}
		});

	}

	private void deleteBindedFact() {
		List newPatterns = new ArrayList();
		for (int i = 0; i < parentPattern.patterns.length; i++) {
			if (parentPattern.patterns[i] != pattern) {
				newPatterns.add(parentPattern.patterns[i]);
			}
		}
		parentPattern.patterns = (FactPattern[]) newPatterns
				.toArray(new FactPattern[newPatterns.size()]);
		getModeller().reloadLhs();
	}

	private SuggestionCompletionEngine getCompletions() {
		return getModeller().getSuggestionCompletionEngine();
	}

}
