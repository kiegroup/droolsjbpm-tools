package org.drools.eclipse.flow.common.view.property;
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

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog for editing a list.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public abstract class EditMapDialog<T> extends EditBeanDialog<Map<String, T>> {
    
    private Class<? extends EditBeanDialog<T>> editItemDialogClass;
    private Map<String, T> newMap;
    private ListViewer listViewer;
    private Button removeButton;
    private Button editButton;

    protected EditMapDialog(Shell parentShell, String title, Class<? extends EditBeanDialog<T>> editItemDialogClass) {
        super(parentShell, title);
        this.editItemDialogClass = editItemDialogClass;
    }
    
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        composite.setLayout(gridLayout);

        listViewer = new ListViewer(composite, SWT.SINGLE);
        listViewer.add(newMap.keySet().toArray());
        listViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                removeButton.setEnabled(!event.getSelection().isEmpty());
                editButton.setEnabled(!event.getSelection().isEmpty());
            }
        });
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.verticalSpan = 3;
        listViewer.getList().setLayoutData(gridData);
        
        Button addButton = new Button(composite, SWT.NONE);
        addButton.setText("Add");
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.BEGINNING;
        addButton.setLayoutData(gridData);
        addButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                addItem();
            }
        });

        removeButton = new Button(composite, SWT.NONE);
        removeButton.setText("Remove");
        removeButton.setEnabled(false);
        gridData = new GridData();
        gridData.verticalAlignment = GridData.BEGINNING;
        removeButton.setLayoutData(gridData);
        removeButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                removeItem();
            }
        });

        editButton = new Button(composite, SWT.NONE);
        editButton.setText("Edit");
        editButton.setEnabled(false);
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.BEGINNING;
        editButton.setLayoutData(gridData);
        editButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                editItem();
            }
        });

        return composite;
    }
    
	public void setValue(Map<String, T> value) {
        super.setValue(value);
        if (value == null) {
        	this.newMap = new HashMap<String, T>();
        } else {
        	this.newMap = new HashMap<String, T>((Map<String, T>) value);
        }
    }
    
    protected Map<String, T> updateValue(Map<String, T> value) {
        return newMap;
    }

    private void addItem() {
        EditBeanDialog<T> dialog = createEditItemDialog();
        dialog.setValue(createItem());
        int code = dialog.open();
        String key = ((MapItemDialog) dialog).getKey();
        T result = dialog.getValue();
        if (code != CANCEL) {
            T object = newMap.put(key, result);
            if (object == null) {
            	listViewer.add(key);
            }
        }
    }
    
    protected abstract T createItem();
    
    @SuppressWarnings("unchecked")
	private void editItem() {
        EditBeanDialog<T> dialog = createEditItemDialog();
        Iterator<String> iterator = ((StructuredSelection) listViewer.getSelection()).iterator();
        if (iterator.hasNext()) {
        	String key = iterator.next();
            ((MapItemDialog) dialog).setKey(key);
            dialog.setValue(newMap.get(key));
            int code = dialog.open();
            T result = dialog.getValue();
            if (code != CANCEL) {
                newMap.put(key, result);
            }
        }
    }
    
    @SuppressWarnings("unchecked")
	private void removeItem() {
        Iterator<String> iterator = ((StructuredSelection) listViewer.getSelection()).iterator();
        // single selection only allowed
        if (iterator.hasNext()) {
            String key = iterator.next();
            newMap.remove(key);
            listViewer.remove(key);
        }
    }
    
	protected EditBeanDialog<T> createEditItemDialog() {
        try {
            return (EditBeanDialog<T>) editItemDialogClass.getConstructor(
                new Class[] { Shell.class }).newInstance(
                new Object[] { getShell() });
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } 
    }
}