package org.drools.eclipse.editors.completion;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.ui.IEditorPart;

/**
 *
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public abstract class AbstractCompletionProcessor implements IContentAssistProcessor {

    private IEditorPart editor;

    public AbstractCompletionProcessor(IEditorPart editor) {
    	this.editor = editor;
    }

    protected IEditorPart getEditor() {
    	return editor;
    }

	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
    	List proposals = getCompletionProposals(viewer, documentOffset);
    	if (proposals == null) {
    		return new ICompletionProposal[0];
    	}
        Collections.sort(proposals, new RuleCompletionProposal.RuleCompletionProposalComparator());
        return (ICompletionProposal[]) proposals.toArray(new ICompletionProposal[proposals.size()]);
	}

	/**
	 * Returns a list of RuleCompletionProposals.
	 *
	 * @param viewer
	 * @param documentOffset
	 * @return
	 */
	protected abstract List getCompletionProposals(ITextViewer viewer, int documentOffset);

    /**
     *  Filter out the proposals whose content does not start with the given prefix.
     */
    protected static void filterProposalsOnPrefix(String prefix, List props) {
    	if ( prefix != null && prefix.trim().length() > 0 ) {
    		Iterator iterator = props.iterator();
            String prefixLc = prefix.toLowerCase();
    		while ( iterator.hasNext() ) {
                ICompletionProposal item = (ICompletionProposal) iterator.next();
                String content = item.getDisplayString().toLowerCase();
                if ( !content.toLowerCase().startsWith( prefixLc ) ) {
                    iterator.remove();
                }
    		}
    	}
    }

    /**
     * Read some text from behind the cursor position.
     * This provides context to both filter what is shown based
     * on what the user has typed in, and also to provide more information for the
     * list of suggestions based on context.
     */
    protected String readBackwards(int documentOffset, IDocument doc) throws BadLocationException {
        int startPart = doc.getPartition(documentOffset).getOffset();
        String prefix = doc.get(startPart, documentOffset - startPart);
        return prefix;
    }

	/*
	 * @see IContentAssistProcessor
	 */
	public char[] getCompletionProposalAutoActivationCharacters() {
		return null;
	}

	/*
	 * @see IContentAssistProcessor
	 */
	public char[] getContextInformationAutoActivationCharacters() {
		return null;
	}

	/*
	 * @see IContentAssistProcessor
	 */
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

	/*
	 * @see IContentAssistProcessor
	 */
	public IContextInformation[] computeContextInformation(ITextViewer viewer, int documentOffset) {
		return null;
	}

	/*
	 * @see IContentAssistProcessor
	 */
	public String getErrorMessage() {
		return null;
	}
}
