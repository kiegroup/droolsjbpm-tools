package org.drools.ide.editors.outline;

import org.drools.ide.DroolsIDEPlugin;
import org.eclipse.jface.resource.ImageDescriptor;

public class FunctionTreeNode extends OutlineNode
    implements
    Comparable {

    private final PackageTreeNode packageTreeNode;
    private final String          functionLabel;

    public FunctionTreeNode(PackageTreeNode parent,
                            String functionLabel) {
        packageTreeNode = parent;
        this.functionLabel = functionLabel;

    }

    public Object[] getChildren(Object o) {
        return new Object[0];
    }

    public ImageDescriptor getImageDescriptor(Object object) {
        return DroolsIDEPlugin.getImageDescriptor( "icons/public_co.gif" );
    }

    public String getLabel(Object o) {
        return functionLabel;
    }

    public Object getParent(Object o) {
        return packageTreeNode;
    }

    public int compareTo(Object obj) {
        if ( obj instanceof FunctionTreeNode ) {
            FunctionTreeNode other = (FunctionTreeNode) obj;
            return this.functionLabel.compareTo( other.functionLabel );
        } else {
            return 0;
        }
    }
}
