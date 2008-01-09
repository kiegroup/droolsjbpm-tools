package org.drools.eclipse.flow.common.editor.core;
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * Default wrapper of a model element.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public abstract class DefaultElementWrapper implements IPropertySource, ElementWrapper, Serializable {

    protected static IPropertyDescriptor[] descriptors;

    public static final String NAME = "Name";
    static {
        descriptors = new IPropertyDescriptor[] {
            new TextPropertyDescriptor(NAME, "Name"),
        };
    }
    
    private Object element;
    private Rectangle constraint;
    private ProcessWrapper parent;
    private List incomingConnections = new ArrayList();
    private List outgoingConnections = new ArrayList();
    private transient List listeners = new ArrayList();
    
    protected void setElement(Object element) {
		this.element = element;
	}

	public Object getElement() {
		return element;
	}

	public void setConstraint(Rectangle constraint) {
		this.constraint = constraint;
		notifyListeners(CHANGE_CONSTRAINT);
	}

	public Rectangle getConstraint() {
		return constraint;
	}

	public void setParent(ProcessWrapper parent) {
		this.parent = parent;
	}

	protected ProcessWrapper getParent() {
		return parent;
	}

	public List getOutgoingConnections() {
		return Collections.unmodifiableList(outgoingConnections);
	}

	public List getIncomingConnections() {
		return Collections.unmodifiableList(incomingConnections);
	}

	public void addIncomingConnection(ElementConnection connection) {
		incomingConnections.add(connection);
		internalAddIncomingConnection(connection);
		notifyListeners(CHANGE_INCOMING_CONNECTIONS);
	}

	protected void internalAddIncomingConnection(ElementConnection connection) {
	}

	public void removeIncomingConnection(ElementConnection connection) {
		incomingConnections.remove(connection);
		internalRemoveIncomingConnection(connection);
		notifyListeners(CHANGE_INCOMING_CONNECTIONS);
	}

	protected void internalRemoveIncomingConnection(ElementConnection connection) {
	}

	public void addOutgoingConnection(ElementConnection connection) {
		outgoingConnections.add(connection);
		internalAddOutgoingConnection(connection);
		notifyListeners(CHANGE_OUTGOING_CONNECTIONS);
	}

	protected void internalAddOutgoingConnection(ElementConnection connection) {
	}

	public void removeOutgoingConnection(ElementConnection connection) {
		outgoingConnections.remove(connection);
		internalRemoveOutgoingConnection(connection);
		notifyListeners(CHANGE_OUTGOING_CONNECTIONS);
	}

	protected void internalRemoveOutgoingConnection(ElementConnection connection) {
	}

	public void setName(String name) {
		internalSetName(name);
		notifyListeners(CHANGE_NAME);
	}

	protected void internalSetName(String name) {
	}

	public void addListener(ModelListener listener) {
		listeners.add(listener);
	}

	public void removeListener(ModelListener listener) {
		listeners.remove(listener);
	}

	protected void notifyListeners(int change) {
		ModelEvent event = new ModelEvent(change);
		for (Iterator it = listeners.iterator(); it.hasNext();) {
			ModelListener listener = (ModelListener) it.next();
			listener.modelChanged(event);
		}
	}

	private void readObject(ObjectInputStream aInputStream)
			throws ClassNotFoundException, IOException {
		aInputStream.defaultReadObject();
		listeners = new ArrayList();
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		return descriptors;
	}

	public Object getEditableValue() {
		return this;
	}

	public boolean isPropertySet(Object id) {
		return true;
	}

	public Object getPropertyValue(Object id) {
		if (NAME.equals(id)) {
			return getName();
		}
		return null;
	}

	public void resetPropertyValue(Object id) {
		if (NAME.equals(id)) {
			setName("");
		}
	}

	public void setPropertyValue(Object id, Object value) {
		if (NAME.equals(id)) {
			setName((String) value);
		}
	}
}
