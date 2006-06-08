package org.drools.ide.editors;

import org.drools.ide.editors.scanners.DRLPartionScanner;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.FastPartitioner;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;


/**
 * Simple document provider.
 * @author Michael Neale
 */
public class DRLDocumentProvider
    extends TextFileDocumentProvider {
    
    public IDocument getDocument(Object element) {
        IDocument document = super.getDocument(element);
        if (document != null) {
            IDocumentPartitioner partitioner =
                new FastPartitioner(
                    new DRLPartionScanner(),
                    DRLPartionScanner.LEGAL_CONTENT_TYPES);
            partitioner.connect(document);
            document.setDocumentPartitioner(partitioner);
        }
        return document;
    }
    
    protected IAnnotationModel createAnnotationModel(IFile file) {
        return new DRLAnnotationModel(file);
	}
}
