package org.drools.ide.editors.completion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.drools.ide.editors.completion.DefaultCompletionProcessor;

import junit.framework.TestCase;

public class DRLCompletionProcessorTest extends TestCase {

    public void testLookBehind() {
        DefaultCompletionProcessor proc = new DefaultCompletionProcessor();
        
        assertEquals("something", proc.stripWhiteSpace(" something"));
        assertEquals("another", proc.stripWhiteSpace("another"));
        
        String s = "rule something \n\nwhen";
        assertEquals("when", proc.stripWhiteSpace(s));
        
    }
    
    public void testPrefixExist() {
        DefaultCompletionProcessor proc = new DefaultCompletionProcessor();
        assertFalse(proc.doesPrefixExist("something "));
        assertTrue(proc.doesPrefixExist("y"));
    }
    
    public void testPrefixFiltering() {
        DefaultCompletionProcessor proc = new DefaultCompletionProcessor();

        List raw = new ArrayList();
        raw.add("aardvark");
        raw.add("smeg");
        raw.add("apple");
        raw.add("ape");
        
        List result = proc.filterList(raw, "a");
        assertEquals(3, result.size());
        assertEquals("aardvark", result.get(0));
        assertEquals("apple", result.get(1));
        assertEquals("ape", result.get(2));

        
        result = proc.filterList(raw, "xzyz");
        assertEquals(0, result.size());
        
        
    }
    
}
