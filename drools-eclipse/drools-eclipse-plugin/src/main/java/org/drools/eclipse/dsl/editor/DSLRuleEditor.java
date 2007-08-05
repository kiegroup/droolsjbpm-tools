package org.drools.eclipse.dsl.editor;

import org.drools.eclipse.editors.DRLRuleEditor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ui.part.FileEditorInput;

public class DSLRuleEditor extends DRLRuleEditor {

    protected DSLAdapter dslAdapter;

	public DSLAdapter getDSLAdapter() {
		if (dslAdapter == null) {
			try {
				String content = getSourceViewer().getDocument().get();
				dslAdapter = new DSLAdapter(content, ((FileEditorInput) getEditorInput()).getFile());
				if (!dslAdapter.isValid()) {
					dslAdapter = null;
				}
			} catch (CoreException exc) {
				dslAdapter = null;
			}
		}
		return dslAdapter;
	}

	protected SourceViewerConfiguration createSourceViewerConfiguration() {
		return new DSLRuleSourceViewerConfig(this); 
	}

	public void doSave(IProgressMonitor monitor) {
		super.doSave(monitor);
		// remove cached content
		dslAdapter = null;
	}
}
