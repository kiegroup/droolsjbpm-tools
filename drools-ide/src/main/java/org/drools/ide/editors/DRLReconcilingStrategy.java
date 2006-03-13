package org.drools.ide.editors;

import java.util.Iterator;

import org.drools.ide.builder.DroolsBuildMarker;
import org.drools.ide.builder.DroolsBuilder;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;

public class DRLReconcilingStrategy implements IReconcilingStrategy {

	private ISourceViewer sourceViewer;
	private DRLRuleEditor editor;

	public DRLReconcilingStrategy(ISourceViewer sourceViewer, DRLRuleEditor editor) {
		this.sourceViewer = sourceViewer;
		this.editor = editor;
	}

	public void setDocument(IDocument document) {
	}

	public void reconcile(DirtyRegion dirtyRegion, IRegion region) {
		//reconcile();
	}

	public void reconcile(IRegion region) {
		//reconcile();
	}

	private void reconcile() {
		IEditorInput input = editor.getEditorInput();
		if (input != null) {
			if (!(input instanceof IFileEditorInput)) {
				return;
			}
			IFile file = ((IFileEditorInput) input).getFile();
			IAnnotationModel annotationModel = sourceViewer.getAnnotationModel();
	        removeAnnotationsFor(annotationModel);

            IDocumentProvider documentProvider = editor.getDocumentProvider();
            if (documentProvider == null) {
                return;
            }
            String s = documentProvider.getDocument(input).get();
            DroolsBuildMarker[] markers = DroolsBuilder.parseFile(file, s);
            for (int i = 0; i < markers.length; i++) {
            	createAnnotation(file, annotationModel, markers[i].getText(), markers[i].getOffset(), markers[i].getLength());
            }
		} 
    }

    private static void createAnnotation(IFile file, final IAnnotationModel annotationModel, final String message, final int offset, final int length) {
		Annotation annotation = new DRLProblemAnnotation(message);
		Position position = new Position(0, 1);
//		Position position = new Position(offset, length);
        annotationModel.addAnnotation(annotation, position);
    }
    
    public static void removeAnnotationsFor(IAnnotationModel annotationModel) {
		Iterator iterator = annotationModel.getAnnotationIterator();
		while (iterator.hasNext()) {
			Annotation annotation = (Annotation) iterator.next();
			if (annotation instanceof DRLProblemAnnotation)
				annotationModel.removeAnnotation(annotation);
		};
    }
}