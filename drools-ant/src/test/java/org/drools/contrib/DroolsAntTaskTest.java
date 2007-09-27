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

import junit.framework.Assert;

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
    
    public void testAnalyticsReport() {
        try {
            executeTarget( "analyticsreport" );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
        
    public void testDslRules() {
        try {
            executeTarget( "dslRules" );
        } catch ( Exception e ) {
            e.printStackTrace();
            Assert.fail( "Should not throw any exception: " + e.getMessage() );
        }
    }
    
    
    public void testRules() {
        try {
            executeTarget( "rules" );
        } catch ( Exception e ) {
            e.printStackTrace();
            Assert.fail( "Should not throw any exception: " + e.getMessage() );
        }
    }

    public void testNoPackageFile() {
        try {
            executeTarget( "rulesnopackagefile" );
            Assert.fail( "Should throw an exception " );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void testManyPackageFiles() {
        try {
            executeTarget( "rulesmanypackagefile" );
            Assert.fail( "Should throw an exception " );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }    

}
