package org.drools.eclipse.flow.ruleflow.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.drools.eclipse.flow.common.editor.core.DefaultElementWrapper;
import org.drools.eclipse.flow.common.editor.core.ElementConnection;
import org.drools.eclipse.flow.common.editor.core.ElementWrapper;
import org.drools.eclipse.flow.common.view.property.ListPropertyDescriptor;
import org.drools.eclipse.flow.ruleflow.view.property.exceptionHandler.ExceptionHandlersPropertyDescriptor;
import org.drools.eclipse.flow.ruleflow.view.property.variable.VariableListCellEditor;
import org.drools.process.core.context.exception.ExceptionHandler;
import org.drools.process.core.context.exception.ExceptionScope;
import org.drools.process.core.context.variable.Variable;
import org.drools.process.core.context.variable.VariableScope;
import org.drools.workflow.core.Node;
import org.drools.workflow.core.node.CompositeContextNode;
import org.drools.workflow.core.node.CompositeNode;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

public class CompositeContextNodeWrapper extends CompositeNodeWrapper {

    public static final String VARIABLES = "variables";
    public static final String START_NODE = "startNodeId";
    public static final String END_NODE = "endNodeId";
    public static final String EXCEPTION_HANDLERS = "exceptionHandlers";

    private static final long serialVersionUID = 400L;

    private IPropertyDescriptor[] descriptors;

    public CompositeContextNodeWrapper() {
        setNode(new CompositeContextNode());
        getCompositeNode().setName("CompositeNode");
        VariableScope variableScope = new VariableScope();
        getCompositeContextNode().addContext(variableScope);
        getCompositeContextNode().setDefaultContext(variableScope);
    }
    
	public IPropertyDescriptor[] getPropertyDescriptors() {
		if (descriptors == null) {
			initPropertyDescriptors();
		}
		return descriptors;
	}

    private void initPropertyDescriptors() {
        descriptors = new IPropertyDescriptor[DefaultElementWrapper.descriptors.length + 4];
        System.arraycopy(DefaultElementWrapper.descriptors, 0, descriptors, 0, DefaultElementWrapper.descriptors.length);
        descriptors[descriptors.length - 4] = 
            new TextPropertyDescriptor(START_NODE, "StartNodeId");
        descriptors[descriptors.length - 3] = 
            new TextPropertyDescriptor(END_NODE, "EndNodeId");
        descriptors[descriptors.length - 2] = 
        	new ListPropertyDescriptor(VARIABLES, "Variables", VariableListCellEditor.class);
        descriptors[descriptors.length - 1] = 
        	new ExceptionHandlersPropertyDescriptor(EXCEPTION_HANDLERS,
        		"Exception Handlers", getProcessWrapper().getProcess());
    }
    
    public CompositeContextNode getCompositeContextNode() {
        return (CompositeContextNode) getNode();
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
 
    public Object getPropertyValue(Object id) {
        if (VARIABLES.equals(id)) {
            return ((VariableScope) getCompositeContextNode().getDefaultContext(VariableScope.VARIABLE_SCOPE)).getVariables();
        }
        if (START_NODE.equals(id)) {
        	CompositeNode.NodeAndType link = getCompositeNode().getLinkedIncomingNode(Node.CONNECTION_DEFAULT_TYPE);
        	return link == null ? "" : link.getNodeId() + "";
        }
        if (END_NODE.equals(id)) {
        	CompositeNode.NodeAndType link = getCompositeNode().getLinkedOutgoingNode(Node.CONNECTION_DEFAULT_TYPE);
        	return link == null ? "" : link.getNodeId() + "";
        }
        if (EXCEPTION_HANDLERS.equals(id)) {
        	ExceptionScope exceptionScope = (ExceptionScope)
        		getCompositeContextNode().getDefaultContext(ExceptionScope.EXCEPTION_SCOPE);
        	if (exceptionScope == null) {
        		return new HashMap<String, ExceptionHandler>();
        	}
            return exceptionScope.getExceptionHandlers();
        }
        return super.getPropertyValue(id);
    }

    public void resetPropertyValue(Object id) {
        if (VARIABLES.equals(id)) {
            ((VariableScope) getCompositeContextNode().getDefaultContext(
                VariableScope.VARIABLE_SCOPE)).setVariables(new ArrayList<Variable>());
        } else if (START_NODE.equals(id)) {
            getCompositeNode().linkIncomingConnections(Node.CONNECTION_DEFAULT_TYPE, null);
        } else if (END_NODE.equals(id)) {
            getCompositeNode().linkOutgoingConnections(null, Node.CONNECTION_DEFAULT_TYPE);
        } else if (EXCEPTION_HANDLERS.equals(id)) {
        	ExceptionScope exceptionScope = (ExceptionScope)
    			getCompositeContextNode().getDefaultContext(ExceptionScope.EXCEPTION_SCOPE);
        	if (exceptionScope != null) {
        		exceptionScope.setExceptionHandlers(new HashMap<String, ExceptionHandler>());
        	}
        } else {
        	super.resetPropertyValue(id);
        }
    }

    @SuppressWarnings("unchecked")
	public void setPropertyValue(Object id, Object value) {
        if (VARIABLES.equals(id)) {
            ((VariableScope) getCompositeContextNode().getDefaultContext(
                VariableScope.VARIABLE_SCOPE)).setVariables((List<Variable>) value);
        } else if (START_NODE.equals(id)) {
        	try {
        		getCompositeNode().linkIncomingConnections(
    				Node.CONNECTION_DEFAULT_TYPE, new Long((String) value), Node.CONNECTION_DEFAULT_TYPE);
        	} catch (IllegalArgumentException e) {
        		// could not link
        	}
        } else if (END_NODE.equals(id)) {
        	try {
            	getCompositeNode().linkOutgoingConnections(
        			new Long((String) value), Node.CONNECTION_DEFAULT_TYPE, Node.CONNECTION_DEFAULT_TYPE);
        	} catch (IllegalArgumentException e) {
        		// could not link
        	}
        } else if (EXCEPTION_HANDLERS.equals(id)) {
        	ExceptionScope exceptionScope = (ExceptionScope)
    			getCompositeContextNode().getDefaultContext(ExceptionScope.EXCEPTION_SCOPE);
        	if (exceptionScope == null) {
        		exceptionScope = new ExceptionScope();
        		getCompositeContextNode().addContext(exceptionScope);
        		getCompositeContextNode().setDefaultContext(exceptionScope);
        	}
    		exceptionScope.setExceptionHandlers((Map<String, ExceptionHandler>) value);
        } else {
            super.setPropertyValue(id, value);
        }
    }
}
