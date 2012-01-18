/*
 * Copyright 2010 JBoss Inc
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

package org.jbpm.eclipse.preferences;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.jbpm.eclipse.util.JBPMRuntime;
import org.jbpm.eclipse.util.JBPMRuntimeManager;

public class JBPMRuntimesBlock implements ISelectionProvider {

	private Composite fControl;
	private List<JBPMRuntime> jbpmRuntimes = new ArrayList<JBPMRuntime>();
	private CheckboxTableViewer jbpmRuntimesList;
	private Button fAddButton;
	private Button fRemoveButton;
	private Button fEditButton;
	private ListenerList fSelectionListeners = new ListenerList();
	private ISelection fPrevSelection = new StructuredSelection();
    private Table fTable;
	
	class JBPMRuntimesContentProvider implements IStructuredContentProvider {		
		public Object[] getElements(Object input) {
			return jbpmRuntimes.toArray();
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
	}
	
	class JBPMRuntimesLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof JBPMRuntime) {
				JBPMRuntime runtime = (JBPMRuntime) element;
				switch(columnIndex) {
					case 0:
						return runtime.getName();
					case 1:
						return runtime.getPath();
				}
			}
			return element.toString();
		}
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
	}	

	public void addSelectionChangedListener(ISelectionChangedListener listener) {
		fSelectionListeners.add(listener);
	}

	public ISelection getSelection() {
		return new StructuredSelection(jbpmRuntimesList.getCheckedElements());
	}

	public void removeSelectionChangedListener(ISelectionChangedListener listener) {
		fSelectionListeners.remove(listener);
	}

	public void setSelection(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			if (!selection.equals(fPrevSelection)) {
				fPrevSelection = selection;
				Object runtime = ((IStructuredSelection)selection).getFirstElement();
				if (runtime == null) {
					jbpmRuntimesList.setCheckedElements(new Object[0]);
				} else {
					jbpmRuntimesList.setCheckedElements(new Object[]{runtime});
					jbpmRuntimesList.reveal(runtime);
				}
				fireSelectionChanged();
			}
		}
	}

	private void fireSelectionChanged() {
		SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());
		Object[] listeners = fSelectionListeners.getListeners();
		for (int i = 0; i < listeners.length; i++) {
			ISelectionChangedListener listener = (ISelectionChangedListener)listeners[i];
			listener.selectionChanged(event);
		}	
	}

	public void createControl(Composite ancestor) {
		Font font = ancestor.getFont();
		Composite parent= SWTFactory.createComposite(ancestor, font, 2, 1, GridData.FILL_BOTH);
		fControl = parent;	
				
		SWTFactory.createLabel(parent, "Installed jBPM Runtimes", 2);
				
		fTable = new Table(parent, SWT.CHECK | SWT.BORDER | SWT.MULTI | SWT.FULL_SELECTION);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.heightHint = 250;
		gd.widthHint = 350;
		fTable.setLayoutData(gd);
		fTable.setFont(font);
		fTable.setHeaderVisible(true);
		fTable.setLinesVisible(true);	

		TableColumn column = new TableColumn(fTable, SWT.NULL);
		column.setText("Name"); 
		int defaultwidth = 350/2 +1;
		column.setWidth(defaultwidth);
	
		column = new TableColumn(fTable, SWT.NULL);
		column.setText("Location"); 
		column.setWidth(defaultwidth);
		
		jbpmRuntimesList = new CheckboxTableViewer(fTable);			
		jbpmRuntimesList.setLabelProvider(new JBPMRuntimesLabelProvider());
		jbpmRuntimesList.setContentProvider(new JBPMRuntimesContentProvider());
		
		jbpmRuntimesList.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent evt) {
				enableButtons();
			}
		});
		
		jbpmRuntimesList.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getChecked()) {
					setDefaultJBPMRuntime((JBPMRuntime) event.getElement());
				} else {
					setDefaultJBPMRuntime(null);
				}
			}
		});
		
		jbpmRuntimesList.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				if (!jbpmRuntimesList.getSelection().isEmpty()) {
					editJBPMRuntime();
				}
			}
		});
		fTable.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.character == SWT.DEL && event.stateMask == 0) {
					if (fRemoveButton.isEnabled()){
						removeJBPMRuntimes();
					}
				}
			}
		});	
		
		Composite buttons = SWTFactory.createComposite(parent, font, 1, 1, GridData.VERTICAL_ALIGN_BEGINNING, 0, 0);
		
		fAddButton = SWTFactory.createPushButton(buttons, "Add...", null); 
		fAddButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event evt) {
				addJBPMRuntime();
			}
		});
		
		fEditButton= SWTFactory.createPushButton(buttons, "Edit...", null); 
		fEditButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event evt) {
				editJBPMRuntime();
			}
		});
		
		fRemoveButton= SWTFactory.createPushButton(buttons, "Remove", null); 
		fRemoveButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event evt) {
				removeJBPMRuntimes();
			}
		});
		
		SWTFactory.createVerticalSpacer(parent, 1);
		
		enableButtons();
		fAddButton.setEnabled(true);
	}
	
	private void enableButtons() {
		IStructuredSelection selection = (IStructuredSelection) jbpmRuntimesList.getSelection();
		int selectionCount= selection.size();
		fEditButton.setEnabled(selectionCount == 1);
		fRemoveButton.setEnabled(selectionCount > 0);
	}
	
	public Control getControl() {
		return fControl;
	}

	public void setJBPMRuntimes(JBPMRuntime[] runtimes) {
		jbpmRuntimes.clear();
		for (int i = 0; i < runtimes.length; i++) {
			jbpmRuntimes.add(runtimes[i]);
		}
		jbpmRuntimesList.setInput(jbpmRuntimes);
		jbpmRuntimesList.refresh();
	}

	public JBPMRuntime[] getJBPMRuntimes() {
		JBPMRuntime selected = getDefaultJBPMRuntime();
		for (JBPMRuntime runtime: jbpmRuntimes) {
			runtime.setDefault(runtime.equals(selected));
		}
		return jbpmRuntimes.toArray(new JBPMRuntime[jbpmRuntimes.size()]);
	}
	
	private void addJBPMRuntime() {
		JBPMRuntimeDialog dialog = new JBPMRuntimeDialog(getShell(), jbpmRuntimes);
		if (dialog.open() == Window.OK) {
			JBPMRuntime result = dialog.getResult();
			if (result != null) {
				JBPMRuntimeManager.recognizeJars(result);
				jbpmRuntimes.add(result);
				jbpmRuntimesList.refresh();
				jbpmRuntimesList.setSelection(new StructuredSelection(result));
			}
		}
	}
	
	private void editJBPMRuntime() {
		IStructuredSelection selection= (IStructuredSelection) jbpmRuntimesList.getSelection();
		JBPMRuntime runtime = (JBPMRuntime) selection.getFirstElement();
		if (runtime == null) {
			return;
		}
		JBPMRuntimeDialog dialog = new JBPMRuntimeDialog(getShell(), jbpmRuntimes);
		dialog.setJBPMRuntime(runtime);
		if (dialog.open() == Window.OK) {
			JBPMRuntime result = dialog.getResult();
			if (result != null) {
				JBPMRuntimeManager.recognizeJars(result);
				// replace with the edited VM
				int index = jbpmRuntimes.indexOf(runtime);
				jbpmRuntimes.remove(index);
				jbpmRuntimes.add(index, result);
				jbpmRuntimesList.refresh();
				jbpmRuntimesList.setSelection(new StructuredSelection(result));
			}
		}
	}
	
	private void removeJBPMRuntimes() {
		IStructuredSelection selection= (IStructuredSelection) jbpmRuntimesList.getSelection();
		JBPMRuntime[] runtimes = new JBPMRuntime[selection.size()];
		Iterator<?> iter = selection.iterator();
		int i = 0;
		while (iter.hasNext()) {
			runtimes[i] = (JBPMRuntime) iter.next();
			i++;
		}
		removeJBPMRuntimes(runtimes);
	}	
	
	public void removeJBPMRuntimes(JBPMRuntime[] runtimes) {
		IStructuredSelection prev = (IStructuredSelection) getSelection();
		for (int i = 0; i < runtimes.length; i++) {
			jbpmRuntimes.remove(runtimes[i]);
		}
		jbpmRuntimesList.refresh();
		IStructuredSelection curr = (IStructuredSelection) getSelection();
		if (!curr.equals(prev)) {
			runtimes = getJBPMRuntimes();
			if (curr.size() == 0 && runtimes.length == 1) {
				setSelection(new StructuredSelection(runtimes[0]));
			} else {
				fireSelectionChanged();
			}
		}
	}

	protected Shell getShell() {
		return getControl().getShell();
	}

	public void setDefaultJBPMRuntime(JBPMRuntime runtime) {
		if (runtime == null) {
			setSelection(new StructuredSelection());
		} else {
			setSelection(new StructuredSelection(runtime));
		}
	}
	
	public JBPMRuntime getDefaultJBPMRuntime() {
		Object[] objects = jbpmRuntimesList.getCheckedElements();
		if (objects.length == 0) {
			return null;
		}
		return (JBPMRuntime) objects[0];
	}

}
