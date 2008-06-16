package org.drools.eclipse.flow.common.editor.editpart.work;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.flow.common.view.property.EditBeanDialog;
import org.drools.process.core.Work;
import org.drools.process.core.WorkDefinition;
import org.drools.process.core.WorkEditor;
import org.drools.process.core.impl.WorkImpl;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

/**
 * Custom Work editor for email work item.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class EmailCustomEditor extends EditBeanDialog implements WorkEditor {

    private List<Recipient> recipients = new ArrayList<Recipient>();
    private Text fromText;
    private Text subjectText;
    private Text bodyText;

    public EmailCustomEditor(Shell parentShell) {
        super(parentShell, "Custom Work Editor");
    }

    protected Control createDialogArea(Composite parent) {
        Work work = (Work) getValue();
        String from = (String) work.getParameter("From");
        String to = (String)work.getParameter("To");
        String body = (String)work.getParameter("Text");
        String subject = (String)work.getParameter("Subject");
        if (to != null) {
            for (String email: to.split(";")) {
                if (!"".equals(email)) {
                    Recipient recipient = new Recipient();
                    recipient.setEmail(email);
                    recipients.add(recipient);
                }
            }
        }
        
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new FillLayout());
        final TabFolder tabFolder = new TabFolder(composite, SWT.NONE);
        createHeadersTab(tabFolder);
        createBodyTab(tabFolder);
        
        if (from != null) {
            fromText.setText(from);
        }
        if (subject != null) {
            subjectText.setText(subject);
        }
        if (body != null) {
            bodyText.setText(body);
        }
        
        return composite;
    }

    public void createHeadersTab(TabFolder tabFolder) {
        final TabItem headersTabItem = new TabItem(tabFolder, SWT.NONE);
        headersTabItem.setText("Header");
        final Composite container = new Composite(tabFolder, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        gridLayout.horizontalSpacing = 2;
        container.setLayout(gridLayout);
        headersTabItem.setControl(container);

        final Label recipientsLabel = new Label(container, SWT.NONE);
        final GridData gd_recipientsLabel = new GridData();
        recipientsLabel.setLayoutData(gd_recipientsLabel);
        recipientsLabel.setText("Recipients");
        new Label(container, SWT.NONE);

        // create main table
        final TableViewer tableViewer = new TableViewer(container, SWT.BORDER
                | SWT.FULL_SELECTION);

//        // Type column
//        TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
//        column.setLabelProvider(new TypeLabelProvider());
//        column.setEditingSupport(new TypeEditing(tableViewer));
//        column.getColumn().setWidth(70);
//        column.getColumn().setText("Type");
//        column.getColumn().setMoveable(true);
//
//        // Display Name column
//        column = new TableViewerColumn(tableViewer, SWT.NONE);
//        column.setLabelProvider(new DisplayNameLabelProvider());
//        column.setEditingSupport(new DisplayNameEditing(tableViewer));
//        column.getColumn().setWidth(200);
//        column.getColumn().setText("Display Name");
//        column.getColumn().setMoveable(true);

        // Email column
        TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
        column.setLabelProvider(new EmailLabelProvider());
        column.setEditingSupport(new EmailEditing(tableViewer));
        column.getColumn().setText("Email Address");
        column.getColumn().setWidth(200);
        column.getColumn().setMoveable(true);

        final Table table = tableViewer.getTable();
        final GridData gd_table = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        gd_table.heightHint = 128;
        table.setLayoutData(gd_table);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);

        tableViewer.setContentProvider(new RecipientsContentProvider());
        tableViewer.setInput(recipients);
        
        // add/delete buttons
        final Composite composite = new Composite(container, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        composite.setLayout(new RowLayout());
        final Button addButton = new Button(composite, SWT.NONE);
        addButton.setText("Add");
        addButton.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                Recipient recipient = new Recipient();
                recipients.add(recipient);
                tableViewer.add(recipient);
                tableViewer.refresh();
            }
            public void widgetSelected(SelectionEvent e) {
                Recipient recipient = new Recipient();
                recipients.add(recipient);
                tableViewer.add(recipient);
                tableViewer.refresh();
            }
        });
        final Button deleteButton = new Button(composite, SWT.NONE);
        deleteButton.setText("Remove");
        deleteButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
                TableItem[] items = table.getSelection();
                if (items != null && items.length > 0) {
                    recipients.remove((Recipient) items[0].getData());
                    tableViewer.remove(items[0]);
                    tableViewer.refresh();
                }
            }
            public void widgetDefaultSelected(SelectionEvent event) {
                TableItem[] items = table.getSelection();
                if (items != null && items.length > 0) {
                    recipients.remove((Recipient) items[0].getData());
                    tableViewer.remove(items[0]);
                    tableViewer.refresh();
                }
            }
        });
        
        // from label and text field
        final Label fromLabel = new Label(container, SWT.NONE);
        fromLabel.setLayoutData(new GridData());
        fromLabel.setText("From");
        fromText = new Text(container, SWT.BORDER);
        fromText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    }

    public void createBodyTab(TabFolder tabFolder) {
        final TabItem headersTabItem = new TabItem(tabFolder, SWT.NONE);
        headersTabItem.setText("Body");

        final Composite container = new Composite(tabFolder, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.horizontalSpacing = 2;
        container.setLayout(gridLayout);
        headersTabItem.setControl(container);

        // subject label and text field
        final Label subjectLabel = new Label(container, SWT.NONE);
        subjectLabel.setLayoutData(new GridData());
        subjectLabel.setText("Subject");

        subjectText = new Text(container, SWT.BORDER);
        subjectText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        final Label bodyLabel = new Label(container, SWT.NONE);
        bodyLabel.setText("Body");

        bodyText = new Text(container, SWT.MULTI | SWT.BORDER);
        final GridData gd_bodyText = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_bodyText.heightHint = 175;
        bodyText.setLayoutData(gd_bodyText);
    }

    protected Object updateValue(Object value) {
        Work work = getWork();
        work.setName(((Work) value).getName());
        work.setParameter("From", fromText.getText());
        String to = "";
        for (int i = 0; i < recipients.size(); i++) {
            to += recipients.get(i).getEmail();
            if (i != recipients.size() - 1) {
                to += ";";
            }
        }
        work.setParameter("To", to);
        work.setParameter("Subject", subjectText.getText());
        work.setParameter("Text", bodyText.getText());
        return work;
    }

    public Work getWork() {
        return (Work) getValue();
    }

    public void setWork(Work work) {
        setValue(work);
    }

    public void setWorkDefinition(WorkDefinition workDefinition) {
        if (!"Email".equals(workDefinition.getName())) {
            DroolsEclipsePlugin.log(new IllegalArgumentException(
                "The emailCustomEditor can only handle email work items."));
        }
    }

    public void show() {
        open();
    }

    private class TypeLabelProvider extends ColumnLabelProvider {
        public String getText(Object element) {
            return ((Recipient) element).getType();
        }
    }

    private class TypeEditing extends EditingSupport {
        private ComboBoxCellEditor cellEditor;

        private String[] values = new String[] { "to", "cc", "bcc" };

        public TypeEditing(TableViewer viewer) {
            super(viewer);
            cellEditor = new ComboBoxCellEditor(viewer.getTable(), values);
        }

        protected boolean canEdit(Object element) {
            return true;
        }

        protected CellEditor getCellEditor(Object element) {
            return cellEditor;
        }

        protected Object getValue(Object element) {
            Recipient p = ((Recipient) element);
            return new Integer(Arrays.binarySearch(this.values, p.getType()));
        }

        protected void setValue(Object element, Object value) {
            int i = ((Integer) value).intValue();
            if (i != -1) {
                ((Recipient) element).setType(this.values[i]);
            }
            getViewer().update(element, null);
        }
    }

    private class DisplayNameLabelProvider extends ColumnLabelProvider {
        public String getText(Object element) {
            return ((Recipient) element).getDisplayName();
        }
    }

    private class DisplayNameEditing extends EditingSupport {
        private TextCellEditor cellEditor;

        public DisplayNameEditing(TableViewer viewer) {
            super(viewer);
            cellEditor = new TextCellEditor(viewer.getTable());
        }

        protected boolean canEdit(Object element) {
            return true;
        }

        protected CellEditor getCellEditor(Object element) {
            return cellEditor;
        }

        protected Object getValue(Object element) {
            return ((Recipient) element).getDisplayName();
        }

        protected void setValue(Object element, Object value) {
            ((Recipient) element).setDisplayName(value.toString());
            getViewer().update(element, null);
        }
    }

    private class EmailLabelProvider extends ColumnLabelProvider {
        public String getText(Object element) {
            return ((Recipient) element).getEmail();
        }
    }

    private class EmailEditing extends EditingSupport {
        private TextCellEditor cellEditor;

        public EmailEditing(TableViewer viewer) {
            super(viewer);
            cellEditor = new TextCellEditor(viewer.getTable());
        }

        protected boolean canEdit(Object element) {
            return true;
        }

        protected CellEditor getCellEditor(Object element) {
            return cellEditor;
        }

        protected Object getValue(Object element) {
            return ((Recipient) element).getEmail();
        }

        protected void setValue(Object element, Object value) {
            ((Recipient) element).setEmail(value.toString());
            getViewer().update(element, null);
        }
    }

    private class Recipient {
        
        private String type = "to";
        private String displayName = "";
        private String email = "john.doe@mail.com";

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getDisplayName() {
            return displayName;
        }

        public void setDisplayName(String displayName) {
            this.displayName = displayName;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    private class RecipientsContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            return recipients.toArray();
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
}
