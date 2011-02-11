/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
