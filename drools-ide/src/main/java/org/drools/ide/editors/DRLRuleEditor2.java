package org.drools.ide.editors;

import org.drools.ide.DroolsIDEPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;

/**
 * Generic rule editor for drools.
 * @author Michael Neale
 */
public class DRLRuleEditor2 extends FormEditor {

	private DRLRuleEditor textEditor;

	private ReteViewer reteViewer;

	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		super.init(site, input);
		setPartName(input.getName());
	}

	protected void addPages() {
		try {
			textEditor = new DRLRuleEditor();
			reteViewer = new ReteViewer(this, textEditor.getDocumentProvider());

			int text = addPage(textEditor, getEditorInput());
			int rete = addPage(reteViewer, getEditorInput());

			setPageText(text, "Text Editor");
			setPageText(rete, "Rete Tree");
		} catch (PartInitException e) {
			DroolsIDEPlugin.log(e);
		}
	}

	public void doSave(IProgressMonitor monitor) {
		textEditor.doSave(monitor);
		setInput(getEditorInput());
		reteViewer.clear();
	}

	public void doSaveAs() {
		textEditor.doSaveAs();
	}

	public boolean isSaveAsAllowed() {
		return textEditor.isSaveAsAllowed();
	}

    public Object getAdapter(Class adapter) {
        return textEditor.getAdapter(adapter);
    }
}
