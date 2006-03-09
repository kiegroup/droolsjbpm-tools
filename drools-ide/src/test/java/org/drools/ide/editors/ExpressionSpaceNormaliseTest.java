package org.drools.ide.editors;

import junit.framework.TestCase;

import org.drools.ide.dsl.editor.NLGrammarModel;

public class ExpressionSpaceNormaliseTest extends TestCase {

    public void testNormaliseSpaces() {
        String with = "This  has some  extra\t spaces.";
        String without = "This has some extra spaces.";
        
        assertEquals(without, NLGrammarModel.normaliseSpaces( with ));
        
        assertEquals(without, NLGrammarModel.normaliseSpaces( without ));
        
        assertEquals("smeg", NLGrammarModel.normaliseSpaces( "smeg" ));
        
    }


    
}
