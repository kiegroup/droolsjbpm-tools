package org.drools.ide.editors.completion;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class DRLCompletionProcessorTest extends TestCase {

    public void testLookBehind() {
        DefaultCompletionProcessor proc = new DefaultCompletionProcessor(null);
        
        assertEquals("something", proc.stripWhiteSpace(" something"));
        assertEquals("another", proc.stripWhiteSpace("another"));
        
        String s = "rule something \n\nwhen";
        assertEquals("when", proc.stripWhiteSpace(s));
        
    }
    
    public void testPrefixExist() {
        DefaultCompletionProcessor proc = new DefaultCompletionProcessor(null);
        assertFalse(proc.doesPrefixExist("something "));
        assertTrue(proc.doesPrefixExist("y"));
    }
    
    public void testPrefixFiltering() {
        DefaultCompletionProcessor proc = new DefaultCompletionProcessor(null);

        List raw = new ArrayList();
        raw.add(new RuleCompletionProposal("aardvark", "something"));
        raw.add(new RuleCompletionProposal("smeg"));
        raw.add(new RuleCompletionProposal("apple"));
        raw.add(new RuleCompletionProposal("ape", "zzzzz"));
        
        List result = proc.filterList(raw, "a");
        assertEquals(3, result.size());
        assertEquals("something", result.get(0).toString());
        assertEquals("apple", result.get(1).toString());
        assertEquals("zzzzz", result.get(2).toString());

        
        result = proc.filterList(raw, "xzyz");
        assertEquals(0, result.size());
        
        
    }
    
}
