package org.drools.eclipse.editors;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.preferences.IDroolsConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.reconciler.DirtyRegion;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;

public class DRLReconcilingStrategy implements IReconcilingStrategy {

    private static final Pattern RULE_PATTERN = Pattern.compile("\\n\\s*(rule\\s+.*?\\n\\s*end)", Pattern.DOTALL);
    private static final Pattern TEMPLATE_PATTERN = Pattern.compile("\\n\\s*(template\\s+.*?\\n\\s*end)", Pattern.DOTALL);
    private static final Pattern QUERY_PATTERN = Pattern.compile("\\n\\s*(query\\s+.*?\\n\\s*end)", Pattern.DOTALL);
    private static final Pattern FUNCTION_PATTERN = Pattern.compile("\\n\\s*(function\\s+[^\\{]*\\{)", Pattern.DOTALL);
    private static final Pattern IMPORT_PATTERN = Pattern.compile("\\n\\s*((\\s*import\\s+[^\\s;]+;?[\\t\\x0B\\f\\r]*\\n)+)", Pattern.DOTALL);
    
	private ISourceViewer sourceViewer;
	private AbstractRuleEditor editor;
	private IDocument document;
    private boolean folding;

	public DRLReconcilingStrategy(ISourceViewer sourceViewer, final AbstractRuleEditor editor) {
		this.sourceViewer = sourceViewer;
		this.editor = editor;
		IPreferenceStore preferenceStore = DroolsEclipsePlugin.getDefault().getPreferenceStore();
    	folding = preferenceStore.getBoolean(IDroolsConstants.EDITOR_FOLDING);
    	preferenceStore.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (IDroolsConstants.EDITOR_FOLDING.equals(event.getProperty())) {
					folding = ((Boolean) event.getNewValue()).booleanValue();
					if (folding) {
						reconcile();
					} else {
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								editor.updateFoldingStructure(new ArrayList());
							}
						});
					}
				}
			}
    	});
    }

	public void setDocument(IDocument document) {
		this.document = document;
		reconcile();
	}

	public void reconcile(DirtyRegion dirtyRegion, IRegion region) {
		reconcile();
	}

	public void reconcile(IRegion region) {
		reconcile();
	}

	private void reconcile() {
		if (document != null) {
            String s = document.get();
            if (folding) {
            	calculateFolding(s);
            }
            
//			IFile file = ((IFileEditorInput) input).getFile();
//			IAnnotationModel annotationModel = sourceViewer.getAnnotationModel();
//	        removeAnnotationsFor(annotationModel);
//            DroolsBuildMarker[] markers = DroolsBuilder.parseFile(file, s);
//            for (int i = 0; i < markers.length; i++) {
//            	createAnnotation(file, annotationModel, markers[i].getText(), markers[i].getOffset(), markers[i].getLength());
//            }
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
			if (annotation instanceof DRLProblemAnnotation) {
				annotationModel.removeAnnotation(annotation);
			}
		}
    }

    protected void calculateFolding(String input) {
    	// TODO replace this parsing by getting this input from the parsed rule file
    	final List positions = new ArrayList();
        Matcher matcher = RULE_PATTERN.matcher(input);
        while (matcher.find()) {
			positions.add(new Position(matcher.start(1), matcher.end(1) - matcher.start(1)));
        }
        matcher = QUERY_PATTERN.matcher(input);
        while (matcher.find()) {
			positions.add(new Position(matcher.start(1), matcher.end(1) - matcher.start(1)));
        }
        matcher = TEMPLATE_PATTERN.matcher(input);
        while (matcher.find()) {
			positions.add(new Position(matcher.start(1), matcher.end(1) - matcher.start(1)));
        }
        matcher = IMPORT_PATTERN.matcher(input);
        while (matcher.find()) {
			positions.add(new Position(matcher.start(1), matcher.end(1) - matcher.start(1)));
        }
        matcher = FUNCTION_PATTERN.matcher(input);
        while (matcher.find()) {
        	int start = matcher.start(1);
        	// TODO also take comments, strings etc. in consideration
        	// use JavaPairMatcher or similar
        	int nbOpenBrackets = 1;
        	for (int i = matcher.end(); i < input.length(); i++) {
    			if (input.charAt(i) == '{') {
    				nbOpenBrackets++;
    			} else if (input.charAt(i) == '}') {
    				if (--nbOpenBrackets == 0) {
            			positions.add(new Position(start, i - start + 1));
            			break;
    				}
    			}
        	}
        }
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				editor.updateFoldingStructure(positions);
			}
		});
	}
}