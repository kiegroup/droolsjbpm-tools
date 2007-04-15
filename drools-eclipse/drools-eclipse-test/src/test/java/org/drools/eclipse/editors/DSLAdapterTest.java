package org.drools.eclipse.editors;

import junit.framework.TestCase;

/**
 * 
 * @author Michael Neale
 */
public class DSLAdapterTest extends TestCase {
    
    public void testFindExpander() {
        if ( true ) {
            fail( "fail on purpose" );
        }
        
        StringBuffer buf = largeString();
        
        String pat = "\nexpander  \t abc.dsl";
        
        DSLAdapter ad = new DSLAdapter();
        assertEquals("abc.dsl", DSLAdapter.findDSLConfigName( pat ));
        
        
        assertEquals("abc.dsl", DSLAdapter.findDSLConfigName( buf.toString() ));
        
        assertEquals(null, DSLAdapter.findDSLConfigName( "abc /n/n" ));
        
        ad = new DSLAdapter("fdfds", null);
        assertEquals(null, ad.getDSLConfigName());
        
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
                buf.append( "\nuse expander abc.dsl  \n" );
            }
            
            buf.append( "\n" );
            buf.append( " fdsfdsfds && " + i);
            
        }
        return buf;
    }
    
}
