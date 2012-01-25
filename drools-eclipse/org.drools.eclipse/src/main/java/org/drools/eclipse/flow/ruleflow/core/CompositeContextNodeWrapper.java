/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.jbpm.process.core.context.exception.ExceptionHandler;
import org.jbpm.process.core.context.exception.ExceptionScope;
import org.jbpm.process.core.context.variable.Variable;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.node.CompositeContextNode;
import org.jbpm.workflow.core.node.CompositeNode;

public class CompositeContextNodeWrapper extends CompositeNodeWrapper {

    public static final String VARIABLES = "variables";
    public static final String START_NODE = "startNodeId";
    public static final String END_NODE = "endNodeId";
    public static final String EXCEPTION_HANDLERS = "exceptionHandlers";

    private static final long serialVersionUID = 510l;

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
    	boolean fullProps = isFullProperties();
        descriptors = new IPropertyDescriptor[DefaultElementWrapper.DESCRIPTORS.length + (fullProps ? 4 : 1)];
        System.arraycopy(DefaultElementWrapper.DESCRIPTORS, 0, descriptors, 0, DefaultElementWrapper.DESCRIPTORS.length);
        if (fullProps) {
	        descriptors[descriptors.length - 4] = 
	            new TextPropertyDescriptor(START_NODE, "StartNodeId");
	        descriptors[descriptors.length - 3] = 
	            new TextPropertyDescriptor(END_NODE, "EndNodeId");
	        descriptors[descriptors.length - 2] = 
	            new ExceptionHandlersPropertyDescriptor(EXCEPTION_HANDLERS,
	                "Exception Handlers", getProcessWrapper().getProcess());
        }
        descriptors[descriptors.length - 1] = 
            new ListPropertyDescriptor(VARIABLES, "Variables", VariableListCellEditor.class);
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
