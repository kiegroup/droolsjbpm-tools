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
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.kie.eclipse.runtime.AbstractRuntimeInstaller;
import org.kie.eclipse.runtime.IRuntime;
import org.kie.eclipse.runtime.IRuntimeInstaller;
import org.kie.eclipse.runtime.IRuntimeManager;

public abstract class AbstractRuntimeDialog extends Dialog {

	private IRuntimeManager runtimeManager;
    private IRuntime runtime;
    private Text nameText;
    private Text pathText;
    private Text versionText;
    private Combo runtimesCombo;
    private Button selectExistingButton;
    private Button downloadNewButton;
    private String product;
    private IRuntimeInstaller selectedInstaller;
    private List<IRuntime> runtimes;

    private Listener textModifyListener = new Listener() {
        public void handleEvent(Event e) {
            getButton(IDialogConstants.OK_ID).setEnabled(validate());
        }
    };
    
    public AbstractRuntimeDialog(Shell parent, List<IRuntime> runtimes) {
        super(parent);
        setBlockOnOpen(true);
        this.runtimes = runtimes;
        this.runtimeManager = getRuntimeManager();
        this.product = runtimeManager.createNewRuntime().getProduct();
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }
    
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        composite.setLayout(new GridLayout(2, false));
        
        Label label = new Label(composite, SWT.WRAP);
        label.setFont(composite.getFont());
        label.setText("Either select an existing Runtime on your file system or download a new one.");
        label.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2,1));

        GridData gd = new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1);
        gd.verticalIndent = 10;
	    selectExistingButton = new Button(composite, SWT.RADIO | SWT.LEFT);
	    selectExistingButton.setText("Select an existing Runtime");
	    selectExistingButton.setLayoutData(gd);
	    
        gd = new GridData(GridData.BEGINNING, GridData.BEGINNING, false, false, 1, 1);
        gd.verticalIndent = 10;
	    downloadNewButton = new Button(composite, SWT.RADIO | SWT.LEFT);
	    downloadNewButton.setText("Download a new Runtime");
	    downloadNewButton.setLayoutData(gd);
	    
        final Composite selectExistingGroup = new Composite(composite, SWT.NONE);
        selectExistingGroup.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
        selectExistingGroup.setLayout(new GridLayout(3, false));
        
        Label nameLabel = new Label(selectExistingGroup, SWT.NONE);
        nameLabel.setText("Name:");
        nameLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1));
        nameText = new Text(selectExistingGroup, SWT.SINGLE | SWT.BORDER);
        nameText.setText(runtime == null || runtime.getName() == null ? "" : runtime.getName());
        nameText.addListener(SWT.Modify, textModifyListener);
        nameText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));

        Label pathLabel = new Label(selectExistingGroup, SWT.NONE);
        pathLabel.setText("Path:");
        pathLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1));
        pathText = new Text(selectExistingGroup, SWT.SINGLE | SWT.BORDER);
        pathText.setText(runtime == null || runtime.getPath() == null ? "" : runtime.getPath());
        pathText.addListener(SWT.Modify, textModifyListener);
        pathText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 1, 1));
        Button selectButton = new Button(selectExistingGroup, SWT.PUSH | SWT.LEFT);
        selectButton.setText("Browse...");
        selectButton.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1));
        selectButton.addSelectionListener(new SelectionListener() {
            public void widgetSelected(SelectionEvent e) {
                browse();
            }
            public void widgetDefaultSelected(SelectionEvent e) {
            }
        });
        
        Label versionLabel = new Label(selectExistingGroup, SWT.NONE);
        versionLabel.setText("Version:");
        versionLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.CENTER, false, false, 1, 1));
        versionText = new Text(selectExistingGroup, SWT.SINGLE | SWT.BORDER);
        versionText.setText(runtime == null || runtime.getName() == null ? "" : runtime.getVersion());
        versionText.addListener(SWT.Modify, textModifyListener);
        versionText.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, false, false, 2, 1));

        final Composite downloadNewGroup = new Composite(composite, SWT.NONE);
        downloadNewGroup.setLayout(new GridLayout(2, false));
        downloadNewGroup.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
        
        Label downloadLabel = new Label(downloadNewGroup, SWT.NONE);
        downloadLabel.setText("Select a Runtime to download:");
        downloadLabel.setLayoutData(new GridData(GridData.BEGINNING, GridData.FILL, false, false, 1, 1));
        
        runtimesCombo = new Combo(downloadNewGroup, SWT.READ_ONLY);
        runtimesCombo.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1));

        runtimesCombo.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
            	Integer key = runtimesCombo.getSelectionIndex();
            	selectedInstaller = (IRuntimeInstaller) runtimesCombo.getData(key.toString());
            }
        });
        
        fillRuntimesCombo();
        if (runtimesCombo.getItemCount()==0) {
        	runtimesCombo.setVisible(false);
        	gd = (GridData)runtimesCombo.getLayoutData();
        	gd.exclude = true;
        	downloadLabel.setText("There are no additional runtimes available for download.");
        }
        
	    selectExistingButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setControlVisible(selectExistingGroup, true);
				setControlVisible(downloadNewGroup, false);
	            getButton(IDialogConstants.OK_ID).setEnabled(validate());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	    downloadNewButton.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setControlVisible(selectExistingGroup, false);
				setControlVisible(downloadNewGroup, true);
	            getButton(IDialogConstants.OK_ID).setEnabled(validate());
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
	    
	    selectExistingButton.setSelection(true);
		setControlVisible(selectExistingGroup, true);
		setControlVisible(downloadNewGroup, false);
        
        return composite;
    }

    private void setControlVisible(Control control, boolean visible) {
    	Object ld = control.getLayoutData();
    	if (ld instanceof GridData) {
    		((GridData)ld).exclude = !visible;
    	}
    	control.setVisible(visible);
    	control.getParent().getParent().layout();
    	control.getParent().layout();
    }

    private void fillRuntimesCombo() {
        runtimesCombo.removeAll();
    	
        selectedInstaller = null;
        int key = 0;
        for (IRuntimeInstaller installer : AbstractRuntimeInstaller.FACTORY.createInstallers()) {
    		if (!product.equals(installer.getProduct()))
    			continue;
    		boolean alreadyInstalled = false;
        	for (IRuntime rt : runtimeManager.getConfiguredRuntimes()) {
        		if (rt.getProduct().equals(installer.getProduct())
        				&& rt.getVersion()!=null
        				&& rt.getVersion().equals(installer.getVersion())) {
        			alreadyInstalled = true;
        			break;
        		}
        	}
        	if (!alreadyInstalled) {
	        	String name = installer.getRuntimeName();
	            runtimesCombo.add(name);
	            runtimesCombo.setData(""+key, installer);
	            if (selectedInstaller==null)
	            	selectedInstaller = installer;
	            ++key;
        	}
        }
        
        runtimesCombo.select(0);
    }

    protected void createButtonsForButtonBar(Composite parent) {
        super.createButtonsForButtonBar(parent);
        getButton(IDialogConstants.OK_ID).setEnabled(false);
    }

    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("New Runtime");
    }
    
    protected Point getInitialSize() {
        return new Point(500, 250);
    }

    public void setRuntime(IRuntime runtime) {
        this.runtime = runtime;
    }

    private boolean validate() {
    	if (selectExistingButton.getSelection()) {
	        String name = nameText.getText();
	        if (name == null || "".equals(name.trim())) {
	            return false;
	        }
	        if (runtime == null || !name.equals(runtime.getName())) {
	            for (IRuntime runtime: runtimes) {
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
    	}
    	else if (downloadNewButton.getSelection()) {
    		if (runtimesCombo.getItemCount()>0)
    			return true;
    	}
        return false;
    }

    private void browse() {
        String selectedDirectory = null;
        String dirName = pathText.getText();

        DirectoryDialog dialog = new DirectoryDialog(getShell());
        dialog.setMessage("Select the runtime directory.");
        dialog.setFilterPath(dirName);
        selectedDirectory = dialog.open();

        if (selectedDirectory != null) {
            pathText.setText(selectedDirectory);
        }
    }

    private void downloadRuntime() {
    	final IRuntime rt = runtimeManager.createNewRuntime();
    	rt.setName(selectedInstaller.getRuntimeName());
    	rt.setProduct(selectedInstaller.getProduct());
    	rt.setVersion(selectedInstaller.getVersion());
//    	runtime.setPath("."+product+".runtime_"+selectedInstaller.getVersion());
    	
    	IRunnableWithProgress op = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		    	runtime = runtimeManager.downloadOrCreateRuntime(rt, monitor);
			}
		};
    	try {
			new ProgressMonitorDialog(getShell()).run(true, true, op);
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }

    public IRuntime getResult() {
        return runtime;
    }

    protected void okPressed() {
    	if (selectExistingButton.getSelection()) {
	        runtime = getRuntimeManager().createNewRuntime();
	        runtime.setName(nameText.getText());
	        runtime.setPath(pathText.getText());
	        runtime.setVersion(versionText.getText());
    	}
    	else {
    		downloadRuntime();
    	}
        super.okPressed();
    }

    abstract protected IRuntimeManager getRuntimeManager();
}
