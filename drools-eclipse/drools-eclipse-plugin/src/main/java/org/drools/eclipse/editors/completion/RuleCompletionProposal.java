package org.drools.eclipse.editors.completion;

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
    private int priority;
    
    /** This is used when the stuff that is displayed, is the stuff that is used. */
    public RuleCompletionProposal(int replacementLength, String content) {
        this(replacementLength, content, content);
    }

    /** This is used when a different display value is shown to what is put in when selected. */
    public RuleCompletionProposal(int replacementLength, String display, String content) {
        this(replacementLength, display, content, content.length());
    }
    
    /** Also allows an icon to be used */
    public RuleCompletionProposal(int replacementLength, String display, String content, Image image) {
        this(replacementLength, display, content, content.length(), image);
    }

    public RuleCompletionProposal(int replacementLength, String display, String content, int cursorPosition) {
    	this(replacementLength, display, content, cursorPosition, null);
    }
    
    /** This is used when a different display value is shown to what is put in when selected. */
    public RuleCompletionProposal(int replacementLength, String display, String content, int cursorPosition, Image image) {
    	this.replacementLength = replacementLength;
        this.content = content;
        this.display = display;
        this.cursorPosition = cursorPosition;
        this.image = image;
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
    
	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}
    
    public String toString() {
        return content;
    }
    
    public static class RuleCompletionProposalComparator implements Comparator {
		public int compare(Object arg0, Object arg1) {
			RuleCompletionProposal prop0 = (RuleCompletionProposal) arg0;
			RuleCompletionProposal prop1 = (RuleCompletionProposal) arg1;
			if (prop0.getPriority() == prop1.getPriority()) {
				return prop0.getDisplay().compareTo(prop1.getDisplay());
			} else if (prop0.getPriority() > prop1.getPriority()) {
				return -1;
			} else {
				return 1;
			}
		}
    }
}
