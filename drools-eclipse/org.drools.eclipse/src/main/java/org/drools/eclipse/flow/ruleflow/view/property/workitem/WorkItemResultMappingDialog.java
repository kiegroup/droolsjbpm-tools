package org.drools.eclipse.flow.ruleflow.view.property.workitem;
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
import java.util.Map;

import org.drools.eclipse.flow.common.view.property.EditBeanDialog;
import org.drools.workflow.core.node.WorkItemNode;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * Dialog for editing work item result mappings.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class WorkItemResultMappingDialog extends EditBeanDialog implements FocusListener, MouseListener {

    private Table table;
    private Button removeButton;
    private Text text;
    private TableEditor editor;
    private int selectedColumn = -1;
    
	public WorkItemResultMappingDialog(Shell parentShell, WorkItemNode workItemNode) {
		super(parentShell, "Result Mapping");
		setValue(workItemNode.getOutMappings());
	}

	protected Object updateValue(Object value) {
	    Map<String, String> mapping = new HashMap<String, String>();
	    for (TableItem item: table.getItems()) {
	        mapping.put(item.getText(1), item.getText(0));
	    }
		return mapping;
	}

	protected Point getInitialSize() {
		return new Point(450, 300);
	}

	public Control createDialogArea(Composite parent) {
	    Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        composite.setLayout(layout);

        table = new Table(composite, SWT.SINGLE);
        GridData gd = new GridData();
        gd.verticalSpan = 3;
        gd.grabExcessHorizontalSpace = true;
        gd.grabExcessVerticalSpace = true;
        gd.verticalAlignment = GridData.FILL;
        gd.horizontalAlignment = GridData.FILL;
        table.setLayoutData(gd);
        table.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                removeButton.setEnabled(table.getSelectionIndex() != -1);
            }
            public void widgetSelected(SelectionEvent e) {
                removeButton.setEnabled(table.getSelectionIndex() != -1);
            }
        });
        table.addMouseListener(this);
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        TableColumn variableNameColumn = new TableColumn(table, SWT.LEFT);
        variableNameColumn.setText("Variable");
        variableNameColumn.setWidth(150);
        TableColumn parameterNameColumn = new TableColumn(table, SWT.LEFT);
        parameterNameColumn.setText("Result Parameter");
        parameterNameColumn.setWidth(225);
        
        editor = new TableEditor(table);
        text = new Text(table, SWT.NORMAL);
        text.setVisible(false);
        text.setText("");
        editor.minimumWidth = text.getSize().x;
        editor.horizontalAlignment = SWT.LEFT;
        editor.grabHorizontal = true;

		Button addButton = new Button(composite, SWT.PUSH);
		addButton.setText("Add");
		addButton.setFont(JFaceResources.getDialogFont());
		addButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				addButtonPressed();
			}
		});
		gd = new GridData();
		gd.horizontalAlignment = SWT.FILL;
		addButton.setLayoutData(gd);

		removeButton = new Button(composite, SWT.PUSH);
		removeButton.setText("Remove");
		removeButton.setFont(JFaceResources.getDialogFont());
		removeButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				removeButtonPressed();
			}
		});
		gd = new GridData();
		removeButton.setLayoutData(gd);
		removeButton.setEnabled(false);

        updateTable();
        
		return composite;
	}
	
	private void updateTable() {
        Map<String, String> mapping = (Map<String, String>) getValue();
	    for (Map.Entry<String, String> entry: mapping.entrySet()) {
            TableItem item = new TableItem(table, SWT.NONE);
            item.setText(new String[] { entry.getValue(), entry.getKey()} );
        }
	}
	
	private void addButtonPressed() {
	    TableItem item = new TableItem(table, SWT.NONE);
        item.setText(0, "variable");
        item.setText(1, "result parameter");
        table.setSelection(item);
	}
	
	private void removeButtonPressed() {
        int i = table.getSelectionIndex();
        if (i == -1) {
            return;
        }
        table.remove(i);
        removeButton.setEnabled(table.getItemCount() == 0);
	}
	
	private void doEdit() {
        if (text.isVisible()) {
            endEdit();
        }
        if (table.getSelectionIndex() == -1 || selectedColumn == -1) return;
        TableItem selection = table.getItem(table.getSelectionIndex());
        String value = selection.getText(selectedColumn);
        text.setText(value == null ? "" : value);
        editor.setEditor(text, selection, selectedColumn);
        text.setVisible(true);
        text.selectAll();
        text.setFocus();
        text.addFocusListener(this);
    }
    
    private void endEdit() {
        text.setVisible(false);
        text.setText("");
        text.removeFocusListener(this);
    }
    
    public void focusGained(FocusEvent e) {
    }

    public void focusLost(FocusEvent e) {
        if (e.widget == text) {
            applyValue();
            endEdit();
        }
    }
    
    public void mouseDoubleClick(MouseEvent e) {
    }

    public void mouseDown(MouseEvent e) {
        selectedColumn = getSelectedColumn(e.x, e.y);
        if (selectedColumn == -1) return;
        doEdit();
    }
    
    public void mouseUp(MouseEvent e) {
    }
    
    private int getSelectedColumn(int x, int y) {
        int columnToEdit = -1;
        int columns = table.getColumnCount();
        if (table.getSelection().length == 0) {
            return -1;
        }
        for (int i = 0; i < columns; i++) {
            Rectangle bounds = table.getSelection()[0].getBounds(i);
            if (bounds.contains(x, y)) {
                columnToEdit = i;
                break;
            }
        }   
        return columnToEdit;
    }


    private void applyValue() {
        int i = table.getSelectionIndex();
        if (i == -1) {
            return;
        }
        TableItem item = table.getItem(i);
        item.setText(selectedColumn, text.getText());
    }

}
