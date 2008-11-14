package org.drools.eclipse.editors.scanners;

import org.eclipse.jface.text.rules.IWhitespaceDetector;

/**
 * A rule aware white space detector.
 */
public class WhitespaceDetector implements IWhitespaceDetector {

    /* (non-Javadoc)
     * Method declared on IWhitespaceDetector
     */
    public boolean isWhitespace(char character) {
        return Character.isWhitespace(character);
    }
}

