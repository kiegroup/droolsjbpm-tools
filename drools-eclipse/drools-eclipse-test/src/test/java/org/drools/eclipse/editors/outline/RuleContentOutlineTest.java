package org.drools.eclipse.editors.outline;

import junit.framework.TestCase;

import org.drools.eclipse.core.DroolsElement;
import org.drools.eclipse.core.Function;
import org.drools.eclipse.core.Package;
import org.drools.eclipse.core.RuleSet;
import org.drools.eclipse.editors.AbstractRuleEditor;
import org.drools.eclipse.editors.outline.RuleContentOutlinePage;

public class RuleContentOutlineTest extends TestCase {

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
