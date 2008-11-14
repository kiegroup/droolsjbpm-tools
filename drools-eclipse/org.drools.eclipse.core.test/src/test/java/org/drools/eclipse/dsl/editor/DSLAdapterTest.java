package org.drools.eclipse.dsl.editor;

import junit.framework.TestCase;

import org.drools.eclipse.dsl.editor.DSLAdapter;

/**
 * 
 * @author Michael Neale
 */
public class DSLAdapterTest extends TestCase {
    
    public void testFindExpander() {
        
        StringBuffer buf = largeString();
        
        String pat = "\nexpander  \t abc.dsl ";
        
        assertEquals("abc.dsl", DSLAdapter.findDSLConfigName( pat ));
        
        
        assertEquals("abc.dsl", DSLAdapter.findDSLConfigName( buf.toString() ));
        
        assertEquals(null, DSLAdapter.findDSLConfigName( "abc /n/n" ));
        
        assertEquals(null, DSLAdapter.findDSLConfigName( "fdfds" ));
        
    }
    
    public void testLoadGrammar() throws Exception {
        DSLAdapter ad = new DSLAdapter();
        
        ad.readConfig( this.getClass().getResourceAsStream( "test.dsl" ) );
        assertNotNull(ad.listConditionItems());
        assertNotNull(ad.listConsequenceItems());
    }

    private StringBuffer largeString() {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < 1000; i++) {
            if (i == 42) {
                buf.append( "\n   expander abc.dsl  \n" );
            }
            
            buf.append( "\n" );
            buf.append( " fdsfdsfds && " + i);
            
        }
        return buf;
    }
    
}
