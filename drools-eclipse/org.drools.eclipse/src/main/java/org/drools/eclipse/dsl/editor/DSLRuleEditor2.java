package org.drools.eclipse.dsl.editor;

import java.io.Reader;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.lang.dsl.DSLMappingFile;
import org.drools.lang.dsl.DSLTokenizedMappingFile;
import org.drools.lang.dsl.DefaultExpander;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;

/**
 * Editor for rules using a domain-specific language.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class DSLRuleEditor2 extends FormEditor {

	private DSLRuleEditor dslRuleEditor;
	private DSLtoDRLRuleViewer drlRuleViewer;

	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		setPartName(input.getName());
	}

	protected void addPages() {
		try {
			dslRuleEditor = new DSLRuleEditor() {
				public void close(boolean save) {
					super.close(save);
					DSLRuleEditor2.this.close(save);
				}
				protected void setPartName(String partName) {
					super.setPartName(partName);
					DSLRuleEditor2.this.setPartName(partName);
				}
			};
			int text = addPage(dslRuleEditor, getEditorInput());
			setPageText(text, "Text Editor");
			drlRuleViewer = new DSLtoDRLRuleViewer(dslRuleEditor);
			text = addPage(drlRuleViewer, getEditorInput());
			setPageText(text, "DRL Viewer");
		} catch (PartInitException e) {
			DroolsEclipsePlugin.log(e);
		}
	}

	public void doSave(IProgressMonitor monitor) {
		dslRuleEditor.doSave(monitor);
		setInput(getEditorInput());
	}

	public void doSaveAs() {
		dslRuleEditor.doSaveAs();
	}

	public boolean isSaveAsAllowed() {
		return dslRuleEditor.isSaveAsAllowed();
	}

	public Object getAdapter(Class adapter) {
		return dslRuleEditor.getAdapter(adapter);
	}

	public void setFocus() {
		if (getActivePage() == 1) {
			// check if translation does succeed
			try {
		        DefaultExpander expander = new DefaultExpander();
		        String content = dslRuleEditor.getContent();
	        	Reader reader = DSLAdapter.getDSLContent(content, dslRuleEditor.getResource());
	        	if (reader == null) {
	        		throw new IllegalArgumentException("Could not find dsl definition.");
	        	}
	            DSLMappingFile mapping = new DSLTokenizedMappingFile();
	            mapping.parseAndLoad(reader);
	            reader.close();
	            expander.addDSLMapping(mapping.getMapping());
	            expander.expand(content);
	            // if translation succeeds, change to drl viewer
				drlRuleViewer.setInput(getEditorInput());
	        } catch (Throwable t) {
	        	// if translation fails, show error and go to first page
	        	handleError(t);
	        	setActivePage(0);
	        }
		}
		super.setFocus();
	}

    private void handleError(Throwable t) {
        DroolsEclipsePlugin.log( t );
        Throwable cause = t.getCause();
        if ( cause == null ) {
            cause = t;
        }
        String message = cause.getClass().getName()+": "+cause.getMessage();
        if ( message == null || message.length() == 0 ) {
            message = "Uncategorized Error!";
        }
        IStatus status = new Status( IStatus.ERROR,
                                     DroolsEclipsePlugin.getUniqueIdentifier(),
                                     -1,
                                     message,
                                     null);
        ErrorDialog.openError( getSite().getShell(),
                               "DSL Rule Translation Error!",
                               "DSL Rule Translation Error!",
                               status );

    }
}
