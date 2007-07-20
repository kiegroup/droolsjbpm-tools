package org.drools.eclipse.editors.completion;

import java.util.Collection;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;

public class JavaCompletionRequestor extends CompletionRequestor {
    private final String prefix;
    private final String text;
    private final Collection   list;

    public JavaCompletionRequestor(String prefix,
                                   String text,
                                   Collection list) {
        this.prefix = prefix;
        this.text = text;
        this.list = list;
    }

    public void accept(CompletionProposal proposal) {
        // TODO set other proposal properties too (display name, icon, ...)
        String completion = new String( proposal.getCompletion() );
        RuleCompletionProposal prop = new RuleCompletionProposal( prefix.length(),
                                                                  completion );

        switch ( proposal.getKind() ) {
            case CompletionProposal.LOCAL_VARIABLE_REF :
                prop.setImage( DefaultCompletionProcessor.VARIABLE_ICON );
                break;
            case CompletionProposal.METHOD_REF :
                // TODO: Object methods are proposed when in the start of a line
                if ( CompletionUtil.isStartOfNewStatement( text,
                                                           prefix ) ) {
                    return;
                }
                prop.setImage( DefaultCompletionProcessor.METHOD_ICON );
                break;
            default :
        }
        list.add( prop );
    }
}