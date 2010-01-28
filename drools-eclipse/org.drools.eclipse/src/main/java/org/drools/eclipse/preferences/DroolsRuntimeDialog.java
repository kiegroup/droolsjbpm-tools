package org.drools.eclipse.preferences;

import java.io.File;
import java.util.List;

import org.drools.eclipse.util.DroolsRuntime;
import org.drools.eclipse.util.DroolsRuntimeManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class DroolsRuntimeDialog extends Dialog {

	private DroolsRuntime runtime;
	private Text nameText;
	private Text pathText;
	private List<DroolsRuntime> runtimes;
	
	private Listener textModifyListener = new Listener() {
        public void handleEvent(Event e) {
            boolean valid = validate();
            getButton(IDialogConstants.OK_ID).setEnabled(valid);
        }
    };
    
	public DroolsRuntimeDialog(Shell parent, List<DroolsRuntime> runtimes) {
		super(parent);
        setBlockOnOpen(true);
        this.runtimes = runtimes;
    }
    
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 3;
        composite.setLayout(gridLayout);
        
        Label label = new Label(composite, SWT.WRAP);
		label.setFont(composite.getFont());
        label.setText("Either select an existing Drools Runtime on your file system or create a new one.");
        GridData gridData = new GridData();
        gridData.horizontalSpan = 3;
        gridData.widthHint = 450;
		label.setLayoutData(gridData);
        
        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("Name: ");
        nameText = new Text(composite, SWT.NONE);
		nameText.setText(runtime == null || runtime.getName() == null ? "" : runtime.getName());
        nameText.addListener(SWT.Modify, textModifyListener);
        gridData = new GridData();
        gridData.horizontalSpan = 2;
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        nameText.setLayoutData(gridData);

        label = new Label(composite, SWT.NONE);
        label.setText("Path: ");
        pathText = new Text(composite, SWT.NONE);
		pathText.setText(runtime == null || runtime.getPath() == null ? "" : runtime.getPath());
        pathText.addListener(SWT.Modify, textModifyListener);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        pathText.setLayoutData(gridData);
        Button selectButton = new Button(composite, SWT.PUSH | SWT.LEFT);
        selectButton.setText("Browse ...");
        gridData = new GridData();
        selectButton.setLayoutData(gridData);
        selectButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				browse();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
        });

        Button createButton = new Button(composite, SWT.PUSH | SWT.LEFT);
        createButton.setText("Create a new Drools 5 Runtime ...");
        gridData = new GridData();
        gridData.horizontalSpan = 2;
        createButton.setLayoutData(gridData);
        createButton.addSelectionListener(new SelectionListener() {
			public void widgetSelected(SelectionEvent e) {
				createRuntime();
			}
			public void widgetDefaultSelected(SelectionEvent e) {
			}
        });
        
		return composite;
    }
	
	protected void createButtonsForButtonBar(Composite parent) {
		super.createButtonsForButtonBar(parent);
		getButton(IDialogConstants.OK_ID).setEnabled(false);
	}
	
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Drools Runtime");
    }
    
    protected Point getInitialSize() {
        return new Point(500, 250);
    }

    public void setDroolsRuntime(DroolsRuntime runtime) {
    	this.runtime = runtime;
	}
	
	private boolean validate() {
		String name = nameText.getText();
		if (name == null || "".equals(name.trim())) {
			return false;
		}
		if (runtime == null || !name.equals(runtime.getName())) {
			for (DroolsRuntime runtime: runtimes) {
				if (name.equals(runtime.getName())) {
					return false;
				}
			}
		}
		String location = pathText.getText();
		if (location != null) {
			File file = new File(location);
			if (file.exists() && file.isDirectory()) {
				return true;
			}
		}
		return false;
	}
	
	private void browse() {
		String selectedDirectory = null;
		String dirName = pathText.getText();

		DirectoryDialog dialog = new DirectoryDialog(getShell());
		dialog.setMessage("Select the Drools runtime directory.");
		dialog.setFilterPath(dirName);
		selectedDirectory = dialog.open();
		
		if (selectedDirectory != null) {
			pathText.setText(selectedDirectory);
		}
	}
	
	private void createRuntime() {
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		dialog.setMessage("Select the new Drools 5 runtime directory.");
		String selectedDirectory = dialog.open();
		
		if (selectedDirectory != null) {
			DroolsRuntimeManager.createDefaultRuntime(selectedDirectory);
			nameText.setText("Drools 5.1.0 runtime");
			pathText.setText(selectedDirectory);
		}
	}

	public DroolsRuntime getResult() {
		return runtime;
	}

    protected void okPressed() {
        runtime = new DroolsRuntime();
        runtime.setName(nameText.getText());
        runtime.setPath(pathText.getText());
        super.okPressed();
    }

}
