package org.drools.ide.editors;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

public class RuleTreeNode
    implements
    IWorkbenchAdapter,
    IAdaptable {

    private final PackageTreeNode packageTreeNode;
    private final String          ruleName;

    public RuleTreeNode(PackageTreeNode parent,
                        String ruleName) {
        packageTreeNode = parent;
        this.ruleName = ruleName;

    }

    public Object getAdapter(Class adapter) {
        if ( adapter == IWorkbenchAdapter.class ) {
            return this;
        }
        return null;
    }

    public Object[] getChildren(Object o) {
        return new Object[0];
    }

    public ImageDescriptor getImageDescriptor(Object object) {
        return null;
    }

    public String getLabel(Object o) {
        return ruleName;
    }

    public Object getParent(Object o) {
        return packageTreeNode;
    }

}
