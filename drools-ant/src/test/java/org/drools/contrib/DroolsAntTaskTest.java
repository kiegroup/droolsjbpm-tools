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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.FileInputStream;
import java.io.IOException;

import org.apache.tools.ant.BuildException;
import org.drools.RuleBase;
import org.drools.core.util.DroolsStreamUtils;
import org.drools.rule.Package;
import org.junit.Before;
import org.junit.Test;
import org.kie.KnowledgeBase;
import org.kie.definition.KnowledgePackage;

/**
 * DroolsAntTask test case
 */
public class DroolsAntTaskTest extends BuildFileTest {

    @Before
    public void setUp() {

        // Maven and eclipse need different paths
        // When running in Maven the property is set.
        String path = System.getProperty( "eclipsepath" );
        if ( path == null ) {
            System.setProperty( "eclipsepath",
                                "../../../" );
        }

        configureProject( "src/test/resources/DroolsAntTask.xml" );
    }

    @Test
    public void testVerifierReport() {
        executeTarget( "verifierreport" );
    }

    @Test
    public void testDslRules() throws IOException,
                              ClassNotFoundException {
        executeTarget( "dslRules" );

        Package p1 = (Package) DroolsStreamUtils.streamIn( new FileInputStream( "target/cheese.rules.pkg" ) );

        assertNotNull( p1 );
        assertEquals( 1,
                      p1.getRules().length );
    }

    @Test
    public void testDslRulesKnowledge() throws IOException,
                                       ClassNotFoundException {
        executeTarget( "dslRulesKnowledge" );

        KnowledgePackage kpackage1 = (KnowledgePackage) DroolsStreamUtils.streamIn( new FileInputStream( "target/cheese.rules.pkg" ) );

        assertNotNull( kpackage1 );
        assertEquals( 1,
                      kpackage1.getRules().size() );
    }

    @Test
    public void testRules() throws IOException,
                           ClassNotFoundException {
        executeTarget( "rules" );

        RuleBase r1 = (RuleBase) DroolsStreamUtils.streamIn( new FileInputStream( "target/cheese.rules" ) );

        assertNotNull( r1 );
        assertEquals( 1,
                      r1.getPackages().length );
    }

    @Test
    public void testRulesKnowledge() throws IOException,
                                    ClassNotFoundException {
        executeTarget( "rulesKnowledge" );

        KnowledgeBase kbase = (KnowledgeBase) DroolsStreamUtils.streamIn( new FileInputStream( "target/cheese.rules" ) );

        assertNotNull( kbase );
        assertEquals( 1,
                      kbase.getKnowledgePackages().size() );
    }

    @Test
    public void testRuleFlow() throws IOException,
                              ClassNotFoundException {
        executeTarget("ruleFlow");

        RuleBase r1 = (RuleBase) DroolsStreamUtils.streamIn( new FileInputStream( "target/ruleFlow.rules" ) );

        assertNotNull( r1 );
        assertEquals( 1,
                      r1.getPackages().length );
    }

    @Test
    public void testRuleFlowKnowledge() throws IOException,
                                       ClassNotFoundException {
        executeTarget( "ruleFlowKnowledge" );

        KnowledgeBase kbase = (KnowledgeBase) DroolsStreamUtils.streamIn( new FileInputStream( "target/ruleFlow.rules" ) );

        assertNotNull( kbase );
        assertEquals( 1,
                      kbase.getKnowledgePackages().size() );
    }

    @Test(expected = BuildException.class)
    public void testNoPackageFile() {
        executeTarget( "rulesnopackagefile" );
    }

}
