package org.drools.eclipse.preferences;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.drools.eclipse.util.DroolsRuntime;
import org.drools.eclipse.util.DroolsRuntimeManager;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jdt.internal.debug.ui.SWTFactory;
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

public class DroolsRuntimesBlock implements ISelectionProvider {

	private Composite fControl;
	private List<DroolsRuntime> droolsRuntimes = new ArrayList<DroolsRuntime>();
	private CheckboxTableViewer droolsRuntimesList;
	private Button fAddButton;
	private Button fRemoveButton;
	private Button fEditButton;
	private ListenerList fSelectionListeners = new ListenerList();
	private ISelection fPrevSelection = new StructuredSelection();
    private Table fTable;
	
	class DroolsRuntimesContentProvider implements IStructuredContentProvider {		
		public Object[] getElements(Object input) {
			return droolsRuntimes.toArray();
		}
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
	}
	
	class DroolsRuntimesLabelProvider extends LabelProvider implements ITableLabelProvider {
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof DroolsRuntime) {
				DroolsRuntime runtime = (DroolsRuntime) element;
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
		return new StructuredSelection(droolsRuntimesList.getCheckedElements());
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
					droolsRuntimesList.setCheckedElements(new Object[0]);
				} else {
					droolsRuntimesList.setCheckedElements(new Object[]{runtime});
					droolsRuntimesList.reveal(runtime);
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
				
		SWTFactory.createLabel(parent, "Installed Drools Runtimes", 2);
				
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
		
		droolsRuntimesList = new CheckboxTableViewer(fTable);			
		droolsRuntimesList.setLabelProvider(new DroolsRuntimesLabelProvider());
		droolsRuntimesList.setContentProvider(new DroolsRuntimesContentProvider());
		
		droolsRuntimesList.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent evt) {
				enableButtons();
			}
		});
		
		droolsRuntimesList.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent event) {
				if (event.getChecked()) {
					setDefaultDroolsRuntime((DroolsRuntime) event.getElement());
				} else {
					setDefaultDroolsRuntime(null);
				}
			}
		});
		
		droolsRuntimesList.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent e) {
				if (!droolsRuntimesList.getSelection().isEmpty()) {
					editDroolsRuntime();
				}
			}
		});
		fTable.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent event) {
				if (event.character == SWT.DEL && event.stateMask == 0) {
					if (fRemoveButton.isEnabled()){
						removeDroolsRuntimes();
					}
				}
			}
		});	
		
		Composite buttons = SWTFactory.createComposite(parent, font, 1, 1, GridData.VERTICAL_ALIGN_BEGINNING, 0, 0);
		
		fAddButton = SWTFactory.createPushButton(buttons, "Add...", null); 
		fAddButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event evt) {
				addDroolsRuntime();
			}
		});
		
		fEditButton= SWTFactory.createPushButton(buttons, "Edit...", null); 
		fEditButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event evt) {
				editDroolsRuntime();
			}
		});
		
		fRemoveButton= SWTFactory.createPushButton(buttons, "Remove", null); 
		fRemoveButton.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event evt) {
				removeDroolsRuntimes();
			}
		});
		
		SWTFactory.createVerticalSpacer(parent, 1);
		
		enableButtons();
		fAddButton.setEnabled(true);
	}
	
	private void enableButtons() {
		IStructuredSelection selection = (IStructuredSelection) droolsRuntimesList.getSelection();
		int selectionCount= selection.size();
		fEditButton.setEnabled(selectionCount == 1);
		if (selectionCount > 0 && selectionCount < droolsRuntimesList.getTable().getItemCount()) {
			fRemoveButton.setEnabled(true);
		} else {
			fRemoveButton.setEnabled(false);
		}
	}
	
	public Control getControl() {
		return fControl;
	}

	public void setDroolsRuntimes(DroolsRuntime[] runtimes) {
		droolsRuntimes.clear();
		for (int i = 0; i < runtimes.length; i++) {
			droolsRuntimes.add(runtimes[i]);
		}
		droolsRuntimesList.setInput(droolsRuntimes);
		droolsRuntimesList.refresh();
	}

	public DroolsRuntime[] getDroolsRuntimes() {
		DroolsRuntime selected = getDefaultDroolsRuntime();
		for (DroolsRuntime runtime: droolsRuntimes) {
			runtime.setDefault(runtime.equals(selected));
		}
		return droolsRuntimes.toArray(new DroolsRuntime[droolsRuntimes.size()]);
	}
	
	private void addDroolsRuntime() {
		DroolsRuntimeDialog dialog = new DroolsRuntimeDialog(getShell());
		if (dialog.open() == Window.OK) {
			DroolsRuntime result = dialog.getResult();
			if (result != null) {
				DroolsRuntimeManager.recognizeJars(result);
				droolsRuntimes.add(result);
				droolsRuntimesList.refresh();
				droolsRuntimesList.setSelection(new StructuredSelection(result));
			}
		}
	}
	
	private void editDroolsRuntime() {
		IStructuredSelection selection= (IStructuredSelection) droolsRuntimesList.getSelection();
		DroolsRuntime runtime = (DroolsRuntime) selection.getFirstElement();
		if (runtime == null) {
			return;
		}
		DroolsRuntimeDialog dialog = new DroolsRuntimeDialog(getShell());
		dialog.setDroolsRuntime(runtime);
		if (dialog.open() == Window.OK) {
			DroolsRuntime result = dialog.getResult();
			if (result != null) {
				DroolsRuntimeManager.recognizeJars(result);
				// replace with the edited VM
				int index = droolsRuntimes.indexOf(runtime);
				droolsRuntimes.remove(index);
				droolsRuntimes.add(index, result);
				droolsRuntimesList.refresh();
				droolsRuntimesList.setSelection(new StructuredSelection(result));
			}
		}
	}
	
	private void removeDroolsRuntimes() {
		IStructuredSelection selection= (IStructuredSelection) droolsRuntimesList.getSelection();
		DroolsRuntime[] runtimes = new DroolsRuntime[selection.size()];
		Iterator<?> iter = selection.iterator();
		int i = 0;
		while (iter.hasNext()) {
			runtimes[i] = (DroolsRuntime) iter.next();
			i++;
		}
		removeDroolsRuntimes(runtimes);
	}	
	
	public void removeDroolsRuntimes(DroolsRuntime[] runtimes) {
		IStructuredSelection prev = (IStructuredSelection) getSelection();
		for (int i = 0; i < runtimes.length; i++) {
			droolsRuntimes.remove(runtimes[i]);
		}
		droolsRuntimesList.refresh();
		IStructuredSelection curr = (IStructuredSelection) getSelection();
		if (!curr.equals(prev)) {
			runtimes = getDroolsRuntimes();
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

	public void setDefaultDroolsRuntime(DroolsRuntime runtime) {
		if (runtime == null) {
			setSelection(new StructuredSelection());
		} else {
			setSelection(new StructuredSelection(runtime));
		}
	}
	
	public DroolsRuntime getDefaultDroolsRuntime() {
		Object[] objects = droolsRuntimesList.getCheckedElements();
		if (objects.length == 0) {
			return null;
		}
		return (DroolsRuntime) objects[0];
	}

}
