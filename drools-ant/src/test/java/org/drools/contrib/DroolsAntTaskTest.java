/*
 * Copyright 2005 JBoss Inc
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

package org.drools.contrib;

import java.io.FileInputStream;
import java.io.IOException;

import junit.framework.Assert;

import org.drools.KnowledgeBase;
import org.drools.RuleBase;
import org.drools.definition.KnowledgePackage;
import org.drools.rule.Package;
import org.drools.util.DroolsStreamUtils;

/**
 * DroolsAntTask test case
 */
public class DroolsAntTaskTest extends BuildFileTest {

    public DroolsAntTaskTest() {
        super( "DroolsAntTest" );
    }

    public void setUp() {
        configureProject( "src/test/resources/DroolsAntTask.xml" );
    }

    public void testVerifierReport() {
        try {
            executeTarget( "verifierreport" );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public void testDslRules() throws IOException,
                              ClassNotFoundException {
        try {
            executeTarget( "dslRules" );
        } catch ( Exception e ) {
            e.printStackTrace();
            Assert.fail( "Should not throw any exception: " + e.getMessage() );
        }

        Package p1 = (Package) DroolsStreamUtils.streamIn( new FileInputStream( "target/cheese.rules.dpkg" ) );

        assertNotNull( p1 );
        assertEquals( 1,
                      p1.getRules().length );
    }

    public void testDslRulesKnowledge() throws IOException,
                                       ClassNotFoundException {
        try {
            executeTarget( "dslRulesKnowledge" );
        } catch ( Exception e ) {
            e.printStackTrace();
            Assert.fail( "Should not throw any exception: " + e.getMessage() );
        }

        KnowledgePackage kpackage1 = (KnowledgePackage) DroolsStreamUtils.streamIn( new FileInputStream( "target/cheese.rules.dpkg" ) );

        assertNotNull( kpackage1 );
        assertEquals( 1,
                      kpackage1.getRules().size() );
    }

    public void testRules() throws IOException,
                           ClassNotFoundException {
        try {
            executeTarget( "rules" );
        } catch ( Exception e ) {
            e.printStackTrace();
            Assert.fail( "Should not throw any exception: " + e.getMessage() );
        }

        RuleBase r1 = (RuleBase) DroolsStreamUtils.streamIn( new FileInputStream( "target/cheese.rules" ) );

        assertNotNull( r1 );
        assertEquals( 1,
                      r1.getPackages().length );
    }

    public void testRulesKnowledge() throws IOException,
                                    ClassNotFoundException {
        try {
            executeTarget( "rulesKnowledge" );
        } catch ( Exception e ) {
            e.printStackTrace();
            Assert.fail( "Should not throw any exception: " + e.getMessage() );
        }

        KnowledgeBase kbase = (KnowledgeBase) DroolsStreamUtils.streamIn( new FileInputStream( "target/cheese.rules" ) );

        assertNotNull( kbase );
        assertEquals( 1,
                      kbase.getKnowledgePackages().size() );
    }

    public void testNoPackageFile() {
        try {
            executeTarget( "rulesnopackagefile" );
            Assert.fail( "Should throw an exception " );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public void testManyPackageFiles() {
        try {
            executeTarget( "rulesmanypackagefile" );
            Assert.fail( "Should throw an exception " );
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

}
