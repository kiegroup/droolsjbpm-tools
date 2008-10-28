package org.drools.eclipse.flow.ruleflow.view.property.action;

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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.drools.eclipse.editors.DRLRuleEditor;
import org.drools.eclipse.editors.completion.RuleCompletionProcessor;
import org.drools.lang.descr.GlobalDescr;
import org.drools.workflow.core.WorkflowProcess;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Completion for ruleflow constraints. 
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class ActionCompletionProcessor extends RuleCompletionProcessor {

	private WorkflowProcess process;
	private List<String> imports;
	private List<String> functions;
	private List<GlobalDescr> globals;
	private Map<String, String> attributes;
	private String dialect;
	
	public ActionCompletionProcessor(WorkflowProcess process) {
		super(null);
		this.process = process;
	}
	
	public void setDialect(String dialect) {
	    this.dialect = dialect;
	    this.attributes = null;
	}
	
	public IEditorPart getEditor() {
		IWorkbench workbench = PlatformUI.getWorkbench();
		if (workbench != null) { 
			IWorkbenchWindow workbenchWindow = workbench.getActiveWorkbenchWindow();
			if (workbenchWindow != null) {
				IWorkbenchPage workbenchPage = workbenchWindow.getActivePage(); 
				if (workbenchPage != null) {
					return workbenchPage.getActiveEditor();
				}
			}
		}
		return null;
	}

    protected String readBackwards(int documentOffset, IDocument doc) throws BadLocationException {
        int startPart = doc.getPartition(documentOffset).getOffset();
        String prefix = doc.get(startPart, documentOffset - startPart);
        return "rule dummy "
            + (dialect == null ? "" : " dialect \"" + dialect + "\" ")
            + "\n when \n then \n org.drools.spi.ProcessContext context = null; \n " + prefix;
    }
    
    public List<String> getImports() {
    	if (imports == null) {
    		loadImports();
    	}
    	return imports;
    }
    
    private void loadImports() {
    	this.imports = new ArrayList<String>();
    	List<String> imports = process.getImports();
    	if (imports != null) {
	    	Iterator<String> iterator = imports.iterator();
	        while (iterator.hasNext()) {
	            String importName = iterator.next();
	            if (importName.endsWith(".*")) {
	            	IJavaProject javaProject = getJavaProject();
	            	if (javaProject != null) {
		                String packageName = importName.substring(0, importName.length() - 2);
		                this.imports.addAll(DRLRuleEditor.getAllClassesInPackage(packageName, javaProject));
	            	}
	            } else {
	            	this.imports.add(importName);
	            }
	        }
    	}
    }
    
    public List<GlobalDescr> getGlobals() {
    	if (globals == null) {
    		loadGlobals();
    	}
    	return globals;
    }
    
    private void loadGlobals() {
    	String[] globalNames = process.getGlobalNames();
    	this.globals = new ArrayList<GlobalDescr>(globalNames.length);
    	for (String globalName: globalNames) {
    		this.globals.add(new GlobalDescr(globalName, "java.lang.Object"));
    	}
    }
    
    protected List<String> getFunctions() {
    	if (functions == null) {
    		loadFunctions();
    	}
    	return functions;
    }
    
    private void loadFunctions() {
    	this.functions = new ArrayList<String>();
    	List<String> imports = process.getFunctionImports();
    	if (imports != null) {
	    	for (String functionImport: imports) {
                int index = functionImport.lastIndexOf('.');
                if (index != -1) {
                    functions.add(functionImport.substring(index + 1));
                }
            }
    	}
    }
    
    private void loadAttributes() {
        if (this.dialect == null) {
            attributes = Collections.EMPTY_MAP;
        } else {
            Map<String, String> result = new HashMap<String, String>();
            result.put("dialect", dialect);
            attributes = result;
        }
    }
    
    protected Map<String, String> getAttributes() {
        if (attributes == null) {
            loadAttributes();
        }
        return attributes;
    }
    
    private IJavaProject getJavaProject() {
    	IEditorPart editor = getEditor();
    	if (editor != null && editor.getEditorInput() instanceof IFileEditorInput) {
			IFile file = ((IFileEditorInput) editor.getEditorInput()).getFile();
	    	try {
	    		if (file.getProject().getNature("org.eclipse.jdt.core.javanature") != null) {
	    			return JavaCore.create(file.getProject());
	    		}
	    	} catch (CoreException e) {
	    		// do nothing
	    	}
		}
    	return null;
    }
    
    public void reset() {
    	this.imports = null;
    	this.globals = null;
    }
}
