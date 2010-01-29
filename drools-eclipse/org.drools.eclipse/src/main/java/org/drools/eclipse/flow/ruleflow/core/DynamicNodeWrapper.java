package org.drools.eclipse.flow.ruleflow.core;

import org.drools.eclipse.flow.common.editor.core.ElementConnection;
import org.drools.eclipse.flow.common.editor.core.ElementWrapper;
import org.drools.process.core.context.variable.VariableScope;
import org.drools.workflow.core.node.DynamicNode;

public class DynamicNodeWrapper extends CompositeNodeWrapper {

    private static final long serialVersionUID = 4L;

    public DynamicNodeWrapper() {
        setNode(new DynamicNode());
        getDynamicNode().setName("Dynamic");
        VariableScope variableScope = new VariableScope();
        getDynamicNode().addContext(variableScope);
        getDynamicNode().setDefaultContext(variableScope);
    }
    
    public DynamicNode getDynamicNode() {
    	return (DynamicNode) getNode();
    }
    
    public boolean acceptsIncomingConnection(ElementConnection connection, ElementWrapper source) {
        return super.acceptsIncomingConnection(connection, source)
        	&& getIncomingConnections().isEmpty();
    }

    public boolean acceptsOutgoingConnection(ElementConnection connection, ElementWrapper target) {
        return target == null
			|| (target.getParent() == getParent() && getOutgoingConnections().isEmpty());
//			|| (target.getParent() == this && getForEachNode().getLinkedIncomingNode(Node.CONNECTION_DEFAULT_TYPE) == null);
    }
    
}
