/**
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

import java.util.ArrayList;
import java.util.Collection;

import org.drools.rule.builder.dialect.java.KnowledgeHelperFixer;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class RuleCompletionProcessorTest {

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

    @Test
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

    @Test
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

    @Test
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

    @Test
    public void testIsStartOfDialectExpression() {
        //for now inside a method start, we are not starting a new expression for completion purpose
        String s = "System.out.println(\"\");\r\n" + "  update(";
        assertFalse( CompletionUtil.isStartOfDialectExpression( s ) );
    }

}
