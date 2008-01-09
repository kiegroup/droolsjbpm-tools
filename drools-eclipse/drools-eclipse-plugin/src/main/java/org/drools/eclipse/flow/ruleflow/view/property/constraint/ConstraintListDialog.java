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

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.drools.eclipse.flow.common.view.property.EditBeanDialog;
import org.drools.workflow.core.Connection;
import org.drools.workflow.core.Constraint;
import org.drools.workflow.core.WorkflowProcess;
import org.drools.workflow.core.node.Split;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog for editing constraints.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class ConstraintListDialog extends EditBeanDialog {

	private WorkflowProcess process;
	private Split split;
	private Map newMap;
	private Map labels = new HashMap();

	protected ConstraintListDialog(Shell parentShell, WorkflowProcess process,
			Split split) {
		super(parentShell, "Edit Constraints");
		this.process = process;
		this.split = split;
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		composite.setLayout(gridLayout);

		List outgoingConnections = split.getDefaultOutgoingConnections();
		labels.clear();
		for (Iterator it = outgoingConnections.iterator(); it.hasNext(); ) {
			Connection outgoingConnection = (Connection) it.next();
			Label label1 = new Label(composite, SWT.NONE);
			label1.setText("To node " + outgoingConnection.getTo().getName()
					+ ": ");

			Label label2 = new Label(composite, SWT.NONE);
			labels.put(outgoingConnection, label2);
			GridData gridData = new GridData();
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalAlignment = GridData.FILL;
			label2.setLayoutData(gridData);
			Constraint constraint = (Constraint) newMap.get(outgoingConnection);
			if (constraint != null) {
				label2.setText(constraint.getName());
			}

			Button editButton = new Button(composite, SWT.NONE);
			editButton.setText("Edit");
			editButton.addSelectionListener(new EditButtonListener(
					outgoingConnection));
		}

		return composite;
	}

	public void setValue(Object value) {
		super.setValue(value);
		this.newMap = new HashMap((Map) value);
	}

	protected Object updateValue(Object value) {
		return newMap;
	}

	private void editItem(final Connection connection) {

		final Runnable r = new Runnable() {
			public void run() {
				RuleFlowConstraintDialog dialog = new RuleFlowConstraintDialog(
						getShell(), process);
				dialog.create();
				Constraint constraint = (Constraint) newMap.get(connection);
				dialog.setConstraint(constraint);
				int code = dialog.open();
				if (code != CANCEL) {
					constraint = dialog.getConstraint();
					newMap.put(connection, constraint);
					setConnectionText((Label) labels.get(connection), constraint
							.getName());
				}
			}

		};
		r.run();
	}

	private void setConnectionText(final Label connection, final String name) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				connection.setText(name);
			}
		});
	}

	private class EditButtonListener extends SelectionAdapter {
		private Connection connection;

		public EditButtonListener(Connection connection) {
			this.connection = connection;
		}

		public void widgetSelected(SelectionEvent e) {
			editItem(connection);
		}
	}
}
