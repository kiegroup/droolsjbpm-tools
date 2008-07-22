package org.drools.eclipse.flow.ruleflow.core;

import org.drools.eclipse.flow.common.editor.core.ElementConnection;
import org.drools.eclipse.flow.common.editor.core.ElementContainerElementWrapper;
import org.drools.eclipse.flow.common.editor.core.ElementWrapper;
import org.drools.workflow.core.Connection;
import org.drools.workflow.core.Node;
import org.drools.workflow.core.node.CompositeNode;
import org.eclipse.draw2d.geometry.Rectangle;

public class CompositeNodeWrapper extends ElementContainerElementWrapper implements NodeWrapper {

    private static final long serialVersionUID = 400L;

    public CompositeNodeWrapper() {
        setNode(new CompositeNode());
        getCompositeNode().setName("CompositeNode");
    }
    
    public void setNode(Node node) {
        setElement(node);
    }
    
    public Node getNode() {
        return (Node) getElement();
    }
    
    public String getId() {
        long id = getNode().getId();
        return id == -1 ? null : getNode().getId() + "";
    }

    public String getName() {
        return getNode().getName();
    }

    public void internalSetName(String name) {
        getNode().setName(name);    
        notifyListeners(CHANGE_NAME);
    }
    
    protected void internalSetConstraint(Rectangle constraint) {
        Node node = getNode();
        node.setMetaData("x", constraint.x);
        node.setMetaData("y", constraint.y);
        node.setMetaData("width", constraint.width);
        node.setMetaData("height", constraint.height);
    }
    
    public Rectangle internalGetConstraint() {
        Node node = getNode();
        Integer x = (Integer) node.getMetaData("x");
        Integer y = (Integer) node.getMetaData("y");
        Integer width = (Integer) node.getMetaData("width");
        Integer height = (Integer) node.getMetaData("height");
        return new Rectangle(
            x == null ? 0 : x,
            y == null ? 0 : y,
            width == null ? -1 : width,
            height == null ? -1 : height);
    }
    
    public CompositeNode getCompositeNode() {
        return (CompositeNode) getNode();
    }
    
    public boolean acceptsIncomingConnection(ElementConnection connection, ElementWrapper source) {
        return getIncomingConnections().isEmpty()
        	&& (source == null
    			|| ((NodeWrapper) source).getNode().getNodeContainer() == getNode().getNodeContainer()
    			|| ((NodeWrapper) source).getNode().getNodeContainer() == getNode());
    }

    public boolean acceptsOutgoingConnection(ElementConnection connection, ElementWrapper target) {
        return getOutgoingConnections().isEmpty()
        	&& (target == null
    			|| ((NodeWrapper) target).getNode().getNodeContainer() == getNode().getNodeContainer()
        		|| ((NodeWrapper) target).getNode().getNodeContainer() == getNode());
    }

    protected void internalAddElement(ElementWrapper element) {
        getCompositeNode().addNode(((NodeWrapper) element).getNode());
    }

    protected void internalRemoveElement(ElementWrapper element) {
        getCompositeNode().removeNode(((NodeWrapper) element).getNode());
    }
 
}
