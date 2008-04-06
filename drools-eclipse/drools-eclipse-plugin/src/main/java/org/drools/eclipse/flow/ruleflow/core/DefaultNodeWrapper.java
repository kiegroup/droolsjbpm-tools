package org.drools.eclipse.flow.ruleflow.core;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.flow.common.editor.core.ElementConnection;
import org.drools.workflow.core.Node;
import org.drools.workflow.core.NodeExtension;

public class DefaultNodeWrapper extends AbstractNodeWrapper {

    private static final long serialVersionUID = 400L;
    
    public DefaultNodeWrapper(String nodeClassName) {
        try {
            Class<?> nodeClass = Class.forName(nodeClassName);
            Node node = (Node) nodeClass.newInstance();
            if (node instanceof NodeExtension) {
                NodeExtension nodeExtension = (NodeExtension) node;
                node.setName(nodeExtension.getDefaultName());
            }
            setNode(node);
        } catch (Throwable t) {
            DroolsEclipsePlugin.log(t);
        }
    }

    public boolean acceptsIncomingConnection(ElementConnection connection) {
        return getIncomingConnections().isEmpty();
    }

    public boolean acceptsOutgoingConnection(ElementConnection connection) {
        return getOutgoingConnections().isEmpty();
    }

}
