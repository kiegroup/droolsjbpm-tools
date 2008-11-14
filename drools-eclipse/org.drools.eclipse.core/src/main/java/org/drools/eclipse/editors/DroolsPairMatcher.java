package org.drools.eclipse.editors;

import java.util.Stack;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.source.ICharacterPairMatcher;

public final class DroolsPairMatcher implements ICharacterPairMatcher {

	private int anchor;
	private static final char[] leftChars = new char[] { '(', '{', '[' };
	private static final char[] rightChars = new char[] { ')', '}', ']' };

	public IRegion match(IDocument document, int offset) {
        if (offset <= 0) {
        	return null;
        }
        try {
		    char c = document.getChar(offset - 1);
		    for (int i = 0; i < rightChars.length; i++) {
			    if (c == rightChars[i]) {
			        return searchLeft(document, offset, rightChars[i], leftChars[i]);
			    }
			    if (c == leftChars[i]) {
			        return searchRight(document, offset, rightChars[i], leftChars[i]);
			    }
		    }
        } catch (BadLocationException e) {
        	DroolsEclipsePlugin.log(e);
        }
        return null;
    }

	private IRegion searchRight(IDocument document, int offset, char rightChar, char leftChar) throws BadLocationException {
        Stack stack = new Stack();
        anchor = ICharacterPairMatcher.LEFT;
        char[] chars = document.get(offset, document.getLength() - offset).toCharArray();
        for (int i = 0; i < chars.length; i++) {
	        if (chars[i] == leftChar) {
	            stack.push(new Character(chars[i]));
	            continue;
	        }
	        if (chars[i] == rightChar) {
	        	if (stack.isEmpty()) {
		            return new Region(offset - 1, i + 2);
		        } else {
		        	stack.pop();
		        }
	        }
        }
        return null;
    }

	private IRegion searchLeft(IDocument document, int offset, char rightChar, char leftChar)
			throws BadLocationException {
		Stack stack = new Stack();
		anchor = ICharacterPairMatcher.RIGHT;
		char[] chars = document.get(0, offset - 1).toCharArray();
        for (int i = chars.length - 1; i >= 0; i--) {
			if (chars[i] == rightChar) {
				stack.push(new Character(chars[i]));
				continue;
			}
			if (chars[i] == leftChar) {
				if (stack.isEmpty()) {
					return new Region(i, offset - i);
				} else {
					stack.pop();
				}
			}
        }
		return null;
	}

	public int getAnchor() {
		return anchor;
	}

	public void dispose() {
	}

	public void clear() {
	}

}
