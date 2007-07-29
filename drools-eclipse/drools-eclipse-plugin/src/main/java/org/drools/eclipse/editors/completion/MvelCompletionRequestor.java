package org.drools.eclipse.editors.completion;

import java.util.Collection;

import org.eclipse.jdt.core.CompletionProposal;
import org.eclipse.jdt.core.CompletionRequestor;

public class MvelCompletionRequestor extends CompletionRequestor {
    private final String     prefix;
    private final String     text;
    private final Collection list;
    private final int        documentOffset;

    public MvelCompletionRequestor(String prefix,
    							   int documentOffset,
                                   String text,
                                   Collection list) {
        this.prefix = prefix;
        this.text = text;
        this.list = list;
        this.documentOffset = documentOffset;
    }

    public void accept(CompletionProposal proposal) {
        // TODO set other proposal properties too (display name, icon, ...)
        String completion = new String( proposal.getCompletion() );
        RuleCompletionProposal prop = new RuleCompletionProposal( 
        		documentOffset - prefix.length(), prefix.length(), completion );;

        switch ( proposal.getKind() ) {
            case CompletionProposal.LOCAL_VARIABLE_REF :
                prop.setImage( DefaultCompletionProcessor.VARIABLE_ICON );
                break;

            case CompletionProposal.METHOD_REF :
                // TODO: Object methods are proposed when in the start of a line

                //get the eventual property name for that method name and signature
                String propertyOrMethodName = CompletionUtil.getPropertyName( completion,
                                                                              proposal.getSignature() );
                //is the completion for a bean accessor?
                boolean isAccessor = completion.equals( propertyOrMethodName );

                prop = new RuleCompletionProposal(
        			documentOffset - prefix.length(), prefix.length(), propertyOrMethodName );
                boolean startOfNewStatement = CompletionUtil.isStartOfNewStatement( text,
                                                                                    prefix );
                if ( startOfNewStatement ) {
                    //ignore non accessor methods when starting a new statement
                    if ( isAccessor ) {
                        prop.setImage( DefaultCompletionProcessor.VARIABLE_ICON );
                    }
                } else {
                    if ( isAccessor ) {
                        prop.setImage( DefaultCompletionProcessor.VARIABLE_ICON );
                    } else {
                        prop.setImage( DefaultCompletionProcessor.METHOD_ICON );
                    }
                }

                break;

            default :
        }
        list.add( prop );
    }
}