/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

package org.drools.eclipse.wizard.rule;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class DRLGenTemplateTest {
    
    @Test
    public void testNewRule() throws Exception {
        DRLGenerator gen = new DRLGenerator();
        InputStream result = gen.generateRule("myPackage", getClass().getResourceAsStream( "new_rule.drl.template" ));
        
        assertNotNull(result);
        StringBuffer buf = getResult( result );
        assertTrue(buf.toString().indexOf( "package myPackage" ) > -1);
        assertFalse(buf.toString().indexOf( "$date$" ) > -1);
        
    }
    
    @Test
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
