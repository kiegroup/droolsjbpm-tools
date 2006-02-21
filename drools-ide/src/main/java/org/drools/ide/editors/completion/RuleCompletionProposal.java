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
    

    
    /** This is used when a different display value is shown to what is put in when selected. */
    public RuleCompletionProposal(String display, String content) {
        this.content = content;
        this.display = display;
    }

    /** This is used when the stuff that is displayed, is the stuff that is used. */
    public RuleCompletionProposal(String content) {
        this.content = content;
        this.display = content;
    }

    public String getContent() {
        return content;
    }

    public String getDisplay() {
        return display;
    }
    
    public String toString() {
        return content;
    }
    
}
