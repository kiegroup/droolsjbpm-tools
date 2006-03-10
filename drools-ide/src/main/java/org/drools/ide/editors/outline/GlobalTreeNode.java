package org.drools.ide.editors.outline;

import org.drools.ide.DroolsIDEPlugin;
import org.eclipse.jface.resource.ImageDescriptor;

public class GlobalTreeNode extends OutlineNode
    implements
    Comparable {

    private final PackageTreeNode packageTreeNode;
    private final String          queryLabel;

    public GlobalTreeNode(PackageTreeNode parent,
                            String importLabel) {
        packageTreeNode = parent;
        this.queryLabel = importLabel;

    }

    public Object[] getChildren(Object o) {
        return new Object[0];
    }

    public ImageDescriptor getImageDescriptor(Object object) {
        return DroolsIDEPlugin.getImageDescriptor( "icons/public_co.gif" );
    }

    public String getLabel(Object o) {
        return queryLabel;
    }

    public Object getParent(Object o) {
        return packageTreeNode;
    }

    public int compareTo(Object obj) {
        if ( obj instanceof GlobalTreeNode ) {
            GlobalTreeNode other = (GlobalTreeNode) obj;
            return this.queryLabel.compareTo( other.queryLabel );
        } else {
            return 0;
        }
    }
}
