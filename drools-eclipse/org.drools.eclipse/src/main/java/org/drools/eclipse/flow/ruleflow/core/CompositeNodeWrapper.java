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

import java.util.List;
import java.util.Map;

import org.drools.definition.process.NodeContainer;
import org.drools.eclipse.flow.common.editor.core.DefaultElementWrapper;
import org.drools.eclipse.flow.common.editor.core.ElementConnection;
import org.drools.eclipse.flow.common.editor.core.ElementContainerElementWrapper;
import org.drools.eclipse.flow.common.editor.core.ElementWrapper;
import org.drools.eclipse.flow.ruleflow.view.property.action.OnEntryActionsPropertyDescriptor;
import org.drools.eclipse.flow.ruleflow.view.property.action.OnExitActionsPropertyDescriptor;
import org.drools.eclipse.flow.ruleflow.view.property.timers.TimersPropertyDescriptor;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.jbpm.process.core.timer.Timer;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.DroolsAction;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.WorkflowProcess;
import org.jbpm.workflow.core.impl.ExtendedNodeImpl;
import org.jbpm.workflow.core.node.CompositeNode;

public abstract class CompositeNodeWrapper extends ElementContainerElementWrapper implements NodeWrapper {

    public static final String ON_ENTRY_ACTIONS = "OnEntryActions";
    public static final String ON_EXIT_ACTIONS = "OnExitActions";
    public static final String TIMERS = "Timers";

    protected IPropertyDescriptor[] descriptors;
    
    private static final long serialVersionUID = 510l;

    public void setNode(Node node) {
        setElement(node);
    }
    
    public Node getNode() {
        return (Node) getElement();
    }
    
    public boolean isFullProperties() {
    	Node node = getNode();
    	NodeContainer container = node.getNodeContainer();
    	while (!(container instanceof RuleFlowProcess)) {
    		if (container instanceof Node) {
    			container = ((Node) container).getNodeContainer();
    		} else {
    			return false;
    		}
    	}
    	// not full properties for BPMN2 process, which is set to autocomplete = true
    	return !((RuleFlowProcess) container).isAutoComplete();
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
 
    public IPropertyDescriptor[] getPropertyDescriptors() {
        if (descriptors == null) {
            initDescriptors();
        }
        return descriptors;
    }

    protected void initDescriptors() {
        IPropertyDescriptor[] oldDescriptors = DefaultElementWrapper.DESCRIPTORS;
        descriptors = new IPropertyDescriptor[oldDescriptors.length + 3];
        System.arraycopy(oldDescriptors, 0, descriptors, 0, oldDescriptors.length);
        descriptors[descriptors.length - 3] = new OnEntryActionsPropertyDescriptor(
                ON_ENTRY_ACTIONS, "On Entry Actions", getCompositeNode(),
                (WorkflowProcess) getParent().getProcessWrapper().getProcess());
        descriptors[descriptors.length - 2] = new OnExitActionsPropertyDescriptor(
                ON_EXIT_ACTIONS, "On Exit Actions", getCompositeNode(),
                (WorkflowProcess) getParent().getProcessWrapper().getProcess());
        descriptors[descriptors.length - 1] = 
            new TimersPropertyDescriptor(TIMERS, "Timers", getCompositeNode(),
                (WorkflowProcess) getParent().getProcessWrapper().getProcess());
    }
    
    public Object getPropertyValue(Object id) {
        if (ON_ENTRY_ACTIONS.equals(id)) {
            return getCompositeNode().getActions(ExtendedNodeImpl.EVENT_NODE_ENTER);
        }
        if (ON_EXIT_ACTIONS.equals(id)) {
            return getCompositeNode().getActions(ExtendedNodeImpl.EVENT_NODE_EXIT);
        }
        if (TIMERS.equals(id)) {
            return getCompositeNode().getTimers();
        }
        return super.getPropertyValue(id);
    }

    public void resetPropertyValue(Object id) {
        if (ON_ENTRY_ACTIONS.equals(id)) {
            getCompositeNode().setActions(ExtendedNodeImpl.EVENT_NODE_ENTER, null);
        } else if (ON_EXIT_ACTIONS.equals(id)) {
            getCompositeNode().setActions(ExtendedNodeImpl.EVENT_NODE_EXIT, null);
        } else if (TIMERS.equals(id)) {
            getCompositeNode().removeAllTimers();
        } else {
            super.resetPropertyValue(id);
        }
    }

    @SuppressWarnings("unchecked")
    public void setPropertyValue(Object id, Object value) {
        if (ON_ENTRY_ACTIONS.equals(id)) {
            getCompositeNode().setActions(ExtendedNodeImpl.EVENT_NODE_ENTER, (List<DroolsAction>) value);
        } else if (ON_EXIT_ACTIONS.equals(id)) {
            getCompositeNode().setActions(ExtendedNodeImpl.EVENT_NODE_EXIT, (List<DroolsAction>) value);
        } else if (TIMERS.equals(id)) {
            getCompositeNode().removeAllTimers();
            // adding one by one so the ids are set correctly
            for (Map.Entry<Timer, DroolsAction> entry: ((Map<Timer, DroolsAction>) value).entrySet()) {
                getCompositeNode().addTimer(entry.getKey(), entry.getValue());
            }
        } else {
            super.setPropertyValue(id, value);
        }
    }
    
}
