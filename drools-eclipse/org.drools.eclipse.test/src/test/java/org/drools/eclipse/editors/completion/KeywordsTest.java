package org.drools.eclipse.editors.completion;

import org.drools.eclipse.editors.Keywords;

import junit.framework.TestCase;

public class KeywordsTest extends TestCase {

    public void testAll() {
        Keywords keys = Keywords.getInstance();
        String[] all = keys.getAllDroolsKeywords();
        assertTrue(all.length > 0);
        assertEquals("when", all[0]);

    }
    
}
