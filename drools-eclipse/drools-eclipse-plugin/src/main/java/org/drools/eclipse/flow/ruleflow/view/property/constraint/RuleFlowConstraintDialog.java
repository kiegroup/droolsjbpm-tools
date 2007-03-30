package org.drools.eclipse.flow.ruleflow.view.property.constraint;
/*
 * Copyright 2005 JBoss Inc
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.drools.ruleflow.core.IConstraint;
import org.drools.ruleflow.core.IRuleFlowProcess;
import org.drools.ruleflow.core.impl.Constraint;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog for editing constraints.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class RuleFlowConstraintDialog extends Dialog {

	private IConstraint constraint;
	private boolean success;
	private Button alwaysTrue;
	private Text nameText;
	private Text priorityText;
	private TabFolder tabFolder;
	private Text translation;

	public RuleFlowConstraintDialog(Shell parentShell, IRuleFlowProcess process) {
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Constraint editor");
	}

	protected Point getInitialSize() {
		return new Point(600, 450);
	}

	private Control createTextualEditor(Composite parent) {
		translation = new Text(parent, SWT.BORDER);
		return translation;
	}

	public Control createDialogArea(Composite parent) {

		GridLayout layout = new GridLayout();
		parent.setLayout(layout);
		layout.numColumns = 2;

		Composite top = new Composite(parent, SWT.NONE);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		gd.grabExcessHorizontalSpace = true;
		top.setLayoutData(gd);

		layout = new GridLayout();
		layout.numColumns = 2;
		top.setLayout(layout);

		Label l1 = new Label(top, SWT.None);
		l1.setText("Name:");
		gd = new GridData();
		l1.setLayoutData(gd);
		nameText = new Text(top, SWT.BORDER);
		gd = new GridData();
		gd.horizontalAlignment = GridData.FILL;
		gd.widthHint = 200;
		nameText.setLayoutData(gd);

		Label l2 = new Label(top, SWT.NONE);
		gd = new GridData();
		l2.setLayoutData(gd);
		l2.setText("Priority:");
		priorityText = new Text(top, SWT.BORDER);
		gd = new GridData();
		gd.widthHint = 200;
		priorityText.setLayoutData(gd);

		alwaysTrue = new Button(top, SWT.CHECK);
		alwaysTrue.setText("Always true");
		gd = new GridData();
		gd.horizontalSpan = 2;
		alwaysTrue.setLayoutData(gd);

		tabFolder = new TabFolder(parent, SWT.NONE);
		gd = new GridData();
		gd.horizontalSpan = 2;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		gd.verticalAlignment = GridData.FILL;
		gd.horizontalAlignment = GridData.FILL;
		tabFolder.setLayoutData(gd);
		TabItem textEditorTab = new TabItem(tabFolder, SWT.NONE);
		textEditorTab.setText("Textual Editor");

		textEditorTab.setControl(createTextualEditor(tabFolder));

		alwaysTrue.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				tabFolder.setVisible(!alwaysTrue.getSelection());
			}

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		return tabFolder;
	}

	protected void okPressed() {
		int selectionIndex = tabFolder.getSelectionIndex();
		if (selectionIndex == 1) {
			updateTranslation();
		} else {
			updateConstraint();
		}
		super.okPressed();
	}

	private void updateTranslation() {
		// TODO add custom token model checker
		success = true;
		constraint = new Constraint();
		constraint.setConstraint(null);
		constraint.setConstraint(translation.getText());
		constraint.setName(nameText.getText());
		try {
			constraint.setPriority(Integer.parseInt(priorityText.getText()));
		} catch (NumberFormatException exc) {
			constraint.setPriority(1);
		}
	}

	public boolean isSuccess() {
		return success;
	}

	public void updateConstraint() {
		constraint = new Constraint();
		if (!alwaysTrue.getSelection()) {
			constraint.setConstraint(translation.getText());
		} else {
			constraint.setConstraint("true");
		}
		constraint.setName(nameText.getText());
		try {
			constraint.setPriority(Integer.parseInt(priorityText.getText()));
		} catch (NumberFormatException exc) {
			constraint.setPriority(1);
		}
	}

	public IConstraint getConstraint() {
		return constraint;
	}

	public void setConstraint(IConstraint constraint) {
		this.constraint = constraint;
		if (constraint != null) {
			if ("true".equals(constraint.getConstraint())) {
				alwaysTrue.setSelection(true);
			} else {
				translation.setText(constraint.getConstraint().toString());
			}
			tabFolder.setVisible(!alwaysTrue.getSelection());
			nameText.setText(constraint.getName() == null ? "" : constraint
					.getName());
			priorityText.setText(constraint.getPriority() + "");
			translation.setText(constraint.getConstraint());
		} else {
			priorityText.setText("1");
			nameText.setText("constraint");
		}
	}
}
