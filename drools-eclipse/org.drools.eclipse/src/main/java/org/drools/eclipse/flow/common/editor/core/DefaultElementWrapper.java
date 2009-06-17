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
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.flow.ruleflow.view.property.color.ColorPropertyDescriptor;
import org.drools.eclipse.preferences.IDroolsConstants;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * Default wrapper of a model element.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public abstract class DefaultElementWrapper implements ElementWrapper, IPropertySource, Serializable {

	private static final long serialVersionUID = 4L;

	private static boolean allowNodeCustomization =
		DroolsEclipsePlugin.getDefault().getPreferenceStore().getBoolean(
			IDroolsConstants.ALLOW_NODE_CUSTOMIZATION);

	protected static IPropertyDescriptor[] DESCRIPTORS;

    public static final String NAME = "Name";
    public static final String ID = "Id";
    public static final String COLOR = "Color";
    static {
    	if (allowNodeCustomization) {
	        DESCRIPTORS = new IPropertyDescriptor[] {
	            new TextPropertyDescriptor(NAME, "Name"),
	            new TextPropertyDescriptor(ID, "Id") {
	            	public CellEditor createPropertyEditor(Composite parent) {
	                    return null;
	                }
	            },
	            new ColorPropertyDescriptor(COLOR, "Color"),
	        };
    	} else {
    		DESCRIPTORS = new IPropertyDescriptor[] {
	            new TextPropertyDescriptor(NAME, "Name"),
	            new TextPropertyDescriptor(ID, "Id") {
	            	public CellEditor createPropertyEditor(Composite parent) {
	                    return null;
	                }
	            },
	        };
    	}
    }
    
    private Object element;
    private ElementContainer parent;
    private transient Rectangle constraint;
    private List<ElementConnection> incomingConnections = new ArrayList<ElementConnection>();
    private List<ElementConnection> outgoingConnections = new ArrayList<ElementConnection>();
    private transient List<ModelListener> listeners = new ArrayList<ModelListener>();
    protected Color color;
    
    protected void setElement(Object element) {
		this.element = element;
	}

	public Object getElement() {
		return element;
	}

	public void setConstraint(Rectangle constraint) {
	    this.constraint = constraint;
		internalSetConstraint(constraint);
		notifyListeners(CHANGE_CONSTRAINT);
	}
	
	protected abstract void internalSetConstraint(Rectangle constraint);
	
	public Rectangle getConstraint() {
	    if (constraint == null) {
	        constraint = internalGetConstraint();
	    }
	    return constraint;
	}
	
	protected abstract Rectangle internalGetConstraint();

	public void setParent(ElementContainer parent) {
		this.parent = parent;
	}

	public ElementContainer getParent() {
		return parent;
	}

	public List<ElementConnection> getOutgoingConnections() {
		return Collections.unmodifiableList(outgoingConnections);
	}

	public List<ElementConnection> getIncomingConnections() {
		return Collections.unmodifiableList(incomingConnections);
	}

	public void addIncomingConnection(ElementConnection connection) {
	    localAddIncomingConnection(connection);
		internalAddIncomingConnection(connection);
		notifyListeners(CHANGE_INCOMING_CONNECTIONS);
	}
	
	public void localAddIncomingConnection(ElementConnection connection) {
	    incomingConnections.add(connection);
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
	    localAddOutgoingConnection(connection);
		internalAddOutgoingConnection(connection);
		notifyListeners(CHANGE_OUTGOING_CONNECTIONS);
	}

    public void localAddOutgoingConnection(ElementConnection connection) {
        outgoingConnections.add(connection);
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
	
	public Color getColor() {
		if (color == null) {
			color = internalGetColor();
		}
		return color;
	}
	
	protected Color internalGetColor() {
		return null;
	}
	
	public void setColor(Color color) {
		this.color = color;
		internalSetColor(color == null ? null : RGBToInteger(color.getRGB()));
		notifyListeners(CHANGE_NAME);
	}

	protected void internalSetColor(Integer color) {
	}
	
	public void addListener(ModelListener listener) {
		listeners.add(listener);
	}

	public void removeListener(ModelListener listener) {
		listeners.remove(listener);
	}

	protected void notifyListeners(int change) {
		ModelEvent event = new ModelEvent(change);
		for (ModelListener listener: listeners) {
			listener.modelChanged(event);
		}
	}

	private void readObject(ObjectInputStream aInputStream)
			throws ClassNotFoundException, IOException {
		aInputStream.defaultReadObject();
		listeners = new ArrayList<ModelListener>();
	}

	public IPropertyDescriptor[] getPropertyDescriptors() {
		return DESCRIPTORS;
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
		if (ID.equals(id)) {
			return getId();
		}
		if (COLOR.equals(id)) {
			return getColor();
		}
		return null;
	}

	public void resetPropertyValue(Object id) {
		if (NAME.equals(id)) {
			setName("");
		}
		if (COLOR.equals(id)) {
			setColor(null);
		}
	}

	public void setPropertyValue(Object id, Object value) {
		if (NAME.equals(id)) {
			setName((String) value);
		}
		if (COLOR.equals(id)) {
			setColor((Color) value);
		}
	}
	
	public static Integer RGBToInteger(RGB rgb) {
		return new Integer((rgb.blue << 16) | (rgb.green << 8) | rgb.red);
	}

	public static RGB integerToRGB(Integer color) {
		int n = color.intValue();
		return new RGB(			
			(n & 0x000000FF),
			(n & 0x0000FF00) >> 8,
			(n & 0x00FF0000) >> 16);
	}
}
