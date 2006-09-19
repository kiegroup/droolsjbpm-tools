package org.drools.ide.editors;

import org.drools.ide.DroolsIDEPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
//import org.eclipse.ui.texteditor.IDocumentProvider;
//import org.eclipse.ui.texteditor.IDocumentProviderExtension;

/**
 * This is a multi table editor wrapper for both the text editor and the RETE
 * viewer.
 * @author Kris.
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
			textEditor = new DRLRuleEditor() {
				public void close(boolean save) {
					super.close(save);
					DRLRuleEditor2.this.close(save);
				}
				protected void setPartName(String partName) {
					super.setPartName(partName);
					DRLRuleEditor2.this.setPartName(partName);
			    }
			};
			//IDocumentProvider provider = textEditor.getDocumentProvider();
			//if (provider instanceof IDocumentProviderExtension) {
			//	IDocumentProviderExtension extension = (IDocumentProviderExtension) provider;
			//	extension.isModifiable(getEditorInput());
			//}
			reteViewer = new ReteViewer(textEditor, textEditor.getDocumentProvider());

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
