package org.drools.ide.editors.outline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.drools.ide.DroolsIDEPlugin;
import org.eclipse.jface.resource.ImageDescriptor;

public class RuleTreeNode extends OutlineNode implements Comparable {

    private final PackageTreeNode packageTreeNode;
    private final String ruleName;
    private List attributes = new ArrayList();

    public RuleTreeNode(PackageTreeNode parent,
                        String ruleName) {
        packageTreeNode = parent;
        this.ruleName = ruleName;

    }

    public void addAttribute(String name, Object value, int offset, int length) {
		RuleAttributeTreeNode node = new RuleAttributeTreeNode(this, name, value);
		node.setOffset(offset);
		node.setLength(length);
		attributes.add(node);
	}

    public Object[] getChildren(Object o) {
        Collections.sort( attributes );
        return attributes.toArray();
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
