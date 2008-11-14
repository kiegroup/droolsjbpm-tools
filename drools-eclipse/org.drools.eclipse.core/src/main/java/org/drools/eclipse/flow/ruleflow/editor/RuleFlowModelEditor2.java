package org.drools.eclipse.flow.ruleflow.editor;

/*
 * Copyright 2006 JBoss Inc
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

import org.drools.eclipse.DroolsEclipsePlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.forms.editor.FormEditor;

/**
 * This is a multi table editor wrapper for both the text editor and the flow
 * chart.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class RuleFlowModelEditor2 extends FormEditor {

    private RuleFlowModelEditor editor;
    private TextEditor xmlEditor;

    public void init(IEditorSite site, IEditorInput input)
            throws PartInitException {
        super.init(site, input);
        setPartName(input.getName());
    }

    protected void addPages() {
        try {
            editor = new RuleFlowModelEditor();
            xmlEditor = new TextEditor() {
                public boolean isEditable() {
                    return false;
                }
                
                public void close(boolean save) {
                    super.close(save);
                    RuleFlowModelEditor2.this.close(save);
                }

                protected void setPartName(String partName) {
                    super.setPartName(partName);
                    RuleFlowModelEditor2.this.setPartName(partName);
                }
            };

            int graph = addPage(editor, getEditorInput());

            int xml = addPage(xmlEditor, getEditorInput());

            setPageText(graph, "Graph");
            setPageText(xml, "XML");

            xmlEditor.getDocumentProvider().getDocument(getEditorInput())
                .addDocumentListener(new IDocumentListener() {
                        public void documentAboutToBeChanged(DocumentEvent event) {
                        }
                        public void documentChanged(DocumentEvent event) {
                            editor.setInput(getEditorInput());
                        }
                    });
        } catch (PartInitException e) {
            DroolsEclipsePlugin.log(e);
        }
    }

    public void doSave(IProgressMonitor monitor) {
        editor.doSave(monitor);
        setInput(getEditorInput());
    }

    public void doSaveAs() {
        editor.doSaveAs();
    }

    public boolean isSaveAsAllowed() {
        return editor.isSaveAsAllowed();
    }
    
    public Object getAdapter(Class adapter) {
        return editor.getAdapter(adapter);
    }

//    public void setFocus() {
//        if (getActivePage() == 0) {
//            try {
//                String content = xmlEditor.getDocumentProvider().getDocument(getEditorInput()).get();
//                PackageBuilderConfiguration configuration = new PackageBuilderConfiguration();
//                XmlProcessReader xmlReader = new XmlProcessReader( configuration.getSemanticModules() );
//                final String isValidatingString = System.getProperty("drools.schema.validating");
//                System.setProperty("drools.schema.validating", "true");
//                RuleFlowProcess process = (RuleFlowProcess) xmlReader.read(new StringReader(content));
//                System.setProperty("drools.schema.validating", isValidatingString);
//                if (process != null) {
//                    xmlEditor.doSave(null);
//                    editor.setInput(getEditorInput());
//                }
//            } catch (Throwable t) {
//                DroolsEclipsePlugin.log(t);
//                handleError(t);
//                setActivePage(1);
//            }
//        } else if (getActivePage() == 1) {
//            editor.doSave(null);
//            xmlEditor.setInput(getEditorInput());
//        }
//        super.setFocus();
//    }

//    private void handleError(Throwable t) {
//        DroolsEclipsePlugin.log(t);
//        Throwable cause = t.getCause();
//        if (cause == null) {
//            cause = t;
//        }
//        String message = cause.getClass().getName() + ": " + cause.getMessage();
//        if (message == null || message.length() == 0) {
//            message = "Uncategorized Error!";
//        }
//        IStatus status = new Status(IStatus.ERROR, DroolsEclipsePlugin
//                .getUniqueIdentifier(), -1, message, null);
//        ErrorDialog.openError(getSite().getShell(), "Rete Tree Build Error!",
//                "Unable to parse XML!", status);
//
//    }

}
