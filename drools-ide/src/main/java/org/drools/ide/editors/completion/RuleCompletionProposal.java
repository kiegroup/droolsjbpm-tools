package org.drools.ide.editors.completion;

import java.util.Comparator;

import org.eclipse.swt.graphics.Image;

/**
 * Holds a completion proposal to be popped up.
 * 
 * @author Michael Neale
 *
 */
public class RuleCompletionProposal {
	
    private String content;
    private String display;
    private int replacementLength;
    private int cursorPosition;
    private Image image;
    
    /** This is used when the stuff that is displayed, is the stuff that is used. */
    public RuleCompletionProposal(int replacementLength, String content) {
        this(replacementLength, content, content);
    }

    /** This is used when a different display value is shown to what is put in when selected. */
    public RuleCompletionProposal(int replacementLength, String display, String content) {
        this(replacementLength, display, content, content.length());
    }

    /** This is used when a different display value is shown to what is put in when selected. */
    public RuleCompletionProposal(int replacementLength, String display, String content, int cursorPosition) {
    	this.replacementLength = replacementLength;
        this.content = content;
        this.display = display;
        this.cursorPosition = cursorPosition;
    }

    public String getContent() {
        return content;
    }

    public String getDisplay() {
        return display;
    }
    
    public int getReplacementLength() {
		return replacementLength;
	}

	public int getCursorPosition() {
    	return cursorPosition;
    }
    
	public Image getImage() {
		return image;
	}

	public void setImage(Image image) {
		this.image = image;
	}
    
    public String toString() {
        return content;
    }
    
    public static class RuleCompletionProposalComparator implements Comparator {
		public int compare(Object arg0, Object arg1) {
			return ((RuleCompletionProposal) arg0).getDisplay()
				.compareTo(((RuleCompletionProposal) arg1).getDisplay());
		}
    }
}
