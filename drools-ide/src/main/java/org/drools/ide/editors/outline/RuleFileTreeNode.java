package org.drools.ide.editors.outline;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * This represents a rule file in the outline view. 
 * 
 * @author Jeff Brown
 */
public class RuleFileTreeNode extends OutlineNode {

    private PackageTreeNode packageTreeNode = null;

    public Object[] getChildren(Object o) {
        if(packageTreeNode == null) return new Object[0];
        return new Object[]{packageTreeNode};
    }

    public ImageDescriptor getImageDescriptor(Object object) {
        return null;
    }

    public String getLabel(Object o) {
        return null;
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

    public PackageTreeNode getPackageTreeNode() {
        return packageTreeNode;
    }

    public void setPackageTreeNode(PackageTreeNode packageTreeNode) {
        this.packageTreeNode = packageTreeNode;
    }
}
