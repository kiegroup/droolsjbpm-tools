package org.drools.ide.editors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.drools.ide.DroolsIDEPlugin;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * This represents a package in the outline view. 
 * Each rule resource must have a package declaration.
 * 
 * @author Jeff Brown
 */
public class PackageTreeNode extends OutlineNode {

    private List   rules       = new ArrayList();

    private String packageName = "<unknown package name>";

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    /** Add a rule node to the outline. Keeping track of where it was seen in the document (offset) */
    public void addRule(String ruleName, int offset, int length) {
        RuleTreeNode node = new RuleTreeNode( this, ruleName );
        node.setOffset( offset );
        node.setLength( length );
        rules.add( node );
    }

    /** 
     * This will return the child nodes, as they are to be displayed on screen (sorted an all !) 
     * Rules should appear at the top, sorted, as they are the most important assets.
     * */
    public Object[] getChildren(Object o) {
        //sort and add rules
        Collections.sort( rules );
        return rules.toArray();
    }

    public ImageDescriptor getImageDescriptor(Object object) {
        ImageDescriptor des = DroolsIDEPlugin.getImageDescriptor( "icons/package_obj.gif" );
        return des;
        
    }

    public String getLabel(Object o) {
        return packageName;
    }

    public Object getParent(Object o) {
        return null;
    }

    public Object getAdapter(Class adapter) {
        if ( adapter == IWorkbenchAdapter.class ) {
            return this;
        }
        return null;
    }
}
