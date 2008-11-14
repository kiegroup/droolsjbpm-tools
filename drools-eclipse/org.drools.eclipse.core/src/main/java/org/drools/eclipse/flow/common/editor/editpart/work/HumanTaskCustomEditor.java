package org.drools.eclipse.flow.common.editor.editpart.work;

import org.drools.eclipse.flow.common.view.property.EditBeanDialog;
import org.drools.process.core.Work;
import org.drools.process.core.WorkDefinition;
import org.drools.process.core.WorkEditor;
import org.drools.process.core.impl.WorkImpl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * Custom Work editor for human tasks.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class HumanTaskCustomEditor extends EditBeanDialog<Work> implements WorkEditor {

	private Text nameText;
	private Text actorText;
	private Text commentText;
	private Text priorityText;
	private Button skippableButton;
	private Text contentText;
	
    public HumanTaskCustomEditor(Shell parentShell) {
        super(parentShell, "Human Task Editor");
        setBlockOnOpen(true);
    }
    
    protected Point getInitialSize() {
        return new Point(400, 400);
    }
    
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 2;
        composite.setLayout(gridLayout);
        
        Work work = (Work) getValue();
        
        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("Name: ");
        nameText = new Text(composite, SWT.NONE);
        GridData gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        nameText.setLayoutData(gridData);
        String name = (String) work.getParameter("TaskName");
        nameText.setText(name == null ? "" : name);
        
        Label label = new Label(composite, SWT.NONE);
        label.setText("Actor(s): ");
        actorText = new Text(composite, SWT.NONE);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        actorText.setLayoutData(gridData);
        String value = (String) work.getParameter("ActorId");
        actorText.setText(value == null ? "" : value);
        
        label = new Label(composite, SWT.NONE);
        label.setText("Comment: ");
        commentText = new Text(composite, SWT.MULTI);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        commentText.setLayoutData(gridData);
        value = (String) work.getParameter("Comment");
        commentText.setText(value == null ? "" : value.toString());
        
        label = new Label(composite, SWT.NONE);
        label.setText("Priority: ");
        priorityText = new Text(composite, SWT.NONE);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        priorityText.setLayoutData(gridData);
        value = (String) work.getParameter("Priority");
        priorityText.setText(value == null ? "" : value);
        
        skippableButton = new Button(composite, SWT.CHECK | SWT.LEFT);
        skippableButton.setText("Skippable");
        value = (String) work.getParameter("Skippable");
        skippableButton.setSelection("true".equals(value));
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
        value = (String) work.getParameter("Content");
        contentText.setText(value == null ? "" : value.toString());
        
        return composite;
    }
    
    protected Work updateValue(Work value) {
        Work work = new WorkImpl();
        work.setName("Human Task");
        work.setParameter("TaskName", nameText.getText());
        work.setParameter("ActorId", actorText.getText());
        work.setParameter("Comment", commentText.getText());
        work.setParameter("Priority", priorityText.getText());
        work.setParameter("Skippable", skippableButton.getSelection() + "");
        work.setParameter("Content", contentText.getText());
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

}
