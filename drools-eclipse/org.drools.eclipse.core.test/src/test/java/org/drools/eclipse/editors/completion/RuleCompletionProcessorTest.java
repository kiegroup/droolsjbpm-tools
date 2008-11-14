package org.drools.eclipse.editors.completion;

import java.util.ArrayList;
import java.util.Collection;

import org.drools.rule.builder.dialect.java.KnowledgeHelperFixer;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import junit.framework.TestCase;

public class RuleCompletionProcessorTest extends TestCase {

    class MockCompletionProposal
        implements
        ICompletionProposal {

        String displayString;

        public MockCompletionProposal(String displayString) {
            this.displayString = displayString;
        }

        public void apply(IDocument document) {
            // TODO Auto-generated method stub

        }

        public String getAdditionalProposalInfo() {
            // TODO Auto-generated method stub
            return null;
        }

        public IContextInformation getContextInformation() {
            // TODO Auto-generated method stub
            return null;
        }

        public String getDisplayString() {
            return displayString;
        }

        public Image getImage() {
            // TODO Auto-generated method stub
            return null;
        }

        public Point getSelection(IDocument document) {
            // TODO Auto-generated method stub
            return null;
        }

    }

    public void testContainsProposal() {
        Collection proposals = new ArrayList();

        MockCompletionProposal c1 = new MockCompletionProposal( "getName() Object - MyObject" );
        proposals.add( c1 );

        String newProposal = "getName() String - CompletionProposal";

        assertTrue( RuleCompletionProcessor.containsProposal( proposals,
                                                              newProposal ) );

        MockCompletionProposal c2 = new MockCompletionProposal( "getNoName() Object - MyObject" );
        proposals.add( c2 );
        assertFalse( RuleCompletionProcessor.containsProposal( proposals,
                                                               "getNoName" ) );
    }

    public void testAddAllNewProposals() {
        ArrayList proposals = new ArrayList();
        MockCompletionProposal c = new MockCompletionProposal( "getName() Object - MyObject" );
        proposals.add( c );

        ArrayList newProposals = new ArrayList();
        MockCompletionProposal c1 = new MockCompletionProposal( "getName() Objectw - MyObject" );
        newProposals.add( c1 );
        MockCompletionProposal c2 = new MockCompletionProposal( "getNoName() Object - MyObject" );
        newProposals.add( c2 );
        MockCompletionProposal c3 = new MockCompletionProposal( "getNoName() NoObject - MyObject" );
        newProposals.add( c3 );

        RuleCompletionProcessor.addAllNewProposals( proposals,
                                                    newProposals );

        assertTrue( proposals.size() == 2 );

        ICompletionProposal prop = (ICompletionProposal) proposals.get( 1 );
        assertEquals( "getNoName() Object - MyObject",
                      prop.getDisplayString() );
    }

    public void testProcessMacros() {
        String text = "";
        final String[] functions = new String[]{"update", "retract", "insert", "insertLogical"};
        for ( int i = 0; i < functions.length; i++ ) {
            String string = functions[i];
            String expected = "drools." + string;

            assertEquals( expected,
                          new KnowledgeHelperFixer().fix( string ) );

        }
    }

    public void testIsStartOfDialectExpression() {
        //for now inside a method start, we are not starting a new expression for completion purpose
        String s = "System.out.println(\"\");\r\n" + "  update(";
        assertFalse( CompletionUtil.isStartOfDialectExpression( s ) );
    }

}
