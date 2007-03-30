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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.drools.ruleflow.common.core.IProcess;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * A wrapper for process element.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public abstract class ProcessWrapper implements IPropertySource, Serializable {

	public static final int CHANGE_ELEMENTS = 1;
	
    protected static IPropertyDescriptor[] descriptors;

    public static final String NAME = "name";
    public static final String VERSION = "version";
    public static final String ID = "id";
    static {
        descriptors = new IPropertyDescriptor[] {
            new TextPropertyDescriptor(NAME, "Name"),
            new TextPropertyDescriptor(VERSION, "Version"),
            new TextPropertyDescriptor(ID, "Id"),
        };
    }
    
    private IProcess process;
    private Map elements = new HashMap();
    private transient List listeners = new ArrayList();
    
    public ProcessWrapper() {
        process = createProcess();
    }

    protected abstract IProcess createProcess();

    public IProcess getProcess() {
        return process;
    }
    
    public String getName() {
        return process.getName() == null ? "" : process.getName();
    }
    
    public void setName(String name) {
        process.setName(name);
    }
    
    public String getVersion() {
        return process.getVersion() == null ? "" : process.getVersion();
    }
    
    public void setVersion(String version) {
        process.setVersion(version);
    }
    
    public String getId() {
    	return process.getId();
    }
    
    public void setId(String id) {
    	process.setId(id);
    }
    
    public List getElements() {
        return Collections.unmodifiableList(new ArrayList(elements.values()));
    }
    
    public ElementWrapper getElement(String id) {
        return (ElementWrapper) elements.get(id);
    }
    
    public void addElement(ElementWrapper element) {
        internalAddElement(element);
		//id is set in methode above
		elements.put(element.getId(), element);
		notifyListeners(CHANGE_ELEMENTS);
    }
    
    protected abstract void internalAddElement(ElementWrapper element);
    
    public void removeElement(ElementWrapper element) {
        elements.remove(element.getId());
        notifyListeners(CHANGE_ELEMENTS);
        internalRemoveElement(element);
    }
    
    protected abstract void internalRemoveElement(ElementWrapper element);
    
    public void addListener(ModelListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ModelListener listener) {
        listeners.remove(listener);
    }
    
    public void notifyListeners(int change) {
        ModelEvent event = new ModelEvent(change);
        for (Iterator it = listeners.iterator(); it.hasNext(); ) {
        	ModelListener listener = (ModelListener) it.next();
        	listener.modelChanged(event);
        }
    }
    
    private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
        aInputStream.defaultReadObject();
        listeners = new ArrayList();
    }
    
    public Object getEditableValue() {
        return this;
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
        return descriptors;
    }

    public Object getPropertyValue(Object id) {
        if (NAME.equals(id)) {
            return getName();
        }
        if (VERSION.equals(id)) {
            return getVersion();
        }
        if (ID.equals(id)) {
            return getId() + "";
        }
        return null;
    }

    public boolean isPropertySet(Object id) {
        return true;
    }

    public void resetPropertyValue(Object id) {
        if (NAME.equals(id)) {
            setName("");
        }
        if (VERSION.equals(id)) {
            setVersion("");
        }
        if (ID.equals(id)) {
            setId("");
        }
    }

    public void setPropertyValue(Object id, Object value) {
        if (NAME.equals(id)) {
            setName((String) value);
        } else if (VERSION.equals(id)) {
            setVersion((String) value);
        } else if (ID.equals(id)) {
            setId((String) value);
        }
    }
}
