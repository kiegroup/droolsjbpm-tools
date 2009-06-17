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
import java.util.List;
import java.util.Map;

import org.drools.definition.process.Connection;
import org.drools.eclipse.flow.common.view.property.EditBeanDialog;
import org.drools.workflow.core.Constraint;
import org.drools.workflow.core.WorkflowProcess;
import org.drools.workflow.core.impl.ConnectionRef;
import org.drools.workflow.core.impl.NodeImpl;
import org.drools.workflow.core.node.StateNode;
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
public class StateConstraintListDialog extends EditBeanDialog<Map<ConnectionRef, Constraint>> {

	private WorkflowProcess process;
	private StateNode stateNode;
	private Map<ConnectionRef, Constraint> newMap;
	private Map<Connection, Label> labels = new HashMap<Connection, Label>();

	protected StateConstraintListDialog(Shell parentShell, WorkflowProcess process,
			StateNode stateNode) {
		super(parentShell, "Edit Constraints");
		this.process = process;
		this.stateNode = stateNode;
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 3;
		composite.setLayout(gridLayout);

		List<Connection> outgoingConnections = stateNode.getOutgoingConnections(NodeImpl.CONNECTION_DEFAULT_TYPE);
		labels.clear();
		for (Connection outgoingConnection: outgoingConnections) {
			Label label1 = new Label(composite, SWT.NONE);
			label1.setText("To node "
		        + outgoingConnection.getTo().getName() + ": ");
			Label label2 = new Label(composite, SWT.NONE);
			labels.put(outgoingConnection, label2);
			GridData gridData = new GridData();
			gridData.grabExcessHorizontalSpace = true;
			gridData.horizontalAlignment = GridData.FILL;
			label2.setLayoutData(gridData);
			Constraint constraint = newMap.get(
		        new ConnectionRef(outgoingConnection.getTo().getId(), outgoingConnection.getToType()));
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

	public void setValue(Map<ConnectionRef, Constraint> value) {
		super.setValue(value);
		this.newMap = new HashMap<ConnectionRef, Constraint>((Map<ConnectionRef, Constraint>) value);
	}

	protected Map<ConnectionRef, Constraint> updateValue(Map<ConnectionRef, Constraint> value) {
		return newMap;
	}

	private void editItem(final Connection connection) {

		final Runnable r = new Runnable() {
			public void run() {
				RuleFlowConstraintDialog dialog = new RuleFlowConstraintDialog(
						getShell(), process);
				dialog.create();
				ConnectionRef connectionRef = new ConnectionRef(connection.getTo().getId(), connection.getToType());
				Constraint constraint = newMap.get(connectionRef);
				dialog.setConstraint(constraint);
				dialog.fixType(0);
				dialog.fixDialect(0);
				int code = dialog.open();
				if (code != CANCEL) {
					constraint = dialog.getConstraint();
					newMap.put(
				        connectionRef,
				        constraint);
					setConnectionText(
				        (Label) labels.get(connection), constraint.getName());
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
