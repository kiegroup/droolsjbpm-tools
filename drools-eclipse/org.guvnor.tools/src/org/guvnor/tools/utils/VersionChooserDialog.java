package org.guvnor.tools.utils;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.guvnor.tools.Activator;
import org.guvnor.tools.views.ResourceHistoryContentProvider;
import org.guvnor.tools.views.ResourceHistoryLabelProvider;
import org.guvnor.tools.views.model.ResourceHistoryEntry;

public class VersionChooserDialog extends TitleAreaDialog {
	
	private static final int INITIAL_WIDTH = 790;
	private static final int INITIAL_HEIGHT = 350;
	
	private String fileName;
	private ResourceHistoryEntry[] entries;
	
	private ResourceHistoryEntry selectedEntry;
	
	private TableViewer viewer;
	
	public VersionChooserDialog(Shell parentShell, String fileName, ResourceHistoryEntry[] entries) {
		super(parentShell);
		super.setShellStyle(getShellStyle() | SWT.RESIZE);
		this.entries = entries;
		this.fileName = fileName;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		super.setTitle("Resource Versions");
		super.setMessage("Choose a version for " + fileName);
		super.setTitleImage(Activator.getImageDescriptor(Activator.IMG_GUVREPWIZBAN).createImage());
		
		viewer = new TableViewer(PlatformUtils.createResourceHistoryTable(parent));
		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		
		viewer.setContentProvider(new ResourceHistoryContentProvider(entries));
		viewer.setLabelProvider(new ResourceHistoryLabelProvider());
		viewer.setInput(this);
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				if (viewer.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection)viewer.getSelection();
					if (sel.getFirstElement() instanceof ResourceHistoryEntry) {
						selectedEntry = (ResourceHistoryEntry)sel.getFirstElement();
					}
				}
			}
		});
		
		if (entries.length > 0) {
			viewer.getTable().setSelection(0);
			selectedEntry = entries[0];
		}
		
		return super.createDialogArea(parent);
	}
	
	@Override
	protected Point getInitialSize() {
		// Try to set a reasonable default size.
		return new Point(INITIAL_WIDTH, INITIAL_HEIGHT);
	}
	
	public ResourceHistoryEntry getSelectedEntry() {
		return selectedEntry;
	}
}
