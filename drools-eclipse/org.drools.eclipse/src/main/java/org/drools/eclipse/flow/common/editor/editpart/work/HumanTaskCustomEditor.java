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

package org.drools.eclipse.flow.common.editor.editpart.work;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
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
 * Custom Work editor for human tasks.
 */
public class HumanTaskCustomEditor extends EditBeanDialog<Work> implements WorkEditor {
	
	private static final String COMPONENT_SEPARATOR = "^";
	private static final String COMPONENT_SEPARATOR_ESCAPED = "\\^";
	private static final String ELEMENT_SEPARATOR = "@";
	private static final String ATTRIBUTES_SEPARATOR = "|";
	private static final String ATTRIBUTES_SEPARATOR_ESCAPED = "\\|";
	private static final String KEY_VALUE_SEPARATOR = ":";
	
	private static final String[] KNOWN_KEYS = {"users", "groups", "from", "tousers", "togroups", "replyto", "subject","body"};

    private Text nameText;
    private Text actorText;
    private Text groupText;
    private Text commentText;
    private Text priorityText;
    private Button skippableButton;
    private Text contentText;
    private Text localeText;
    
    private List<Reassignment> reassignments = new ArrayList<Reassignment>();
    private List<Notification> notifications = new ArrayList<Notification>();
    
    private Text notifyFromText;
    private Text notifyToText;
    private Text notifyToGroupsText;
    private Text notifyReplyToText;
    private Text notifySubjectText;
    private Text notifyBodyText;
    private Combo notifyTypeText;
    private Text notifyExpiresAtText;

    public HumanTaskCustomEditor(Shell parentShell) {
        super(parentShell, "Human Task Editor");
        setBlockOnOpen(true);
    }
    
    protected Point getInitialSize() {
        return new Point(460, 660);
    }
    
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        
        composite.setLayout(gridLayout);
        
        final TabFolder tabFolder = new TabFolder(composite, SWT.NONE);
        
        createGeneralTab(tabFolder);
        createReassignmentTab(tabFolder);
        createNotificationTab(tabFolder);
        
        
        return composite;
    }
    
    private void createGeneralTab(TabFolder tabFolder) {
    	final TabItem headersTabItem = new TabItem(tabFolder, SWT.NONE);
        headersTabItem.setText("General");

        final Composite container = new Composite(tabFolder, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.horizontalSpacing = 2;
        container.setLayout(gridLayout);
        headersTabItem.setControl(container);
        
        Work work = (Work) getValue();
        
        Label nameLabel = new Label(container, SWT.NONE);
        nameLabel.setText("Name: ");
        nameText = new Text(container, SWT.NONE);
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        nameText.setLayoutData(gridData);
        String name = (String) work.getParameter("TaskName");
        nameText.setText(name == null ? "" : name);
        
		Label label = new Label(container, SWT.NONE);
		label.setText("Actor(s): ");
		actorText = new Text(container, SWT.NONE);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		actorText.setLayoutData(gridData);
		String value = (String) work.getParameter("ActorId");
		actorText.setText(value == null ? "" : value);

		label = new Label(container, SWT.NONE);
		label.setText("Group(s): ");
		groupText = new Text(container, SWT.NONE);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		groupText.setLayoutData(gridData);
		value = (String) work.getParameter("GroupId");
		groupText.setText(value == null ? "" : value);

		label = new Label(container, SWT.NONE);
		label.setText("Comment: ");
		commentText = new Text(container, SWT.MULTI);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		commentText.setLayoutData(gridData);
		value = (String) work.getParameter("Comment");
		commentText.setText(value == null ? "" : value.toString());

		label = new Label(container, SWT.NONE);
		label.setText("Priority: ");
		priorityText = new Text(container, SWT.NONE);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		priorityText.setLayoutData(gridData);
		value = (String) work.getParameter("Priority");
		priorityText.setText(value == null ? "" : value);

		skippableButton = new Button(container, SWT.CHECK | SWT.LEFT);
		skippableButton.setText("Skippable");
		value = (String) work.getParameter("Skippable");
		skippableButton.setSelection("true".equals(value));
		gridData = new GridData();
		gridData.horizontalSpan = 2;
		skippableButton.setLayoutData(gridData);

		label = new Label(container, SWT.NONE);
		label.setText("Content: ");
		contentText = new Text(container, SWT.MULTI);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		gridData.verticalAlignment = GridData.FILL;
		contentText.setLayoutData(gridData);
		value = (String) work.getParameter("Content");
		contentText.setText(value == null ? "" : value.toString());
		
		label = new Label(container, SWT.NONE);
		label.setText("Locale: ");
		localeText = new Text(container, SWT.NONE);
		gridData = new GridData();
		gridData.grabExcessHorizontalSpace = true;
		gridData.horizontalAlignment = GridData.FILL;
		localeText.setLayoutData(gridData);
		value = (String) work.getParameter("Locale");
		localeText.setText(value == null ? "en-UK" : value.toString());
	}

	private void createReassignmentTab(TabFolder tabFolder) {
		Work work = (Work) getValue();
		
		String notStartedReassign = (String) work.getParameter("NotStartedReassign");
		String notCompletedReassign = (String) work.getParameter("NotCompletedReassign");
		
		if (notStartedReassign != null) {
			String[] reassigns = notStartedReassign.split(COMPONENT_SEPARATOR_ESCAPED);
			
			for (String reassign : reassigns) {
				if (reassign!= null && reassign.length() > 0) {
					reassignments.add(new Reassignment(reassign, "not-started"));
				}
			}
		}
		
		if (notCompletedReassign != null) {
			String[] reassigns = notCompletedReassign.split(COMPONENT_SEPARATOR_ESCAPED);
			
			for (String reassign : reassigns) {
				if (reassign!= null && reassign.length() > 0) {
					reassignments.add(new Reassignment(reassign, "not-completed"));
				}
			}
		}
		
		final TabItem headersTabItem = new TabItem(tabFolder, SWT.NONE);
        headersTabItem.setText("Reassignment");

        final Composite container = new Composite(tabFolder, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.horizontalSpacing = 2;
        container.setLayout(gridLayout);
        headersTabItem.setControl(container);
        
        final TableViewer tableViewer = new TableViewer(container, SWT.BORDER
                | SWT.FULL_SELECTION);
        
        TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
        column.setLabelProvider(new ReassignmentUsersLabelProvider());
        column.setEditingSupport(new ReassignmentUsersEditing(tableViewer));
        column.getColumn().setText("Users");
        column.getColumn().setWidth(100);
        column.getColumn().setMoveable(true);
        
        TableViewerColumn column2 = new TableViewerColumn(tableViewer, SWT.NONE);
        column2.setLabelProvider(new ReassignmentGroupsLabelProvider());
        column2.setEditingSupport(new ReassignmentGroupsEditing(tableViewer));
        column2.getColumn().setText("Groups");
        column2.getColumn().setWidth(100);
        column2.getColumn().setMoveable(true);
        
        TableViewerColumn column3 = new TableViewerColumn(tableViewer, SWT.NONE);
        column3.setLabelProvider(new ReassignmentExpiresAtLabelProvider());
        column3.setEditingSupport(new ReassignmentExpiresAtEditing(tableViewer));
        column3.getColumn().setText("Expires At");
        column3.getColumn().setWidth(100);
        column3.getColumn().setMoveable(true);
        
        TableViewerColumn column4 = new TableViewerColumn(tableViewer, SWT.NONE);
        column4.setLabelProvider(new ReassignmentTypeLabelProvider());
        column4.setEditingSupport(new ReassignmentTypeEditing(tableViewer));
        column4.getColumn().setText("Type");
        column4.getColumn().setWidth(100);
        column4.getColumn().setMoveable(true);
        
        final Table table = tableViewer.getTable();
        final GridData gd_table = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        gd_table.heightHint = 128;
        table.setLayoutData(gd_table);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);

        tableViewer.setContentProvider(new ReassignmentContentProvider());
        tableViewer.setInput(reassignments);

        // add/delete buttons
        final Composite composite = new Composite(container, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        composite.setLayout(new RowLayout());
        final Button addButton = new Button(composite, SWT.NONE);
        addButton.setText("Add");
        addButton.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                Reassignment reassignment = new Reassignment();
                reassignments.add(reassignment);
                tableViewer.add(reassignment);
                tableViewer.refresh();
            }
            public void widgetSelected(SelectionEvent e) {
            	Reassignment reassignment = new Reassignment();
                reassignments.add(reassignment);
                tableViewer.add(reassignment);
                tableViewer.refresh();
            }
        });
        final Button deleteButton = new Button(composite, SWT.NONE);
        deleteButton.setText("Remove");
        deleteButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
                TableItem[] items = table.getSelection();
                if (items != null && items.length > 0) {
                	reassignments.remove((Reassignment) items[0].getData());
                    tableViewer.remove(items[0]);
                    tableViewer.refresh();
                }
            }
            public void widgetDefaultSelected(SelectionEvent event) {
                TableItem[] items = table.getSelection();
                if (items != null && items.length > 0) {
                	reassignments.remove((Reassignment) items[0].getData());
                    tableViewer.remove(items[0]);
                    tableViewer.refresh();
                }
            }
        });
	}
	
	private void createNotificationTab(TabFolder tabFolder) {
		Work work = (Work) getValue();
		
		String notStartedNotify = (String) work.getParameter("NotStartedNotify");
		String notCompletedNotify = (String) work.getParameter("NotCompletedNotify");
		
		if (notStartedNotify != null) {
			String[] notifies = notStartedNotify.split(COMPONENT_SEPARATOR_ESCAPED);
			
			for (String notification : notifies) {
				if (notification!= null && notification.length() > 0) {
					notifications.add(new Notification(notification, "not-started"));
				}
			}
		}
		
		if (notCompletedNotify != null) {
			String[] notifies = notCompletedNotify.split(COMPONENT_SEPARATOR_ESCAPED);
			
			for (String notification : notifies) {
				if (notification!= null && notification.length() > 0) {
					notifications.add(new Notification(notification, "not-completed"));
				}
			}
		}
		
		final TabItem headersTabItem = new TabItem(tabFolder, SWT.NONE);
        headersTabItem.setText("Notifications");

        final Composite container = new Composite(tabFolder, SWT.NONE);
        final GridLayout gridLayout = new GridLayout();
        gridLayout.horizontalSpacing = 2;
        container.setLayout(gridLayout);
        headersTabItem.setControl(container);
        
        final TableViewer tableViewer = new TableViewer(container, SWT.BORDER
                | SWT.FULL_SELECTION);
        
        TableViewerColumn column = new TableViewerColumn(tableViewer, SWT.NONE);
        column.setLabelProvider(new NotificationsLabelProvider());
        column.setEditingSupport(new NotificationsEditing(tableViewer));
        column.getColumn().setText("Notifications");
        column.getColumn().setWidth(400);
        column.getColumn().setMoveable(true);

        final Label typeLabel = new Label(container, SWT.NONE);
        typeLabel.setLayoutData(new GridData());
        typeLabel.setText("Type");

        notifyTypeText = new Combo(container, SWT.NONE);
        notifyTypeText.add("not-started");
        notifyTypeText.add("not-completed");
        notifyTypeText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        final Label expiresAtLabel = new Label(container, SWT.NONE);
        expiresAtLabel.setLayoutData(new GridData());
        expiresAtLabel.setText("ExpiresAt");

        notifyExpiresAtText = new Text(container, SWT.NONE);
        notifyExpiresAtText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        final Label notifyFromLabel = new Label(container, SWT.NONE);
        notifyFromLabel.setLayoutData(new GridData());
        notifyFromLabel.setText("From");

        notifyFromText = new Text(container, SWT.NONE);
        notifyFromText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        final Label notifyToLabel = new Label(container, SWT.NONE);
        notifyToLabel.setText("To Users");

        notifyToText = new Text(container, SWT.NONE);
        notifyToText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        final Label notifyToGroupsLabel = new Label(container, SWT.NONE);
        notifyToGroupsLabel.setText("To Groups");

        notifyToGroupsText = new Text(container, SWT.NONE);
        notifyToGroupsText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        
        final Label notifyReplyToLabel = new Label(container, SWT.NONE);
        notifyReplyToLabel.setText("Reply To");
        
        notifyReplyToText = new Text(container, SWT.NONE);
        notifyReplyToText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        final Label subjectLabel = new Label(container, SWT.NONE);
        subjectLabel.setLayoutData(new GridData());
        subjectLabel.setText("Subject");

        notifySubjectText = new Text(container, SWT.NONE);
        notifySubjectText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        final Label bodyLabel = new Label(container, SWT.NONE);
        bodyLabel.setText("Body");

        notifyBodyText = new Text(container, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL);
        final GridData gd_bodyText = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gd_bodyText.heightHint = 100;
        notifyBodyText.setLayoutData(gd_bodyText);
        
        final Table table = tableViewer.getTable();
        final GridData gd_table = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
        gd_table.heightHint = 100;
        table.setLayoutData(gd_table);
        table.setLinesVisible(true);
        table.setHeaderVisible(true);
        
        table.addSelectionListener(new SelectionListener() {
			
			public void widgetSelected(SelectionEvent e) {
				TableItem[] items = table.getSelection();
                if (items != null && items.length > 0) {
                	int index = notifications.indexOf((Notification) items[0].getData());
                	Notification notification = notifications.get(index);
                	
                    notifyFromText.setText(notification.getFrom());
                    notifyToText.setText(notification.getTo());
                    notifyToGroupsText.setText(notification.getToGroups());
                    notifyReplyToText.setText(notification.getReplyTo());
                    notifySubjectText.setText(notification.getSubject());
                    notifyBodyText.setText(notification.getBody());
                    notifyTypeText.setText(notification.getType());
                    notifyExpiresAtText.setText(notification.getExpiresAt());
                } else {
                	notifyFromText.setText("");
                    notifyToText.setText("");
                    notifyToGroupsText.setText("");
                    notifyReplyToText.setText("");
                    notifySubjectText.setText("");
                    notifyBodyText.setText("");
                    notifyTypeText.setText("");
                    notifyExpiresAtText.setText("");
                }
				
			}
			
			public void widgetDefaultSelected(SelectionEvent e) {
				TableItem[] items = table.getSelection();
                if (items != null && items.length > 0) {
                	int index = notifications.indexOf((Notification) items[0].getData());
                	Notification notification = notifications.get(index);
                	
                    notifyFromText.setText(notification.getFrom());
                    notifyToText.setText(notification.getTo());
                    notifyToGroupsText.setText(notification.getToGroups());
                    notifyReplyToText.setText(notification.getReplyTo());
                    notifySubjectText.setText(notification.getSubject());
                    notifyBodyText.setText(notification.getBody());
                    notifyTypeText.setText(notification.getType());
                    notifyExpiresAtText.setText(notification.getExpiresAt());
                } else {
                	notifyFromText.setText("");
                    notifyToText.setText("");
                    notifyToGroupsText.setText("");
                    notifyReplyToText.setText("");
                    notifySubjectText.setText("");
                    notifyBodyText.setText("");
                    notifyTypeText.setText("");
                    notifyExpiresAtText.setText("");
                }
			}
		});

        tableViewer.setContentProvider(new NotificationsContentProvider());
        tableViewer.setInput(notifications);
        // add/delete buttons
        final Composite composite = new Composite(container, SWT.NONE);
        composite.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 2, 1));
        composite.setLayout(new RowLayout());
        final Button addButton = new Button(composite, SWT.NONE);
        addButton.setText("Add");
        addButton.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
                Notification notification = new Notification();
                notification.setFrom(notifyFromText.getText());
                notification.setTo(notifyToText.getText());
                notification.setToGroups(notifyToGroupsText.getText());
                notification.setReplyTo(notifyReplyToText.getText());
                notification.setSubject(notifySubjectText.getText());
                notification.setBody(notifyBodyText.getText());
                notification.setType(notifyTypeText.getText());
                notification.setExpiresAt(notifyExpiresAtText.getText());
                notifications.add(notification);
                tableViewer.add(notification);
                tableViewer.refresh();
                // clear fields after add operation
                notifyFromText.setText("");
                notifyToText.setText("");
                notifyToGroupsText.setText("");
                notifyReplyToText.setText("");
                notifySubjectText.setText("");
                notifyBodyText.setText("");
                notifyTypeText.setText("");
                notifyExpiresAtText.setText("");
                
            }
            public void widgetSelected(SelectionEvent e) {
            	Notification notification = new Notification();
                notification.setFrom(notifyFromText.getText());
                notification.setTo(notifyToText.getText());
                notification.setToGroups(notifyToGroupsText.getText());
                notification.setReplyTo(notifyReplyToText.getText());
                notification.setSubject(notifySubjectText.getText());
                notification.setBody(notifyBodyText.getText());
                notification.setType(notifyTypeText.getText());
                notification.setExpiresAt(notifyExpiresAtText.getText());
                notifications.add(notification);
                tableViewer.add(notification);
                tableViewer.refresh();
                // clear fields after add operation
                notifyFromText.setText("");
                notifyToText.setText("");
                notifyToGroupsText.setText("");
                notifyReplyToText.setText("");
                notifySubjectText.setText("");
                notifyBodyText.setText("");
                notifyTypeText.setText("");
                notifyExpiresAtText.setText("");
            }
        });
        final Button deleteButton = new Button(composite, SWT.NONE);
        deleteButton.setText("Remove");
        deleteButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
                TableItem[] items = table.getSelection();
                if (items != null && items.length > 0) {
                	notifications.remove((Notification) items[0].getData());
                    tableViewer.remove(items[0]);
                    tableViewer.refresh();
                }
            }
            public void widgetDefaultSelected(SelectionEvent event) {
                TableItem[] items = table.getSelection();
                if (items != null && items.length > 0) {
                	notifications.remove((Notification) items[0].getData());
                    tableViewer.remove(items[0]);
                    tableViewer.refresh();
                }
            }
        });
        
        final Button updateButton = new Button(composite, SWT.NONE);
        updateButton.setText("Update");
        updateButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
                TableItem[] items = table.getSelection();
                if (items != null && items.length > 0) {
                	int index = notifications.indexOf((Notification) items[0].getData());
                	Notification notification = notifications.get(index);
                	notification.setFrom(notifyFromText.getText());
                    notification.setTo(notifyToText.getText());
                    notification.setToGroups(notifyToGroupsText.getText());
                    notification.setReplyTo(notifyReplyToText.getText());
                    notification.setSubject(notifySubjectText.getText());
                    notification.setBody(notifyBodyText.getText());
                    notification.setType(notifyTypeText.getText());
                    notification.setExpiresAt(notifyExpiresAtText.getText());
                    tableViewer.refresh();
                    // clear fields after add operation
                    notifyFromText.setText("");
                    notifyToText.setText("");
                    notifyToGroupsText.setText("");
                    notifyReplyToText.setText("");
                    notifySubjectText.setText("");
                    notifyBodyText.setText("");
                    notifyTypeText.setText("");
                    notifyExpiresAtText.setText("");
                }
            }
            public void widgetDefaultSelected(SelectionEvent event) {
                TableItem[] items = table.getSelection();
                if (items != null && items.length > 0) {
                	int index = notifications.indexOf((Notification) items[0].getData());
                	Notification notification = notifications.get(index);
                	notification.setFrom(notifyFromText.getText());
                    notification.setTo(notifyToText.getText());
                    notification.setToGroups(notifyToGroupsText.getText());
                    notification.setReplyTo(notifyReplyToText.getText());
                    notification.setSubject(notifySubjectText.getText());
                    notification.setBody(notifyBodyText.getText());
                    notification.setType(notifyTypeText.getText());
                    notification.setExpiresAt(notifyExpiresAtText.getText());
                    tableViewer.refresh();
                    // clear fields after add operation
                    notifyFromText.setText("");
                    notifyToText.setText("");
                    notifyToGroupsText.setText("");
                    notifyReplyToText.setText("");
                    notifySubjectText.setText("");
                    notifyBodyText.setText("");
                    notifyTypeText.setText("");
                    notifyExpiresAtText.setText("");
                }
            }
        });
        
        final Button clearButton = new Button(composite, SWT.NONE);
        clearButton.setText("Clear");
        clearButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent event) {
            	// clear fields after add operation
                notifyFromText.setText("");
                notifyToText.setText("");
                notifyToGroupsText.setText("");
                notifyReplyToText.setText("");
                notifySubjectText.setText("");
                notifyBodyText.setText("");
                notifyTypeText.setText("");
                notifyExpiresAtText.setText("");
            }
            public void widgetDefaultSelected(SelectionEvent event) {
            	// clear fields after add operation
                notifyFromText.setText("");
                notifyToText.setText("");
                notifyToGroupsText.setText("");
                notifyReplyToText.setText("");
                notifySubjectText.setText("");
                notifyBodyText.setText("");
                notifyTypeText.setText("");
                notifyExpiresAtText.setText("");
            }
        });
		
	}

	protected Work updateValue(Work value) {
        Work work = new WorkImpl();
        work.setName("Human Task");
        work.setParameter("TaskName", nameText.getText());
        work.setParameter("ActorId", actorText.getText());
        work.setParameter("GroupId", groupText.getText());
        work.setParameter("Comment", commentText.getText());
        work.setParameter("Priority", priorityText.getText());
        work.setParameter("Skippable", skippableButton.getSelection() + "");
        String content = contentText.getText();
        work.setParameter("Content", content.trim().length() == 0 ? null : content);
        work.setParameter("Locale", localeText.getText());
        
        // process reassignment
        if (!reassignments.isEmpty()) {
        	StringBuffer notStartedReassignments = new StringBuffer();
        	StringBuffer notCompletedReassignments = new StringBuffer();
        	for (Reassignment reassign : reassignments) {
        		if ("not-started".equalsIgnoreCase(reassign.getTypeAsString())) {
        			if (notStartedReassignments.length() > 0) {
        				notStartedReassignments.append(COMPONENT_SEPARATOR);
        			}
        			notStartedReassignments.append(reassign.toDataInput());
        		} else if ("not-completed".equalsIgnoreCase(reassign.getTypeAsString())) {
        			if (notCompletedReassignments.length() > 0) {
        				notCompletedReassignments.append(COMPONENT_SEPARATOR);
        			}
        			notCompletedReassignments.append(reassign.toDataInput());
        		}
        	}
        	if (notStartedReassignments.length() > 0) {
        		work.setParameter("NotStartedReassign", notStartedReassignments.toString());
        	}
        	if (notCompletedReassignments.length() > 0) {
        		work.setParameter("NotCompletedReassign", notCompletedReassignments.toString());
        	}
        }
        
        // process notifications
        if (!notifications.isEmpty()) {
        	StringBuffer notStartedNotifications = new StringBuffer();
        	StringBuffer notCompletedNotifications = new StringBuffer();
        	for (Notification notification : notifications) {
        		if ("not-started".equalsIgnoreCase(notification.getType())) {
        			if (notStartedNotifications.length() > 0) {
        				notStartedNotifications.append(COMPONENT_SEPARATOR);
        			}
        			notStartedNotifications.append(notification.toDataInput());
        		} else if ("not-completed".equalsIgnoreCase(notification.getType())) {
        			if (notCompletedNotifications.length() > 0) {
        				notCompletedNotifications.append(COMPONENT_SEPARATOR);
        			}
        			notCompletedNotifications.append(notification.toDataInput());
        		}
        	}
        	if (notStartedNotifications.length() > 0) {
        		work.setParameter("NotStartedNotify", notStartedNotifications.toString());
        	}
        	if (notCompletedNotifications.length() > 0) {
        		work.setParameter("NotCompletedNotify", notCompletedNotifications.toString());
        	}
        }
        
        
        work.setParameterDefinitions(((Work) value).getParameterDefinitions());
        return work;
    }
        
    public Work getWork() {
        return (Work) getValue();
    }

    public void setWork(Work work) {
        setValue(work);
    }

    public void setWorkDefinition(WorkDefinition workDefinition) {
    }

    public boolean show() {
        int result = open();
        return result == OK;
    }
    
    private class ReassignmentContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            return reassignments.toArray();
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }

    private class ReassignmentUsersLabelProvider extends ColumnLabelProvider {
        public String getText(Object element) {
            return ((Reassignment) element).getUsers();
        }
    }
    
    private class ReassignmentGroupsLabelProvider extends ColumnLabelProvider {
        public String getText(Object element) {
            return ((Reassignment) element).getGroups();
        }
    }
    
    private class ReassignmentExpiresAtLabelProvider extends ColumnLabelProvider {
        public String getText(Object element) {
            return ((Reassignment) element).getExpiresAt();
        }
    }
    
    private class ReassignmentTypeLabelProvider extends ColumnLabelProvider {
        public String getText(Object element) {
            return ((Reassignment) element).getTypeAsString();
        }
    }

    private class ReassignmentUsersEditing extends EditingSupport {
        private TextCellEditor cellEditor;

        public ReassignmentUsersEditing(TableViewer viewer) {
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
            return ((Reassignment) element).getUsers();
        }

        protected void setValue(Object element, Object value) {
            ((Reassignment) element).setUsers(value.toString());
            getViewer().update(element, null);
        }
    }
    
    private class ReassignmentGroupsEditing extends EditingSupport {
        private TextCellEditor cellEditor;

        public ReassignmentGroupsEditing(TableViewer viewer) {
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
            return ((Reassignment) element).getGroups();
        }

        protected void setValue(Object element, Object value) {
            ((Reassignment) element).setGroups(value.toString());
            getViewer().update(element, null);
        }
    }
    
    private class ReassignmentExpiresAtEditing extends EditingSupport {
        private TextCellEditor cellEditor;

        public ReassignmentExpiresAtEditing(TableViewer viewer) {
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
            return ((Reassignment) element).getExpiresAt();
        }

        protected void setValue(Object element, Object value) {
            ((Reassignment) element).setExpiresAt(value.toString());
            getViewer().update(element, null);
        }
    }
    
    private class ReassignmentTypeEditing extends EditingSupport {
        private ComboBoxCellEditor cellEditor;

        public ReassignmentTypeEditing(TableViewer viewer) {
            super(viewer);
            cellEditor = new ComboBoxCellEditor(viewer.getTable(), new String[]{"not-started", "not-completed"});
        }

        protected boolean canEdit(Object element) {
            return true;
        }

        protected CellEditor getCellEditor(Object element) {
            return cellEditor;
        }

        protected Object getValue(Object element) {
            return ((Reassignment) element).getType();
        }

        protected void setValue(Object element, Object value) {
            ((Reassignment) element).setType((Integer)value);
            getViewer().update(element, null);
        }
    }
    
    private class Reassignment {
        
        private String expiresAt = "";
        private String users = "";
        private String groups = "";
        // deadline type - start(0) or end(1)
        private Integer type = 0;
        
        public Reassignment() {
        	
        }
        
        public Reassignment(String dataInput, String type) {
        	if ("not-started".equalsIgnoreCase(type)) {
        		this.type = 0;
        	} else if ("not-completed".equalsIgnoreCase(type)) {
        		this.type = 1;
        	}
        	
        	String[] components = dataInput.split(ELEMENT_SEPARATOR);
        	
        	String actions = components[0].substring(1, components[0].length()-1);
        	String[] details = actions.split(ATTRIBUTES_SEPARATOR_ESCAPED);
        	if (details[0].toLowerCase().startsWith("users")) {
        		this.users = details[0].substring(6);
        	} else if (details[0].toLowerCase().startsWith("groups")) {
        		this.groups = details[0].substring(7);
        	}
        	if (details.length > 1) {
	        	if (details[1].toLowerCase().startsWith("users")) {
	        		this.users = details[1].substring(6);
	        	} else if (details[1].toLowerCase().startsWith("groups")) {
	        		this.groups = details[1].substring(7);
	        	}
        	}
        	
        	this.expiresAt = components[1].substring(1, components[1].length()-1);
        }
        
		public String getExpiresAt() {
			return expiresAt;
		}
		public void setExpiresAt(String expiresAt) {
			this.expiresAt = expiresAt;
		}
		public String getUsers() {
			return users;
		}
		public void setUsers(String users) {
			this.users = users;
		}
		public String getGroups() {
			return groups;
		}
		public void setGroups(String groups) {
			this.groups = groups;
		}
        
        public String toDataInput() {
        	boolean separatorRequired = false;
        	StringBuffer dataInput = new StringBuffer();
        	dataInput.append("[");
        	if (users != null && users.length() > 0) {
        		dataInput.append("users:");
        		dataInput.append(users);
        		separatorRequired = true;
        	}
        	if (groups != null && groups.length() > 0) {
        		if (separatorRequired) {
        			dataInput.append(ATTRIBUTES_SEPARATOR);
        		}
        		dataInput.append("groups:");
        		dataInput.append(groups);
        	}
        	dataInput.append("]");
        	dataInput.append(ELEMENT_SEPARATOR);
        	dataInput.append("[");
        	dataInput.append(expiresAt);
        	dataInput.append("]");
        	return dataInput.toString();
        }
		public void setType(Integer type) {
			this.type = type;
		}
		public Integer getType() {
			return type;
		}
		public String getTypeAsString() {
			if (type == 0) {
				return "not-started";
			} else if (type == 1) {
				return "not-completed";
			}
			return null;
		}

    }
    
    private class NotificationsLabelProvider extends ColumnLabelProvider {
        public String getText(Object element) {
            return ((Notification) element).getSubject();
        }
    }

    private class NotificationsEditing extends EditingSupport {
        private TextCellEditor cellEditor;

        public NotificationsEditing(TableViewer viewer) {
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
            return ((Notification) element).getSubject();
        }

        protected void setValue(Object element, Object value) {
            ((Notification) element).setSubject(value.toString());
            getViewer().update(element, null);
        }
    }
    
    private class Notification {
        
        private String expiresAt = "";
        private String from = "";
        private String to = "";
        private String toGroups = "";
        private String replyTo = "";
        private String subject ="";
        private String body ="";
        
        // deadline type - start or end
        private String type="";
        
        public Notification() {
        	
        }
        
        public Notification(String dataInput, String type) {
        	this.type = type;
        	
        	String[] components = dataInput.split(ELEMENT_SEPARATOR);
        	
        	String actions = components[0].substring(1, components[0].length()-1);
        	String[] details = actions.split(ATTRIBUTES_SEPARATOR_ESCAPED);
        	Properties parameters = new Properties();
        	for (String detail : details) {
        		
        		for (String knownKey : KNOWN_KEYS) {
            		if (detail.startsWith(knownKey)) {
            			try {
            				parameters.put(knownKey, detail.substring(knownKey.length()+KEY_VALUE_SEPARATOR.length()));
            			} catch (IndexOutOfBoundsException e) {
            				parameters.put(knownKey, "");
    					}
            		}
            	}
        	}
        	this.setFrom(parameters.getProperty("from", ""));
        	this.setTo(parameters.getProperty("tousers", ""));
        	this.setToGroups(parameters.getProperty("togroups", ""));
        	this.setReplyTo(parameters.getProperty("replyTo", ""));
        	this.setSubject(parameters.getProperty("subject", ""));
        	this.setBody(parameters.getProperty("body", ""));
        	
        	this.expiresAt = components[1].substring(1, components[1].length()-1);
        }

		public String getExpiresAt() {
			return expiresAt;
		}

		public void setExpiresAt(String expiresAt) {
			this.expiresAt = expiresAt;
		}

		public String getFrom() {
			return from;
		}

		public void setFrom(String from) {
			this.from = from;
		}

		public String getTo() {
			return to;
		}

		public void setTo(String to) {
			this.to = to;
		}

		public String getReplyTo() {
			return replyTo;
		}

		public void setReplyTo(String replyTo) {
			this.replyTo = replyTo;
		}

		public String getSubject() {
			return subject;
		}

		public void setSubject(String subject) {
			this.subject = subject;
		}

		public String getBody() {
			return body;
		}

		public void setBody(String body) {
			this.body = body;
		}

		public String getType() {
			return type;
		}

		public void setType(String type) {
			this.type = type;
		}
        
		public String toDataInput() {
        	boolean separatorRequired = false;
        	StringBuffer dataInput = new StringBuffer();
        	dataInput.append("[");
        	if (from != null && from.length() > 0) {
        		dataInput.append("from:");
        		dataInput.append(from);
        		separatorRequired = true;
        	}
        	if (to != null && to.length() > 0) {
        		if (separatorRequired) {
        			dataInput.append(ATTRIBUTES_SEPARATOR);
        		}
        		dataInput.append("tousers:");
        		dataInput.append(to);
        		separatorRequired = true;
        	}
        	
        	if (toGroups != null && toGroups.length() > 0) {
        		if (separatorRequired) {
        			dataInput.append(ATTRIBUTES_SEPARATOR);
        		}
        		dataInput.append("togroups:");
        		dataInput.append(toGroups);
        		separatorRequired = true;
        	}
        	
        	if (replyTo != null && replyTo.length() > 0) {
        		if (separatorRequired) {
        			dataInput.append(ATTRIBUTES_SEPARATOR);
        		}
        		dataInput.append("replyTo:");
        		dataInput.append(replyTo);
        		separatorRequired = true;
        	}
        	if (subject != null && subject.length() > 0) {
        		if (separatorRequired) {
        			dataInput.append(ATTRIBUTES_SEPARATOR);
        		}
        		dataInput.append("subject:");
        		dataInput.append(subject);
        		separatorRequired = true;
        	}
        	if (body != null && body.length() > 0) {
        		if (separatorRequired) {
        			dataInput.append(ATTRIBUTES_SEPARATOR);
        		}
        		dataInput.append("body:");
        		dataInput.append(body);
        		separatorRequired = true;
        	}
        	dataInput.append("]");
        	dataInput.append(ELEMENT_SEPARATOR);
        	dataInput.append("[");
        	dataInput.append(expiresAt);
        	dataInput.append("]");
        	return dataInput.toString();
        }

		public void setToGroups(String toGroups) {
			this.toGroups = toGroups;
		}

		public String getToGroups() {
			return toGroups;
		}
    }
    
    private class NotificationsContentProvider implements IStructuredContentProvider {
        public Object[] getElements(Object inputElement) {
            return notifications.toArray();
        }
        public void dispose() {
        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    }
}
