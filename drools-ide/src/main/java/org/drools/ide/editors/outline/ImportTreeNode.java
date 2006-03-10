package org.drools.ide.editors.outline;

import org.drools.ide.DroolsIDEPlugin;
import org.eclipse.jface.resource.ImageDescriptor;

public class ImportTreeNode extends OutlineNode
    implements
    Comparable {

    private final PackageTreeNode packageTreeNode;
    private final String          importLabel;

    public ImportTreeNode(PackageTreeNode parent,
                            String importLabel) {
        packageTreeNode = parent;
        this.importLabel = importLabel;

    }

    public Object[] getChildren(Object o) {
        return new Object[0];
    }

    public ImageDescriptor getImageDescriptor(Object object) {
        return DroolsIDEPlugin.getImageDescriptor( "icons/import.gif" );
    }

    public String getLabel(Object o) {
        return importLabel;
    }

    public Object getParent(Object o) {
        return packageTreeNode;
    }

    public int compareTo(Object obj) {
        if ( obj instanceof ImportTreeNode ) {
            ImportTreeNode other = (ImportTreeNode) obj;
            return this.importLabel.compareTo( other.importLabel );
        } else {
            return 0;
        }
    }
}
