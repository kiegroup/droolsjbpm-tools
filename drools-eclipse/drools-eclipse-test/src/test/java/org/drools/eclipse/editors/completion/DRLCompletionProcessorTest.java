package org.drools.eclipse.editors.completion;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class DRLCompletionProcessorTest extends TestCase {

    public void testLookBehind() {
        assertEquals("something", CompletionUtil.stripLastWord(" something"));
        assertEquals("another", CompletionUtil.stripLastWord("another"));
        
        String s = "rule something \n\nwhen";
        assertEquals("when", CompletionUtil.stripLastWord(s));
    }
    
    public void testPrefixFiltering() {
        DefaultCompletionProcessor proc = new DefaultCompletionProcessor(null);

        List list = new ArrayList();
        list.add(new RuleCompletionProposal(0, "aardvark", "something"));
        list.add(new RuleCompletionProposal(0, "smeg"));
        list.add(new RuleCompletionProposal(0, "apple"));
        list.add(new RuleCompletionProposal(0, "ape", "ape"));
        
        proc.filterProposalsOnPrefix("a", list);
        assertEquals(2, list.size());
        assertEquals("apple", list.get(0).toString());
        assertEquals("ape", list.get(1).toString());

        
        list = new ArrayList();
        list.add(new RuleCompletionProposal(0, "aardvark", "something"));
        list.add(new RuleCompletionProposal(0, "smeg"));
        list.add(new RuleCompletionProposal(0, "apple"));
        list.add(new RuleCompletionProposal(0, "ape", "zzzzz"));
        proc.filterProposalsOnPrefix("xzyz", list);
        assertEquals(0, list.size());
    }
    
}
