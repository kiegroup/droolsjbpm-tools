/*
 * Copyright 2005 JBoss Inc
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

import java.util.HashMap;
import java.util.Map;

import org.drools.definition.process.NodeContainer;
import org.drools.eclipse.flow.common.editor.core.DefaultElementWrapper;
import org.drools.eclipse.flow.common.editor.core.ElementConnection;
import org.drools.eclipse.flow.common.editor.core.ElementWrapper;
import org.drools.eclipse.flow.ruleflow.view.property.metadata.MetaDataPropertyDescriptor;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.Node;
import org.jbpm.workflow.core.impl.NodeImpl;

/**
 * Wrapper for a node.
 */
public abstract class AbstractNodeWrapper extends DefaultElementWrapper implements NodeWrapper {

    private static final long serialVersionUID = 510l;

    public static IPropertyDescriptor[] DESCRIPTORS;

    public static final String METADATA = "MetaData";
    static {
        DESCRIPTORS = new IPropertyDescriptor[DefaultElementWrapper.DESCRIPTORS.length + 1];
        System.arraycopy(DefaultElementWrapper.DESCRIPTORS, 0, DESCRIPTORS, 0, DefaultElementWrapper.DESCRIPTORS.length);
        DESCRIPTORS[DESCRIPTORS.length - 1] = 
            new MetaDataPropertyDescriptor(METADATA, "MetaData");
    }

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
    
    public Color internalGetColor() {
        Integer rgb = (Integer) getNode().getMetaData("color");
        if (rgb != null) {
            return new Color(Display.getCurrent(), integerToRGB(rgb));
        }
        return null;
    }

    protected void internalSetColor(Integer color) {
        getNode().setMetaData("color", color);
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

    public boolean acceptsIncomingConnection(ElementConnection connection, ElementWrapper source) {
        return source == null
            || source.getParent() == getParent()
            || getParent() == source;
    }

    public boolean acceptsOutgoingConnection(ElementConnection connection, ElementWrapper target) {
        return target == null
            || ((NodeWrapper) target).getNode().getNodeContainer() == getNode().getNodeContainer()
            || ((NodeWrapper) target).getNode() == getNode().getNodeContainer();
    }
    
    public IPropertyDescriptor[] getPropertyDescriptors() {
        return DESCRIPTORS;
    }

    public Object getPropertyValue(Object id) {
        if (METADATA.equals(id)) {
            return ((NodeImpl) getNode()).getMetaData();
        }
        return super.getPropertyValue(id);
    }

    public void resetPropertyValue(Object id) {
        if (METADATA.equals(id)) {
            ((NodeImpl) getNode()).setMetaData(new HashMap<String, Object>());
        } else {
            super.resetPropertyValue(id);
        }
    }

    public void setPropertyValue(Object id, Object value) {
        if (METADATA.equals(id)) {
            ((NodeImpl) getNode()).setMetaData((Map<String, Object>) value);
        } else {
            super.setPropertyValue(id, value);
        }
    }

}
