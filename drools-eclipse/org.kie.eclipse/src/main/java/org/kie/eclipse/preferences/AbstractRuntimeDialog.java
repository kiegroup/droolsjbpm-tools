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

import java.io.File;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.StringConverter;
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
import org.eclipse.swt.widgets.Widget;
import org.kie.eclipse.runtime.AbstractRuntime;
import org.kie.eclipse.runtime.IRuntime;
import org.kie.eclipse.runtime.IRuntimeManager;

public abstract class AbstractRuntimeDialog extends Dialog {

    private IRuntime runtime;
    private Text nameText;
    private Text pathText;
    private Text versionText;
    private Text errorMessageText;
    private List<IRuntime> runtimes;
    private boolean editMode;

    private Listener textModifyListener = new Listener() {
        public void handleEvent(Event e) {
            validate(e.widget);
        }
    };
    
    public AbstractRuntimeDialog(Shell parent, List<IRuntime> runtimes) {
        super(parent);
        setBlockOnOpen(true);
        this.runtimes = runtimes;
        runtime = getRuntimeManager().createNewRuntime();
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }
    
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1));
        composite.setLayout(new GridLayout(3, false));
        
        Label nameLabel = new Label(composite, SWT.NONE);
        nameLabel.setText("Name:");
        nameLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false, 1, 1));
        nameText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        nameText.setText(editMode ? runtime.getName() : "");
        nameText.addListener(SWT.Modify, textModifyListener);
        nameText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));

        Label pathLabel = new Label(composite, SWT.NONE);
        pathLabel.setText("Path:");
        pathLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false, 1, 1));
        pathText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        pathText.setText(editMode ? runtime.getPath() : "");
        pathText.addListener(SWT.Modify, textModifyListener);
        pathText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
        Button selectButton = new Button(composite, SWT.PUSH | SWT.LEFT);
        selectButton.setText("Browse...");
        selectButton.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1));
        selectButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                browse();
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        
        Label versionLabel = new Label(composite, SWT.NONE);
        versionLabel.setText("Version:");
        versionLabel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false, 1, 1));
        versionText = new Text(composite, SWT.SINGLE | SWT.BORDER);
        versionText.setText(editMode ? runtime.getVersion().toString() : "");
        versionText.addListener(SWT.Modify, textModifyListener);
        versionText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
        
        errorMessageText = new Text(composite, SWT.READ_ONLY | SWT.WRAP);
        errorMessageText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 3, 1));
        errorMessageText.setBackground(errorMessageText.getDisplay()
                .getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
	    
        validate(null);
		
        return composite;
    }

    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        if (editMode)
        	newShell.setText("Edit Runtime");
        else
        	newShell.setText("Add Runtime");
    }
    
    protected Point getInitialSize() {
    	Point p = super.getInitialSize();
//    	p.y += p.y/10;
    	return new Point(500,p.y);
    }

    public void setRuntime(IRuntime runtime) {
        if (runtime!=null) {
            this.runtime = runtime;
        	editMode = true;
        }
    }

    private void validate(Widget widget) {
    	setErrorMessage(null);
    	if (widget==nameText || widget==null) {
			String name = nameText.getText();
			if (name == null || "".equals(name.trim())) {
				setErrorMessage("Name is required");
				return;
			}
			if (!name.equals(runtime.getName())) {
				for (IRuntime runtime : runtimes) {
					if (name.equals(runtime.getName())) {
						setErrorMessage("The Runtime \"" + name + "\" is already registered");
						return;
					}
				}
			}
    	}
    	
    	if (widget==pathText || widget==null) {
			String location = pathText.getText();
			if (location != null && !location.isEmpty()) {
				File file = new File(location);
				if (!file.exists() || !file.isDirectory()) {
					setErrorMessage("Path does not exist or is not a directory");
					return;
				}
				IRuntime r = getRuntimeManager().createNewRuntime();
				r.setVersion(null);
				r.setPath(location);
				getRuntimeManager().recognizeJars(r);
				int jarCount = r.getJars()==null ? 0 : r.getJars().length;
				if (jarCount>0) {
					if (versionText.getText().isEmpty()) {
						versionText.setText(r.getVersion().toString());
						widget = versionText;
					}
					if (nameText.getText().isEmpty()) {
						nameText.setText(r.getName() + " " + r.getVersion().toString());
						validate(nameText);
					}
					if (editMode)
						r.setVersion(runtime.getVersion().toString());
					runtime = r;
				}
				else {
					setErrorMessage("The given Path does not contain any " + r.getName() + " Runtime jars");
				}
			} else {
				setErrorMessage("Path is required");
				return;
			}
    	}
    	
    	if (widget==versionText || widget==null) {
			String version = versionText.getText();
			String error = AbstractRuntime.Version.validate(version);
			if (error!=null)
				setErrorMessage(error);

    	}
    }

    private void browse() {
        String selectedDirectory = null;
        String dirName = pathText.getText();

        DirectoryDialog dialog = new DirectoryDialog(getShell());
        dialog.setMessage("Select the Runtime directory.");
        dialog.setFilterPath(dirName);
        selectedDirectory = dialog.open();

        if (selectedDirectory != null) {
            pathText.setText(selectedDirectory);
        }
    }

    public IRuntime getResult() {
        return runtime;
    }

    @Override
	protected void buttonPressed(int buttonId) {
        if (buttonId == IDialogConstants.OK_ID) {
	        runtime.setName(nameText.getText());
	        runtime.setPath(pathText.getText());
	        if (runtime.getJars()==null || runtime.getJars().length==0) {
	        	getRuntimeManager().recognizeJars(runtime);
		        if (runtime.getJars()==null || runtime.getJars().length==0) {
		        	MessageDialog.openError(getShell(), "Invalid Runtime Directory",
		        			"No Runtime was found in the specified path "+pathText.getText());
		        	return;
		        }
	        }
	        runtime.setVersion(versionText.getText());
        } else {
            runtime = null;
        }
        super.buttonPressed(buttonId);
    }

    public void setErrorMessage(String errorMessage) {
    	if (errorMessageText != null && !errorMessageText.isDisposed()) {
    		errorMessageText.setText(errorMessage == null ? " \n " : errorMessage); //$NON-NLS-1$
    		// Disable the error message text control if there is no error, or
    		// no error text (empty or whitespace only).  Hide it also to avoid
    		// color change.
    		// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=130281
    		boolean hasError = errorMessage != null && (StringConverter.removeWhiteSpaces(errorMessage)).length() > 0;
    		errorMessageText.setEnabled(hasError);
    		errorMessageText.setVisible(hasError);
    		errorMessageText.getParent().layout();
    		errorMessageText.getParent().update();
    		// Access the ok button by id, in case clients have overridden button creation.
    		// See https://bugs.eclipse.org/bugs/show_bug.cgi?id=113643
    		Control button = getButton(IDialogConstants.OK_ID);
    		if (button != null) {
    			button.setEnabled(errorMessage == null);
    		}
    	}
    }

    abstract protected IRuntimeManager getRuntimeManager();
}
