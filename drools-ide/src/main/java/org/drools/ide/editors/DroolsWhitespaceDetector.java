package org.drools.ide.editors;

import org.eclipse.jface.text.rules.IWhitespaceDetector;

/**
 * The Drools whitespace detector.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class DroolsWhitespaceDetector implements IWhitespaceDetector {

	public boolean isWhitespace(char c) {
		return (c == ' ' || c == '\t' || c == '\n' || c == '\r');
	}
}
