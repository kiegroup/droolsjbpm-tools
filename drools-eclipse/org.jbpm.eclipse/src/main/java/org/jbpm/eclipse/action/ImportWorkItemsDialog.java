/*
 * Copyright 2005 JBoss Inc
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

package org.jbpm.eclipse.action;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.drools.core.util.ConfFileUtils;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.jbpm.process.workitem.WorkDefinitionImpl;
import org.jbpm.process.workitem.WorkItemRepository;

/**
 * Dialog for importing workitems.
 */
public class ImportWorkItemsDialog extends Dialog {

	private static final String EOL = System.getProperty( "line.separator" );
	
	private TreeViewer treeViewer;
	private Text urlText;
	private Button importButton;
	private Browser documentation;
	private IJavaProject project;
	private Button addToConfigFile;
	private Button addLibraries;
	private Button registerHandlers;
	
    public ImportWorkItemsDialog(IJavaProject project, Shell parentShell) {
        super(parentShell);
        this.project = project;
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }

    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Import services");
    }
    
    protected Point getInitialSize() {
        return new Point(600, 800);
    }
    
    protected Control createDialogArea(Composite parent) {
    	Composite top = (Composite) super.createDialogArea(parent);
    	top.setLayout(new FillLayout());
    	
        SashForm sashForm = new SashForm(top, SWT.VERTICAL);
        Composite composite = new Composite(sashForm, SWT.NONE);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 4;
        composite.setLayout(gridLayout);

        Label label = new Label(composite, SWT.NONE);
        label.setText("URL:");
        GridData gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        label.setLayoutData(gridData);
        urlText = new Text(composite, SWT.NONE);
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        urlText.setLayoutData(gridData);
        Button findButton = new Button(composite, SWT.NONE);
        findButton.setText("...");
        findButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                findRepository();
            }
        });
        gridData = new GridData();
        findButton.setLayoutData(gridData);
        Button getButton = new Button(composite, SWT.NONE);
        getButton.setText("Get");
        getButton.addSelectionListener(new SelectionAdapter(){
            public void widgetSelected(SelectionEvent e) {
                getWorkDefinitions();
            }
        });
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        getButton.setLayoutData(gridData);

        treeViewer = new TreeViewer(composite, SWT.MULTI);
        treeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
        		importButton.setEnabled(!event.getSelection().isEmpty());
            	if (!event.getSelection().isEmpty()) {
            		Object selected = ((StructuredSelection) event.getSelection()).getFirstElement();
            		if (selected instanceof WorkDefinitionImpl) {
            			String docs = ((WorkDefinitionImpl) selected).getDocumentation();
            			if (docs != null) {
            				documentation.setUrl(((WorkDefinitionImpl) selected).getPath() + "/" + docs);
            			} else {
            				documentation.setText("");
            			}
            		} else {
            			documentation.setText("");
            		}
            	}
            }
        });
        treeViewer.setContentProvider(new ITreeContentProvider() {
			public void inputChanged(Viewer arg0, Object arg1, Object arg2) {
			}
			public void dispose() {
			}
			public boolean hasChildren(Object object) {
				if (object instanceof Category) {
					return !((Category) object).getWorkDefinitions().isEmpty();
				}
				return false;
			}
			public Object getParent(Object arg0) {
				return null;
			}
			public Object[] getElements(Object object) {
				return getChildren(object);
			}
			public Object[] getChildren(Object object) {
				if (object instanceof Collection<?>) {
					Object[] result = ((Collection<?>) object).toArray();
					Arrays.sort(result, new Comparator<Object>() {
						public int compare(Object o1, Object o2) {
							return ((Category) o1).name.compareTo(((Category) o2).name);
						}
					});
					return result;
				}
				if (object instanceof Category) {
					return ((Category) object).getWorkDefinitions().toArray();
				}
				return null;
			}
		});
        gridData = new GridData();
        gridData.grabExcessHorizontalSpace = true;
        gridData.grabExcessVerticalSpace = true;
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.FILL;
        gridData.horizontalSpan = 3;
        treeViewer.getTree().setLayoutData(gridData);
        
        importButton = new Button(composite, SWT.NONE);
        importButton.setText("Import");
        gridData = new GridData();
        gridData.horizontalAlignment = GridData.FILL;
        gridData.verticalAlignment = GridData.BEGINNING;
        importButton.setLayoutData(gridData);
        importButton.setEnabled(false);
        importButton.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                importWorkDefinitions();
            }
        });
        addToConfigFile = new Button(composite, SWT.CHECK | SWT.LEFT);
        addToConfigFile.setText("Automatically add to service configuration file");
        GridData data = new GridData();
        data.horizontalSpan = 4;
        addToConfigFile.setLayoutData(data);
	    addToConfigFile.setSelection(true);
        addLibraries = new Button(composite, SWT.CHECK | SWT.LEFT);
        addLibraries.setText("Automatically add libraries to project");
        data = new GridData();
        data.horizontalSpan = 4;
        addLibraries.setLayoutData(data);
        addLibraries.setSelection(true);
        registerHandlers = new Button(composite, SWT.CHECK | SWT.LEFT);
        registerHandlers.setText("Automatically register handlers");
        data = new GridData();
        data.horizontalSpan = 4;
        registerHandlers.setLayoutData(data);
        registerHandlers.setSelection(true);
		try {
			documentation = new Browser(sashForm, SWT.NONE);
		} catch (SWTError e) {
			MessageBox messageBox = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
			messageBox.setMessage("Browser cannot be initialized.");
			messageBox.setText("Exit");
			messageBox.open();
		}
		return top;
    }
    
    private void findRepository() {
		String selectedDirectory = null;
		String dirName = urlText.getText();

		DirectoryDialog dialog = new DirectoryDialog(getShell());
		dialog.setMessage("Select the jBPM service repository");
		dialog.setFilterPath(dirName);
		selectedDirectory = dialog.open();
		
		if (selectedDirectory != null) {
			urlText.setText(selectedDirectory);
		}
    }

    private void getWorkDefinitions() {
    	Map<String, Category> categories = new HashMap<String, Category>();
    	String url = urlText.getText();
    	if (!url.startsWith("http") && !url.startsWith("file:")) {
    		url = "file:/" + url;
    	}
    	Map<String, WorkDefinitionImpl> workDefinitions = 
    		WorkItemRepository.getWorkDefinitions(url);
    	for (WorkDefinitionImpl workDef: workDefinitions.values()) {
    		String c = workDef.getCategory();
    		if (c == null) {
    			c = "Other";
    		}
    		Category category = categories.get(c);
    		if (category == null) {
    			category = new Category(c);
    			categories.put(c, category);
    		}
    		category.addWorkDefinition(workDef);
    	}
		treeViewer.setInput(categories.values());
    }
    
    private void importWorkDefinitions() {
    	IFolder folder = project.getProject().getFolder("src/main/resources");
    	if (!folder.exists()) {
    		folder = project.getProject().getFolder("src/main/rules");
    	}
    	if (!folder.exists()) {
    		throw new IllegalArgumentException("Could not find src/main/resources folder.");
    	}
    	try {
	        IFolder metaInf = folder.getFolder("META-INF");
	        if (!metaInf.exists()) {
	        	createFolder(metaInf, null);
	        }
	        IFile workDefinitionsConfig = metaInf.getFile("drools.rulebase.conf");
	        if (addToConfigFile.getSelection() && !workDefinitionsConfig.exists()) {
	        	workDefinitionsConfig.create(new ByteArrayInputStream("".getBytes()), true, null);
	        }
	    	Iterator<Object> iterator =
	    		((StructuredSelection) treeViewer.getSelection()).iterator();
	    	Map<String, WorkDefinitionImpl> workDefs = new HashMap<String, WorkDefinitionImpl>();
	        while (iterator.hasNext()) {
	        	Object o = iterator.next();
	        	if (o instanceof Category) {
	        		for (WorkDefinitionImpl workDef: ((Category) o).getWorkDefinitions()) {
	        			importWorkDefinition(workDef, folder, metaInf, workDefinitionsConfig);
	        			workDefs.put(workDef.getName(), workDef);
	        		}
	        	} else if (o instanceof WorkDefinitionImpl) {
	        		importWorkDefinition((WorkDefinitionImpl) o, folder, metaInf, workDefinitionsConfig);
        			workDefs.put(((WorkDefinitionImpl) o).getName(), (WorkDefinitionImpl) o);
	        	}
	        }
	        if (registerHandlers.getSelection()) {
		        IFile sessionConfig = metaInf.getFile("drools.session.conf");
		        if (!sessionConfig.exists()) {
		        	sessionConfig.create(new ByteArrayInputStream("".getBytes()), true, null);
		        }
	        	String[] ss = inputStreamContentsToString(sessionConfig.getContents()).split(EOL);
	        	String output = "";
	        	boolean found = false;
	        	for (String s: ss) {
	        		if (!found && s.trim().startsWith("drools.workItemHandlers")) {
	        			found = true;
	        			if (!s.contains("workItemHandlers.conf")) {
	        				s += s + " workItemDefinitions.conf";
	        			}
	        		}
	        		output += s + EOL;
	        	}
	        	if (!found) {
	        		output += "drools.workItemHandlers = workItemHandlers.conf"; 
	        	}
	        	String newInput = output;
	        	sessionConfig.setContents(new ByteArrayInputStream(newInput.getBytes()), true, false, null);
	        	IFile workItemHandlersConfig = metaInf.getFile("workItemHandlers.conf");
		        if (!workItemHandlersConfig.exists()) {
		        	String content = "[";
		        	for (WorkDefinitionImpl def: workDefs.values()) {
			        	if (def.getDefaultHandler() != null) {
			        		content += EOL + "  \"" + def.getName() + "\" : new " + def.getDefaultHandler() + "(),";
			        	}
			        }
		        	if (content.endsWith(",")) {
		        		content = content.substring(0, content.length() - 1) + EOL + "]";
		        	}
		        	workItemHandlersConfig.create(new ByteArrayInputStream(content.getBytes()), true, null);
		        } else {
			        String content = inputStreamContentsToString(workItemHandlersConfig.getContents()).trim();
			        String newContent = "[";
			        if (content.startsWith("[")) {
			        	content = content.substring(1);
			        }
			        for (WorkDefinitionImpl def: workDefs.values()) {
			        	if (def.getDefaultHandler() != null) {
			        		newContent += EOL + "  \"" + def.getName() + "\" : new " + def.getDefaultHandler() + "(),";
			        	}
			        }
			        newContent += content;
			        workItemHandlersConfig.setContents(new ByteArrayInputStream(newContent.getBytes()), true, false, null);
		        }
	        }
	        close();
    	} catch (Throwable t) {
    		throw new IllegalArgumentException("Exception while trying to import services", t);
    	}
    }
    
    private void importWorkDefinition(WorkDefinitionImpl workDef, IFolder resources, IFolder metaInf, IFile workDefinitionsConfig) throws Exception {
    	String defFile = workDef.getPath() + "/" + workDef.getFile();
    	IFile file = metaInf.getFile(workDef.getFile());
        InputStream inputstream = new URL(defFile).openStream();
        if (!file.exists()) {
            file.create(inputstream, true, null);
        } else {
            file.setContents(inputstream, true, false, null);
        }
        if (workDef.getIcon() != null) {
	        String iconFile = workDef.getPath() + "/" + workDef.getIcon();
	    	IFile icon = resources.getFile(workDef.getIcon());
	        inputstream = new URL(iconFile).openStream();
	        if (!icon.exists()) {
	        	icon.create(inputstream, true, null);
	        } else {
	        	icon.setContents(inputstream, true, false, null);
	        }
        }
        if (addToConfigFile.getSelection()) {
        	String[] ss = inputStreamContentsToString(workDefinitionsConfig.getContents()).split(System.getProperty( "line.separator" ));
        	String output = "";
        	boolean found = false;
        	for (String s: ss) {
        		if (!found && s.trim().startsWith("drools.workDefinitions")) {
        			if (!s.contains(workDef.getFile())) {
        				s += " " + workDef.getFile();
        			}
        			found = true;
        		}
        		output += s + EOL;
        	}
        	if (!found) {
        		output += EOL + "drools.workDefinitions = " + workDef.getFile(); 
        	}
        	String newInput = output;
        	workDefinitionsConfig.setContents(new ByteArrayInputStream(newInput.getBytes()), true, false, null);
        }
        String[] dependencies = workDef.getDependencies();
        if (addLibraries.getSelection() && dependencies != null) {
        	IFolder lib = project.getProject().getFolder("lib");
        	if (!lib.exists()) {
        		createFolder(lib, null);
        	}
        	for (String dependency: dependencies) {
        		int index = dependency.indexOf(":");
        		if (index != -1) {
        			String protocol = dependency.substring(0, index);
        			dependency = dependency.substring(index + 1);
        			if ("file".equals(protocol)) {
        				String libName;
        				index = dependency.lastIndexOf("/");
        				if (index != -1) {
        					libName = dependency.substring(index + 1);
        				} else {
        					libName = dependency;
        				}
        				if (libName.startsWith("./")) {
        					libName = libName.substring(2);
        				}
        				IFile libFile = lib.getFile(libName);
        		        inputstream = new URL(workDef.getPath() + "/" + dependency).openStream();
        		        if (!libFile.exists()) {
        		        	libFile.create(inputstream, true, null);
        		        } else {
        		        	libFile.setContents(inputstream, true, false, null);
        		        }
        			}
        		}
        	}
        }
        // TODO: workItemHandler
    }
    
    private void createFolder(IFolder folder, IProgressMonitor monitor) throws CoreException {
        IContainer container = folder.getParent();
        if (container != null && !container.exists()
                && (container instanceof IFolder))
            createFolder((IFolder) container, monitor);
        if (!folder.exists()) {
            folder.create(true, true, monitor);
        }
    }
    
    public static String inputStreamContentsToString(InputStream inputStream) {
        StringBuilder builder = new StringBuilder();
        if ( inputStream == null ) {
            return null;
        }
        
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader( inputStream ));
            String line = null;
        
            while ( ( line = reader.readLine() ) != null ) { // while loop begins here
                builder.append( line );
                builder.append( "\n" );
            }
            
            reader.close();
        } catch ( IOException e ) {
            throw new RuntimeException( "Unable to read inputstream" );
        }
        return builder.toString();
    }
    
    private static class Category {
    	private String name;
    	private List<WorkDefinitionImpl> workDefinitions = new ArrayList<WorkDefinitionImpl>();
    	public Category(String name) {
    		this.name = name;
    	}
    	public String getName() {
    		return name;
    	}
    	public List<WorkDefinitionImpl> getWorkDefinitions() {
    		return workDefinitions;
    	}
    	public void addWorkDefinition(WorkDefinitionImpl workDefinition) {
    		workDefinitions.add(workDefinition);
    	}
    	public String toString() {
    		return name;
    	}
    }

}
