package org.drools.eclipse.wizard.rule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import junit.framework.TestCase;

public class DRLGenTemplateTest extends TestCase {
    
    public void testNewRule() throws Exception {
        DRLGenerator gen = new DRLGenerator();
        InputStream result = gen.generateRule("myPackage", getClass().getResourceAsStream( "new_rule.drl.template" ));
        
        assertNotNull(result);
        StringBuffer buf = getResult( result );
        assertTrue(buf.toString().indexOf( "package myPackage" ) > -1);
        assertFalse(buf.toString().indexOf( "$date$" ) > -1);
        
    }
    
    public void testNewPackage() throws Exception {
        DRLGenerator gen = new DRLGenerator();
        InputStream result = gen.generatePackage("myPackage", true, true, getClass().getResourceAsStream( "new_rule.drl.template" ));
        
        assertNotNull(result);
        StringBuffer buf = getResult( result );
        assertTrue(buf.toString().indexOf( "package myPackage" ) > -1);
        assertFalse(buf.toString().indexOf( "$date$" ) > -1);
        assertFalse(buf.toString().indexOf( "$expander$" ) > -1);
        assertFalse(buf.toString().indexOf( "$functions$" ) > -1);
     
        
        
    }
    

    private StringBuffer getResult(InputStream result) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(result));
        String line = null;
        StringBuffer buf = new StringBuffer();
        while ((line = reader.readLine())  != null) {
            buf.append(line + "\n");
        }
        return buf;
    }
    
    
}
