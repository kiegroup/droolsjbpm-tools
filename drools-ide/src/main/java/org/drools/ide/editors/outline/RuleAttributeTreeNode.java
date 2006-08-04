package org.drools.ide.editors.outline;

import org.drools.ide.DroolsIDEPlugin;
import org.eclipse.jface.resource.ImageDescriptor;

public class RuleAttributeTreeNode extends OutlineNode implements Comparable {

    private final RuleTreeNode ruleTreeNode;
    private final String attributeName;
    private final Object attributeValue;

    public RuleAttributeTreeNode(RuleTreeNode parent,
                        String name, Object value) {
    	ruleTreeNode = parent;
        this.attributeName = name;
        this.attributeValue = value;
    }

    public Object[] getChildren(Object o) {
        return new Object[0];
    }

    public ImageDescriptor getImageDescriptor(Object object) {
        return DroolsIDEPlugin.getImageDescriptor( "icons/drools.gif" );
    }

    public String getLabel(Object o) {
        return attributeName + " = " + attributeValue;
    }

    public Object getParent(Object o) {
        return ruleTreeNode;
    }

    public int compareTo(Object obj) {
        if (obj instanceof RuleAttributeTreeNode) {
            RuleAttributeTreeNode other = (RuleAttributeTreeNode) obj;
            return this.attributeName.compareTo(other.attributeName);
        } else {
            return 0;
        }
    }

}
