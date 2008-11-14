package org.drools.eclipse.dsl.editor;

import org.drools.eclipse.editors.DRLDocumentProvider;
import org.eclipse.jface.text.IDocument;

public class DSLtoDRLDocumentProvider extends DRLDocumentProvider {

	private DSLtoDRLRuleViewer drlViewer;
	private IDocument document;
	
	public DSLtoDRLDocumentProvider(DSLtoDRLRuleViewer drlViewer) {
		this.drlViewer = drlViewer;
	}
	
	public IDocument getDocument(Object element) {
		if (document == null) {
			IDocument superDocument = super.getDocument(element);
			document = new DSLtoDRLDocument(superDocument, drlViewer);
		}
		return document;
    }
	
	public boolean isModifiable(Object element) {
		return false;
	}
	
}
