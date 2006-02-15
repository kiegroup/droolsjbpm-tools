package org.drools.ide.editors.completion;

import org.drools.ide.editors.Keywords;

import junit.framework.TestCase;

public class KeywordsTest extends TestCase {

    public void testAll() {
        Keywords keys = Keywords.getInstance();
        String[] all = keys.getAll();
        assertTrue(all.length > 0);
        String[] all2 = keys.getAll();
        assertEquals(all2, all); //check caching
    }
    
}
