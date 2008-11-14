package org.drools.eclipse.editors.completion;

import java.util.Comparator;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

/**
 * Holds a completion proposal to be popped up.
 *
 * @author Michael Neale
 *
 */
public class RuleCompletionProposal
    implements
    ICompletionProposal {

    private String content;
    private String display;
    private int    replacementOffset;
    private int    replacementLength;
    private int    cursorPosition;
    private Image  image;
    private int    priority;

    /** This is used when the stuff that is displayed, is the stuff that is used. */
    public RuleCompletionProposal(int replacementOffset,
                                  int replacementLength,
                                  String content) {
        this( replacementOffset,
              replacementLength,
              content,
              content );
    }

    /** This is used when a different display value is shown to what is put in when selected. */
    public RuleCompletionProposal(int replacementOffset,
                                  int replacementLength,
                                  String display,
                                  String content) {
        this( replacementOffset,
              replacementLength,
              display,
              content,
              content.length() );
    }

    /** Also allows an icon to be used */
    public RuleCompletionProposal(int replacementOffset,
                                  int replacementLength,
                                  String display,
                                  String content,
                                  Image image) {
        this( replacementOffset,
              replacementLength,
              display,
              content,
              content.length(),
              image );
    }

    public RuleCompletionProposal(int replacementOffset,
                                  int replacementLength,
                                  String display,
                                  String content,
                                  int cursorPosition) {
        this( replacementOffset,
              replacementLength,
              display,
              content,
              cursorPosition,
              null );
    }

    /** This is used when a different display value is shown to what is put in when selected. */
    public RuleCompletionProposal(int replacementOffset,
                                  int replacementLength,
                                  String display,
                                  String content,
                                  int cursorPosition,
                                  Image image) {
        this.replacementOffset = replacementOffset;
        this.replacementLength = replacementLength;
        this.content = content;
        this.display = display;
        this.cursorPosition = cursorPosition;
        this.image = image;
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

    public String getContent() {
        return content;
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

    //TODO:fixme now that we mix JDT and own proposals, comparison is all wrong, resulting in wrong ordering of mixed proposals (such as with mvel
    public static class RuleCompletionProposalComparator
        implements
        Comparator {
        public int compare(Object arg0,
                           Object arg1) {
            if ( arg0 instanceof RuleCompletionProposal ) {
                if ( arg1 instanceof RuleCompletionProposal ) {
                    RuleCompletionProposal prop0 = (RuleCompletionProposal) arg0;
                    RuleCompletionProposal prop1 = (RuleCompletionProposal) arg1;
                    if ( prop0.getPriority() == prop1.getPriority() ) {
                        return prop0.display.compareTo( prop1.display );
                    } else if ( prop0.getPriority() > prop1.getPriority() ) {
                        return -1;
                    } else {
                        return 1;
                    }
                } else {
                    return -1;
                }
            } else {
                if ( arg1 instanceof RuleCompletionProposal ) {
                    return 1;
                }
                return 0;
            }
        }
    }

    public void apply(IDocument document) {
        try {
            document.replace( replacementOffset,
                              replacementLength,
                              content );
        } catch ( BadLocationException x ) {
            // ignore
        }
    }

    public String getAdditionalProposalInfo() {
        return null;
    }

    public IContextInformation getContextInformation() {
        return null;
    }

    public String getDisplayString() {
        if ( display != null ) {
            return display;
        }
        return content;
    }

    public int hashCode() {
        final int PRIME = 31;
        int result = 1;
        result = PRIME * result + ((content == null) ? 0 : content.hashCode());
        result = PRIME * result + ((display == null) ? 0 : display.hashCode());
        return result;
    }

    public boolean equals(Object obj) {
        if ( this == obj ) return true;
        if ( obj == null ) return false;
        if ( getClass() != obj.getClass() ) return false;
        final RuleCompletionProposal other = (RuleCompletionProposal) obj;
        if ( content == null ) {
            if ( other.content != null ) return false;
        } else if ( !content.equals( other.content ) ) return false;
        if ( display == null ) {
            if ( other.display != null ) return false;
        } else if ( !display.equals( other.display ) ) return false;
        return true;
    }

    public Point getSelection(IDocument document) {
        return new Point( replacementOffset + cursorPosition,
                          0 );
    }
}
