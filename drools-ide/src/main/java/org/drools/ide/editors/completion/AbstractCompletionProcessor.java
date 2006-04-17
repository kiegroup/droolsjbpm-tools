package org.drools.ide.editors.completion;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;
import org.eclipse.ui.part.EditorPart;


/**
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public abstract class AbstractCompletionProcessor implements IContentAssistProcessor {

    private EditorPart editor;
   
    public AbstractCompletionProcessor(EditorPart editor) {
    	this.editor = editor;
    }
    
    protected EditorPart getEditor() {
    	return editor;
    }
    
	public ICompletionProposal[] computeCompletionProposals(ITextViewer viewer, int documentOffset) {
    	List proposals = getCompletionProposals(viewer, documentOffset);
    	if (proposals == null) {
    		return new ICompletionProposal[0];
    	}
        Collections.sort(proposals, new RuleCompletionProposal.RuleCompletionProposalComparator());
        ICompletionProposal[] result = makeProposals(proposals, documentOffset);
        return result;
	}
	
	/**
	 * Returns a list of RuleCompletionProposals.
	 * 
	 * @param viewer
	 * @param documentOffset
	 * @return
	 */
	protected abstract List getCompletionProposals(ITextViewer viewer, int documentOffset);

    protected ICompletionProposal[] makeProposals(List props, int documentOffset) {
        ICompletionProposal[] result = new ICompletionProposal[props.size()];
        Iterator iterator = props.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            RuleCompletionProposal proposal = (RuleCompletionProposal) iterator.next();
            result[i++] = new CompletionProposal(proposal.getContent(), 
        		documentOffset - proposal.getReplacementLength(), proposal.getReplacementLength(), 
                proposal.getCursorPosition(), proposal.getImage(), proposal.getDisplay(), null, null);
        }        
        return result;
    }

    /**
     *  Filter out the proposals whose content does not start with the given prefix.
     */
    protected void filterProposalsOnPrefix(String prefix, List props) {
    	if (prefix != null) {
    		Iterator iterator = props.iterator();
    		while ( iterator.hasNext() ) {
    			RuleCompletionProposal item = (RuleCompletionProposal) iterator.next();
    			if (!item.getContent().startsWith(prefix)) {
    				iterator.remove();
    			}
    		}
    	}
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
