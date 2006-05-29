package org.drools.ide.editors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.drools.ide.DroolsIDEPlugin;
import org.drools.ide.preferences.IDroolsConstants;
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

    private static final Pattern RULE_PATTERN = Pattern.compile("\\s*rule\\s+\"?([^\"]+)\"?.*", Pattern.DOTALL);
    private static final Pattern QUERY_PATTERN = Pattern.compile("\\s*query\\s+\"?([^\"]+)\"?.*", Pattern.DOTALL);
    private static final Pattern FUNCTION_PATTERN = Pattern.compile("\\s*function\\s+(.*)\\s+(.*)\\(.*\\).*", Pattern.DOTALL);
    private static final Pattern END_PATTERN = Pattern.compile("\\s*end.*", Pattern.DOTALL);
    private static final Pattern IMPORT_PATTERN = Pattern.compile("\\s*import\\s.*", Pattern.DOTALL);
    
	private ISourceViewer sourceViewer;
	private DRLRuleEditor editor;
	private IDocument document;
    private boolean folding;

	public DRLReconcilingStrategy(ISourceViewer sourceViewer, final DRLRuleEditor editor) {
		this.sourceViewer = sourceViewer;
		this.editor = editor;
		IPreferenceStore preferenceStore = DroolsIDEPlugin.getDefault().getPreferenceStore();
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
        StringReader stringReader = new StringReader( input );
        BufferedReader bufferedReader = new BufferedReader( stringReader );
        try {
            int offset = 0;
            String st = bufferedReader.readLine();
            int start = -1;
            while ( st != null ) {
                Matcher matcher = RULE_PATTERN.matcher(st);
                if (matcher.matches()) {
                	start = offset + matcher.start();
                	offset += st.length() + System.getProperty("line.separator").length(); // + for the newline
                    st = bufferedReader.readLine();
                	while (st != null) {
                		Matcher matcher2 = END_PATTERN.matcher(st);
                		if (matcher2.matches()) {
                			int end = offset + matcher2.end();
                            offset += st.length() + System.getProperty("line.separator").length(); // + for the newline
                            st = bufferedReader.readLine();
                			positions.add(new Position(start, end - start + (st == null ? 0 : System.getProperty("line.separator").length()))); // + for the newline
                			break;
                		}
                        offset += st.length() + System.getProperty("line.separator").length(); // + for the newline
                        st = bufferedReader.readLine();
	                }
                } else {
                	matcher = QUERY_PATTERN.matcher(st);
                    if (matcher.matches()) {
                    	start = offset + matcher.start();
                    	offset += st.length() + System.getProperty("line.separator").length(); // + for the newline
                        st = bufferedReader.readLine();
                    	while (st != null) {
                    		Matcher matcher2 = END_PATTERN.matcher(st);
                    		if (matcher2.matches()) {
                    			int end = offset + matcher2.end();
                                offset += st.length() + System.getProperty("line.separator").length(); // + for the newline
                                st = bufferedReader.readLine();
                    			positions.add(new Position(start, end - start + (st == null ? 0 : System.getProperty("line.separator").length()))); // + for the newline
                    			break;
                    		}
                            offset += st.length() + System.getProperty("line.separator").length(); // + for the newline
                            st = bufferedReader.readLine();
    	                }
                    } else {
	                	matcher = FUNCTION_PATTERN.matcher(st);
	                    if (matcher.matches()) {
	                    	start = offset + matcher.start();
	                    	int nbOpenBrackets = 1;
	                    	offset += st.length() + System.getProperty("line.separator").length(); // + for the newline
                            st = bufferedReader.readLine();
	                    	while (st != null) {
	                    		byte[] bytes = st.getBytes();
	                    		for (int i = 0; i < bytes.length; i++) {
	                    			if (bytes[i] == '{') {
	                    				nbOpenBrackets++;
	                    			} else if (bytes[i] == '}') {
	                    				if (--nbOpenBrackets == 0) {
	    	                    			int end = offset + i + 1;
	    		                            offset += st.length() + System.getProperty("line.separator").length(); // + for the newline
	    		                            st = bufferedReader.readLine();
	    	                    			positions.add(new Position(start, end - start + (st == null ? 0 : System.getProperty("line.separator").length()))); // + for the newline
	    	                    			break;
	                    				}
	                    			}
	                    		}
	                    		if (nbOpenBrackets == 0) {
	                    			break;
	                    		}
	                            offset += st.length() + System.getProperty("line.separator").length(); // + for the newline
	                            st = bufferedReader.readLine();
	    	                }
	                    } else {
		                	matcher = IMPORT_PATTERN.matcher(st);
		                    if (matcher.matches()) {
		                    	start = offset + matcher.start();
		                    	offset += st.length() + System.getProperty("line.separator").length(); // + for the newline
	                            st = bufferedReader.readLine();
		                    	while (st != null) {
		                    		Matcher matcher2 = IMPORT_PATTERN.matcher(st);
		                    		if (!matcher2.matches()) {
		                    			int end = offset;
		                    			positions.add(new Position(start, end - start));
		                    			break;
		                    		}
		                            offset += st.length() + System.getProperty("line.separator").length(); // + for the newline
		                            st = bufferedReader.readLine();
		    	                }
		                    } else {
		                    	offset += st.length() + System.getProperty("line.separator").length(); // + for the newline
	                            st = bufferedReader.readLine();
		                    }
	                    }
                    }
                }
            }
        } catch ( IOException e ) {
        	// do nothing
        }

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				editor.updateFoldingStructure(positions);
			}
		});
	}

}