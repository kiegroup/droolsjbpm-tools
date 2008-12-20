package org.drools.eclipse.task.views;

import java.util.ArrayList;
import java.util.List;

import org.drools.task.AccessType;
import org.drools.task.I18NText;
import org.drools.task.OrganizationalEntity;
import org.drools.task.PeopleAssignments;
import org.drools.task.Task;
import org.drools.task.TaskData;
import org.drools.task.User;
import org.drools.task.service.ContentData;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class NewTaskDialog extends Dialog {

	private Task task;
	private ContentData content;
	
	private Text nameText;
	private Text actorText;
	private Text subjectText;
	private Text commentText;
	private Text priorityText;
	private Button skippableButton;
	private Text contentText;
	
	public NewTaskDialog(Shell shell) {
		super(shell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Create New Task");
    }
    
    protected Point getInitialSize() {
        return new Point(450, 350);
    }
    
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        composite.setLayout(gridLayout);
        Label label = new Label(composite, SWT.NONE);
        label.setText("Name: ");
        nameText = new Text(composite, SWT.NONE);
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        nameText.setLayoutData(gridData);
        
        label = new Label(composite, SWT.NONE);
        label.setText("Potential owner(s): ");
        actorText = new Text(composite, SWT.NONE);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        actorText.setLayoutData(gridData);
        
        label = new Label(composite, SWT.NONE);
        label.setText("Subject: ");
        subjectText = new Text(composite, SWT.NONE);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        subjectText.setLayoutData(gridData);
        
        label = new Label(composite, SWT.NONE);
        label.setText("Comment: ");
        commentText = new Text(composite, SWT.MULTI);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        commentText.setLayoutData(gridData);
        
        label = new Label(composite, SWT.NONE);
        label.setText("Priority: ");
        priorityText = new Text(composite, SWT.NONE);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        priorityText.setLayoutData(gridData);
        
        skippableButton = new Button(composite, SWT.CHECK | SWT.LEFT);
        skippableButton.setText("Skippable");
        gridData = new GridData();
        gridData.horizontalSpan = 2;
        skippableButton.setLayoutData(gridData);
        
        label = new Label(composite, SWT.NONE);
        label.setText("Content: ");
        contentText = new Text(composite, SWT.MULTI);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        contentText.setLayoutData(gridData);
        
		return composite;
	}

	protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
            createTask();
        }
        super.buttonPressed(buttonId);
    }
	
	private void createTask() {
		task = new Task();
		String taskName = nameText.getText();
		List<I18NText> names = new ArrayList<I18NText>();
		names.add(new I18NText("en-UK", taskName));
		task.setNames(names);
		String subject = subjectText.getText();
		List<I18NText> subjects = new ArrayList<I18NText>();
		subjects.add(new I18NText("en-UK", subject));
		task.setSubjects(subjects);
		String comment = commentText.getText();
		List<I18NText> descriptions = new ArrayList<I18NText>();
		descriptions.add(new I18NText("en-UK", comment));
		task.setDescriptions(descriptions);
		String priority = priorityText.getText();
		priorityText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String priority = priorityText.getText();
				boolean enabled = false;
				if (priority.length() == 0) {
					enabled = true;
				} else {
					try {
						new Integer(priority);
						enabled = true;
					} catch (NumberFormatException exc) {
						// do nothing
					}
				}
				getButton(IDialogConstants.OK_ID).setEnabled(enabled);
					
			}
		});
		try {
			task.setPriority(new Integer(priority));
		} catch (NumberFormatException e) {
			// do nothing
		}
		TaskData taskData = new TaskData();
		taskData.setSkipable(skippableButton.getSelection());
		task.setTaskData(taskData);
		
		String actors = actorText.getText();
		PeopleAssignments assignments = new PeopleAssignments();
		String[] actorIds = actors.trim().split(",");
		List<OrganizationalEntity> potentialOwners = new ArrayList<OrganizationalEntity>();
		for (String id: actorIds) {
			User user = new User();
			user.setId(id.trim());
			potentialOwners.add(user);
		}
		assignments.setPotentialOwners(potentialOwners);
		
		List<OrganizationalEntity> businessAdministrators = new ArrayList<OrganizationalEntity>();
		businessAdministrators.add(new User("Administrator"));
		assignments.setBusinessAdministrators(businessAdministrators);
		task.setPeopleAssignments(assignments);
		
		ContentData content = null;
		String contentString = contentText.getText();
		content = new ContentData();
		content.setContent(contentString.getBytes());
		content.setAccessType(AccessType.Inline);
	}
	
	public Task getTask() {
		return task;
	}
	
	public ContentData getContent() {
		return content;
	}
	
}
