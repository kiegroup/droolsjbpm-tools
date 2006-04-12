package org.drools.ide.editors;

import org.drools.ide.editors.outline.FunctionTreeNode;
import org.drools.ide.editors.outline.PackageTreeNode;
import org.drools.ide.editors.outline.RuleContentOutlinePage;

import junit.framework.TestCase;

public class RuleContentOutlineTest extends TestCase {

    public void testNodeBuild() {
        RuleContentOutlinePage page = new RuleContentOutlinePage(null);
        String source = "expander foobar.dsl\nimport foo\nfunction void smeg(s) {\n \n}\n";
        PackageTreeNode node = new PackageTreeNode();
        page.populatePackageTreeNode( node, source );
        Object[] nodes = node.getChildren( new Object() );
        
        for ( int i = 0; i < nodes.length; i++ ) {
            if (nodes[i] instanceof FunctionTreeNode) {
                FunctionTreeNode func = (FunctionTreeNode) nodes[i];
                assertEquals("smeg()", func.getLabel( null ));
                
            }
        }
        
        
        source = "function String foo(String bar) {";
        node = new PackageTreeNode();
        page.populatePackageTreeNode( node, source );
        FunctionTreeNode func = (FunctionTreeNode) node.getChildren( new Object() )[0];        
        assertEquals("foo()", func.getLabel( null ));
        
    }
    
}
