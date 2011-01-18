/**
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

package org.drools.eclipse.editors.outline;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.drools.eclipse.core.DroolsElement;
import org.drools.eclipse.core.Function;
import org.drools.eclipse.core.Package;
import org.drools.eclipse.core.RuleSet;
import org.drools.eclipse.editors.AbstractRuleEditor;
import org.drools.eclipse.editors.outline.RuleContentOutlinePage;

public class RuleContentOutlineTest {

    @Test
    public void testNodeBuild() {
        RuleContentOutlinePage page = new RuleContentOutlinePage( null );
        String source = "package test;\nexpander foobar.dsl\nimport foo\nfunction void smeg(s) {\n \n}\n";
        page.populatePackageTreeNode( source );
        RuleSet ruleSet = page.getRuleSet();
        Package p = ruleSet.getPackage("test");
        assertNotNull(p);
        DroolsElement[] nodes = p.getChildren();

        for ( int i = 0; i < nodes.length; i++ ) {
            if ( nodes[i] instanceof Function ) {
                Function func = (Function) nodes[i];
                assertEquals( "smeg()",
                              func.getFunctionName() );

            }
        }

        source = "package test;\n function String foo(String bar) {";
        page.populatePackageTreeNode( source );
        p = ruleSet.getPackage("test");
        assertNotNull(p);
        Function func = (Function) p.getChildren()[0];
        assertEquals( "foo()",
                      func.getFunctionName() );

    }

}
