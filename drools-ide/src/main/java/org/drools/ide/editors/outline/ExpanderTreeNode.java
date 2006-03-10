package org.drools.ide.editors.outline;

import org.drools.ide.DroolsIDEPlugin;
import org.eclipse.jface.resource.ImageDescriptor;

public class ExpanderTreeNode extends OutlineNode
    implements
    Comparable {

    private final PackageTreeNode packageTreeNode;
    private final String          expanderLabel;

    public ExpanderTreeNode(PackageTreeNode parent,
                            String importLabel) {
        packageTreeNode = parent;
        this.expanderLabel = importLabel;

    }

    public Object[] getChildren(Object o) {
        return new Object[0];
    }

    public ImageDescriptor getImageDescriptor(Object object) {
        return DroolsIDEPlugin.getImageDescriptor( "icons/dsl.gif" );
    }

    public String getLabel(Object o) {
        return expanderLabel;
    }

    public Object getParent(Object o) {
        return packageTreeNode;
    }

    public int compareTo(Object obj) {
        if ( obj instanceof ExpanderTreeNode ) {
            ExpanderTreeNode other = (ExpanderTreeNode) obj;
            return this.expanderLabel.compareTo( other.expanderLabel );
        } else {
            return 0;
        }
    }
}
