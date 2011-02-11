/*
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

package org.drools.eclipse.dsl.editor;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.drools.eclipse.dsl.editor.DSLAdapter;

/**
 * 
 */
public class DSLAdapterTest {
    
    @Test
    public void testFindExpander() {
        
        StringBuffer buf = largeString();
        
        String pat = "\nexpander  \t abc.dsl ";
        
        assertEquals("abc.dsl", DSLAdapter.findDSLConfigName( pat ));
        
        
        assertEquals("abc.dsl", DSLAdapter.findDSLConfigName( buf.toString() ));
        
        assertEquals(null, DSLAdapter.findDSLConfigName( "abc /n/n" ));
        
        assertEquals(null, DSLAdapter.findDSLConfigName( "fdfds" ));
        
    }
    
    @Test
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
