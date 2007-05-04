package org.drools.eclipse.dsl.editor;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.editors.AbstractRuleEditor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class DSLtoDRLRuleViewer extends AbstractRuleEditor {
	
	private DSLRuleEditor dslRuleEditor;

	public DSLtoDRLRuleViewer(DSLRuleEditor dslRuleEditor) {
		this.dslRuleEditor = dslRuleEditor;
	}
	
    protected IDocumentProvider createDocumentProvider() {
    	return new DSLtoDRLDocumentProvider(this);
    }
    
    public String getDSLRuleContent() {
    	return dslRuleEditor.getContent();
    }

	public void handleError(Throwable t) {
		DroolsEclipsePlugin.log(t);
		Throwable cause = t.getCause();
		if (cause == null) {
			cause = t;
		}
		String message = cause.getClass().getName() + ": " + cause.getMessage();
		if (message == null || message.length() == 0) {
			message = "Uncategorized Error!";
		}
		IStatus status = new Status(IStatus.ERROR, DroolsEclipsePlugin
				.getUniqueIdentifier(), -1, message, null);
		ErrorDialog.openError(getSite().getShell(),
				"DSL Rule Translation Error!", "DSL Rule Translation Error!",
				status);

	}
}
