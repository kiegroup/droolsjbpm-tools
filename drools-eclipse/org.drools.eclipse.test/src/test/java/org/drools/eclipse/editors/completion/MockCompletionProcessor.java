package org.drools.eclipse.editors.completion;

import java.util.List;

import org.eclipse.jface.text.ITextViewer;

public class MockCompletionProcessor extends AbstractCompletionProcessor {

    public MockCompletionProcessor() {
        super( null );
    }

    protected List getCompletionProposals(ITextViewer viewer,
                                          int documentOffset) {
        return null;
    }
}
