package org.guvnor.tools.views;


import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.StringTokenizer;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;
import org.guvnor.tools.views.model.ResourceHistoryEntry;

public class ResourceHistoryView extends ViewPart {
	
	private TableViewer viewer;
	
	private ResourceHistoryEntry[] entries;
	
	/**
	 * The constructor.
	 */
	public ResourceHistoryView() {
		entries = new ResourceHistoryEntry[0];
	}
	
	public void createPartControl(Composite parent) {
		
		viewer = new TableViewer(createTable(parent));
		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		
		viewer.setContentProvider(new ResourceHistoryViewContentProvider());
		viewer.setLabelProvider(new ResourceHistoryLabelProvider());
		viewer.setInput(getViewSite());
	}
	
	private Table createTable(Composite parent) {
		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | 
					SWT.FULL_SELECTION | SWT.HIDE_SELECTION;

		Table table = new Table(parent, style);
		
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 3;
		table.setLayoutData(gridData);		
					
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		TableColumn column = new TableColumn(table, SWT.LEFT, 0);
		column.setResizable(true);
		column.setText("Revision");
		column.setWidth(100);
		
		column = new TableColumn(table, SWT.LEFT, 1);
		column.setResizable(true);
		column.setText("Date");
		column.setWidth(175);

		column = new TableColumn(table, SWT.LEFT, 2);
		column.setResizable(true);
		column.setText("Author");
		column.setWidth(200);
 
		column = new TableColumn(table, SWT.CENTER, 3);
		column.setResizable(true);
		column.setText("Comment");
		column.setWidth(350);
		
		return table;
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	public void setEntries(Properties entryProps) {
		entries = new ResourceHistoryEntry[entryProps.size()];
		Enumeration<Object> en = entryProps.keys();
		int i = 0;
		while (en.hasMoreElements()) {
			String oneRevision = (String)en.nextElement();
			String val = entryProps.getProperty(oneRevision);
			StringTokenizer tokens = new StringTokenizer(val, ",");
			String verDate = null;
			String author = null;
			String comment = null;
			try {
				verDate = tokens.nextToken();
				author = tokens.nextToken();
				comment = tokens.nextToken();
			} catch (NoSuchElementException e) {
				// Don't care if some fields are missing
			}
			entries[i] = new ResourceHistoryEntry(oneRevision, verDate, author, comment);
			i++;
		}
		viewer.setContentProvider(new ResourceHistoryViewContentProvider());
		viewer.setInput(getViewSite());
	}
	
	class ResourceHistoryViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}
		public void dispose() {
		}
		public Object[] getElements(Object parent) {
			return entries;
		}
	}
}