package org.drools.ide.editors.outline;

import org.drools.ide.DroolsIDEPlugin;
import org.eclipse.jface.resource.ImageDescriptor;

public class RuleTreeNode extends OutlineNode implements Comparable {

    private final PackageTreeNode packageTreeNode;
    private final String          ruleName;

    public RuleTreeNode(PackageTreeNode parent,
                        String ruleName) {
        packageTreeNode = parent;
        this.ruleName = ruleName;

    }

    public Object[] getChildren(Object o) {
        return new Object[0];
    }

    public ImageDescriptor getImageDescriptor(Object object) {
        return DroolsIDEPlugin.getImageDescriptor( "icons/drools.gif" );
    }

    public String getLabel(Object o) {
        return ruleName;
    }

    public Object getParent(Object o) {
        return packageTreeNode;
    }

    public int compareTo(Object obj) {
        if (obj instanceof RuleTreeNode) {
            RuleTreeNode other = (RuleTreeNode) obj;
            return this.ruleName.compareTo( other.ruleName );
        } else {
            return 0;
        }
    }

}
