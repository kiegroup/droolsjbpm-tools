package org.drools.eclipse.editors;

import org.eclipse.jface.text.source.Annotation;

public class DRLProblemAnnotation extends Annotation {

	public static final String ERROR = "org.drools.eclipse.editors.error_annotation";
	
	public DRLProblemAnnotation(String text) {
		super(ERROR, false, text);
	}

}
