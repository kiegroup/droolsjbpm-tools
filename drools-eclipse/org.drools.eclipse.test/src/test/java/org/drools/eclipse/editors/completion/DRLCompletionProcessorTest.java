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

    public void testPrefixFiltering_FiltersBasedOnDisplayedStringNotContent() {
        List list = new ArrayList();
        list.add(new RuleCompletionProposal(0, 0, "abcd", "zxyz"));
        list.add(new RuleCompletionProposal(0, 0, "azard","good"));
        list.add(new RuleCompletionProposal(0, 0, "art","apple"));
        list.add(new RuleCompletionProposal(0, 0, "spe", "ape"));

        DefaultCompletionProcessor.filterProposalsOnPrefix("a", list);
        assertEquals(3, list.size());
        assertEquals("zxyz", list.get(0).toString());
        assertEquals("good", list.get(1).toString());
        assertEquals("apple", list.get(2).toString());

    }

    public void testPrefixFiltering_FiltersAllWhenThereisNoMatches() {
        List list = new ArrayList();
        list = new ArrayList();
        list.add(new RuleCompletionProposal(0, 0, "aardvark", "something"));
        list.add(new RuleCompletionProposal(0, 0, "smeg"));
        list.add(new RuleCompletionProposal(0, 0, "apple"));
        list.add(new RuleCompletionProposal(0, 0, "ape", "zzzzz"));
        DefaultCompletionProcessor.filterProposalsOnPrefix("xzyz", list);
        assertEquals(0, list.size());

    }

    public void testPrefixFiltering_IgnoreCase() {
        List list = new ArrayList();
        list = new ArrayList();
        list.add(new RuleCompletionProposal(0, 0, "ART"));
        list.add(new RuleCompletionProposal(0, 0, "art"));
        list.add(new RuleCompletionProposal(0, 0, "aRT"));
        list.add(new RuleCompletionProposal(0, 0, "Art", "zzzzz"));
        DefaultCompletionProcessor.filterProposalsOnPrefix("art", list);
        assertEquals(4, list.size());

        DefaultCompletionProcessor.filterProposalsOnPrefix("ART", list);
        assertEquals(4, list.size());

    }

}
