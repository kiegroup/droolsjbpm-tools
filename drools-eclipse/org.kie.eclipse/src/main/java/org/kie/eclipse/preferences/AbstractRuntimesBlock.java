/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

package org.kie.eclipse.preferences;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.debug.internal.ui.SWTFactory;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.kie.eclipse.runtime.IRuntime;
import org.kie.eclipse.runtime.IRuntimeManager;

public abstract class AbstractRuntimesBlock implements ISelectionProvider {

	private IRuntimeManager runtimeManager;
    private Composite fControl;
    private List<IRuntime> runtimes = new ArrayList<IRuntime>();
    private CheckboxTableViewer runtimesList;
    private Button fAddButton;
    private Button fRemoveButton;
    private Button fEditButton;
    private ListenerList fSelectionListeners = new ListenerList();
    private ISelection fPrevSelection = new StructuredSelection();
    private Table fTable;

    class RuntimesContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object input) {
            return runtimes.toArray();
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
        public void dispose() {
        }
    }

    class RuntimesLabelProvider extends LabelProvider implements ITableLabelProvider {
        public String getColumnText(Object element, int columnIndex) {
            if (element instanceof IRuntime) {
            	IRuntime runtime = (IRuntime) element;
                switch(columnIndex) {
                    case 0:
                        return runtime.getName();
                    case 1:
                    	return runtime.getVersion();
                    case 2:
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
        return new StructuredSelection(runtimesList.getCheckedElements());
    }

    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        fSelectionListeners.remove(listener);
    }

    public void setSelection(ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            if (!selection.equals(fPrevSelection)) {
                fPrevSelection = selection;
                if (!runtimesList.getTable().isDisposed()) {
	                Object runtime = ((IStructuredSelection)selection).getFirstElement();
	                if (runtime == null) {
	                    runtimesList.setCheckedElements(new Object[0]);
	                } else {
	                    runtimesList.setCheckedElements(new Object[]{runtime});
	                    runtimesList.reveal(runtime);
	                }
	                fireSelectionChanged();
                }
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
        this.runtimeManager = getRuntimeManager();

        Font font = ancestor.getFont();
        Composite parent= SWTFactory.createComposite(ancestor, font, 2, 1, GridData.FILL_BOTH);
        fControl = parent;

        SWTFactory.createLabel(parent, "Installed Runtimes", 2);

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
        int defaultwidth = 350/3 +1;
        column.setWidth(defaultwidth);

        column = new TableColumn(fTable, SWT.NULL);
        column.setText("Version");
        column.setWidth(defaultwidth/2);

        column = new TableColumn(fTable, SWT.NULL);
        column.setText("Location");
        column.setWidth(defaultwidth * 2);

        runtimesList = new CheckboxTableViewer(fTable);
        runtimesList.setLabelProvider(new RuntimesLabelProvider());
        runtimesList.setContentProvider(new RuntimesContentProvider());

        runtimesList.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent evt) {
                enableButtons();
            }
        });

        runtimesList.addCheckStateListener(new ICheckStateListener() {
            public void checkStateChanged(CheckStateChangedEvent event) {
                if (event.getChecked()) {
                    setDefaultRuntime((IRuntime) event.getElement());
                } else {
                    setDefaultRuntime(null);
                }
            }
        });

        runtimesList.addDoubleClickListener(new IDoubleClickListener() {
            public void doubleClick(DoubleClickEvent e) {
                if (!runtimesList.getSelection().isEmpty()) {
                    editRuntime();
                }
            }
        });
        fTable.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent event) {
                if (event.character == SWT.DEL && event.stateMask == 0) {
                    if (fRemoveButton.isEnabled()){
                        removeRuntimes();
                    }
                }
            }
        });

        Composite buttons = SWTFactory.createComposite(parent, font, 1, 1, GridData.VERTICAL_ALIGN_BEGINNING, 0, 0);

        fAddButton = SWTFactory.createPushButton(buttons, "Add...", null);
        fAddButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event evt) {
                addRuntime();
            }
        });

        fEditButton= SWTFactory.createPushButton(buttons, "Edit...", null);
        fEditButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event evt) {
                editRuntime();
            }
        });

        fRemoveButton= SWTFactory.createPushButton(buttons, "Remove", null);
        fRemoveButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event evt) {
                removeRuntimes();
            }
        });

        SWTFactory.createVerticalSpacer(parent, 1);

        enableButtons();
        fAddButton.setEnabled(true);
    }

    private void enableButtons() {
        IStructuredSelection selection = (IStructuredSelection) runtimesList.getSelection();
        int selectionCount= selection.size();
        fEditButton.setEnabled(selectionCount == 1);
        fRemoveButton.setEnabled(selectionCount > 0);
    }

    public Control getControl() {
        return fControl;
    }

    public void setRuntimes(IRuntime[] rts) {
        runtimes.clear();
        for (int i = 0; i < rts.length; i++) {
            runtimes.add(rts[i]);
        }
        if (!runtimesList.getTable().isDisposed())
        	runtimesList.setInput(runtimes);
        for (IRuntime runtime: rts) {
            if (runtime.isDefault()) {
                setDefaultRuntime(runtime);
                break;
            }
        }
        if (!runtimesList.getTable().isDisposed())
        	runtimesList.refresh();
    }

    public IRuntime[] getRuntimes() {
        IRuntime selected = getDefaultRuntime();
        for (IRuntime runtime: runtimes) {
            runtime.setDefault(runtime.equals(selected));
        }
        return runtimes.toArray(new IRuntime[runtimes.size()]);
    }

    private void addRuntime() {
        AbstractRuntimeDialog dialog = createEditingDialog(getShell(), runtimes);
        if (dialog.open() == Window.OK) {
            IRuntime result = dialog.getResult();
            if (result != null) {
                runtimeManager.recognizeJars(result);
                String version = result.getVersion();
                if (version==null || version.isEmpty()) {
                	MessageDialog.openError(getShell(), "Missing Version",
                			"Could not determine the version of Runtime "+result.getName()+
                			"\nPlease enter a valid version number.");
                	return;
                }
                runtimes.add(result);
                runtimesList.refresh();
                runtimesList.setSelection(new StructuredSelection(result));
            }
        }
    }

    private void editRuntime() {
        IStructuredSelection selection= (IStructuredSelection) runtimesList.getSelection();
        IRuntime runtime = (IRuntime) selection.getFirstElement();
        if (runtime == null) {
            return;
        }
        AbstractRuntimeDialog dialog = createEditingDialog(getShell(), runtimes);
        dialog.setRuntime(runtime);
        if (dialog.open() == Window.OK) {
            IRuntime result = dialog.getResult();
            if (result != null) {
            	// save the possibly updated version...
            	String newVersion = result.getVersion();
            	// ...because this will change it:
                runtimeManager.recognizeJars(result);
                if (newVersion!=null && !newVersion.isEmpty())
                	result.setVersion(newVersion);
                else {
                	newVersion = result.getVersion();
                }
                if (newVersion==null || newVersion.isEmpty()) {
                	MessageDialog.openError(getShell(), "Missing Version",
                			"Could not determine the version of Runtime "+result.getName()+
                			"\nPlease enter a valid version number.");
                	return;
                }
                // replace with the edited VM
                int index = runtimes.indexOf(runtime);
                runtimes.remove(index);
                runtimes.add(index, result);
                runtimesList.refresh();
                runtimesList.setSelection(new StructuredSelection(result));
            }
        }
    }

    private void removeRuntimes() {
        IStructuredSelection selection= (IStructuredSelection) runtimesList.getSelection();
        IRuntime[] runtimes = new IRuntime[selection.size()];
        Iterator<?> iter = selection.iterator();
        int i = 0;
        while (iter.hasNext()) {
            runtimes[i] = (IRuntime) iter.next();
            i++;
        }
        removeRuntimes(runtimes);
    }

    public void removeRuntimes(IRuntime[] rts) {
        IStructuredSelection prev = (IStructuredSelection) getSelection();
        for (int i = 0; i < rts.length; i++) {
            runtimes.remove(rts[i]);
        }
        runtimesList.refresh();
        IStructuredSelection curr = (IStructuredSelection) getSelection();
        if (!curr.equals(prev)) {
            rts = getRuntimes();
            if (curr.size() == 0 && rts.length == 1) {
                setSelection(new StructuredSelection(rts[0]));
            } else {
                fireSelectionChanged();
            }
        }
    }

    protected Shell getShell() {
        return getControl().getShell();
    }

    public void setDefaultRuntime(IRuntime runtime) {
        if (runtime == null) {
            setSelection(new StructuredSelection());
        } else {
            setSelection(new StructuredSelection(runtime));
        }
    }

    public IRuntime getDefaultRuntime() {
        Object[] objects = runtimesList.getCheckedElements();
        if (objects.length == 0) {
            return null;
        }
        return (IRuntime) objects[0];
    }

    abstract protected IRuntimeManager getRuntimeManager();
    abstract protected AbstractRuntimeDialog createEditingDialog(Shell shell, List<IRuntime> runtimes2);
}
