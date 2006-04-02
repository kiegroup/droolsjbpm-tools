package org.drools.ide.editors.completion;

/**
 * Holds a completion proposal to be popped up.
 * 
 * @author Michael Neale
 *
 */
public class RuleCompletionProposal {
	
    private String content;
    private String display;
    private int offset;
    
    /** This is used when the stuff that is displayed, is the stuff that is used. */
    public RuleCompletionProposal(String content) {
        this(content, content);
    }

    /** This is used when a different display value is shown to what is put in when selected. */
    public RuleCompletionProposal(String display, String content) {
        this(display, content, content.length());
    }

    /** This is used when a different display value is shown to what is put in when selected. */
    public RuleCompletionProposal(String display, String content, int offset) {
        this.content = content;
        this.display = display;
        this.offset = offset;
    }

    public String getContent() {
        return content;
    }

    public String getDisplay() {
        return display;
    }
    
    public int getOffset() {
    	return offset;
    }
    
    public String toString() {
        return content;
    }
    
}
