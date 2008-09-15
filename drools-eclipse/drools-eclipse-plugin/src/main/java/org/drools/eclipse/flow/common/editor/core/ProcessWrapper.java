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
import java.util.List;
import java.util.Map;

import org.drools.eclipse.flow.common.view.property.ListPropertyDescriptor;
import org.drools.eclipse.flow.ruleflow.view.property.exceptionHandler.ExceptionHandlersPropertyDescriptor;
import org.drools.eclipse.flow.ruleflow.view.property.swimlane.SwimlanesCellEditor;
import org.drools.eclipse.flow.ruleflow.view.property.variable.VariableListCellEditor;
import org.drools.process.core.Process;
import org.drools.process.core.context.exception.ExceptionHandler;
import org.drools.process.core.context.exception.ExceptionScope;
import org.drools.process.core.context.swimlane.Swimlane;
import org.drools.process.core.context.swimlane.SwimlaneContext;
import org.drools.process.core.context.variable.Variable;
import org.drools.process.core.context.variable.VariableScope;
import org.eclipse.ui.views.properties.ComboBoxPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * A wrapper for process element.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public abstract class ProcessWrapper implements ElementContainer, IPropertySource, Serializable {

	private static final long serialVersionUID = 4L;
	
	public static final int CHANGE_ELEMENTS = 1;
	public static final int CHANGE_ROUTER_LAYOUT = 2;

    public static final Integer ROUTER_LAYOUT_MANUAL = new Integer(0);
    public static final Integer ROUTER_LAYOUT_MANHATTAN = new Integer(1);
    public static final Integer ROUTER_LAYOUT_SHORTEST_PATH = new Integer(2);

    public static final String NAME = "name";
    public static final String VERSION = "version";
    public static final String ID = "id";
    public static final String PACKAGE_NAME = "packageName";
    public static final String ROUTER_LAYOUT = "routerLayout";
    public static final String VARIABLES = "variables";
    public static final String SWIMLANES = "swimlanes";
    public static final String EXCEPTION_HANDLERS = "exceptionHandlers";
     
    private Process process;
    private Map<String, ElementWrapper> elements = new HashMap<String, ElementWrapper>();
    private transient List<ModelListener> listeners = new ArrayList<ModelListener>();
    protected IPropertyDescriptor[] descriptors;
    
    public ProcessWrapper() {
        process = createProcess();
    }
    
    protected abstract Process createProcess();

    public Process getProcess() {
        return process;
    }
    
    public void localSetProcess(Process process) {
        this.process = process;
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
    
    public String getPackageName() {
        return process.getPackageName() == null ? "" : process.getPackageName();
    }
    
    public void setPackageName(String packageName) {
        process.setPackageName(packageName);
    }
    
    public Integer getRouterLayout() {
        Integer routerLayout = (Integer) process.getMetaData("routerLayout");
    	if (routerLayout == null) {
    		return ROUTER_LAYOUT_MANUAL;
    	}
    	return routerLayout;
    }
    
    public void setRouterLayout(Integer routerLayout) {
    	process.setMetaData("routerLayout", routerLayout);
    	notifyListeners(CHANGE_ROUTER_LAYOUT);
    }
    
    public List<ElementWrapper> getElements() {
        return Collections.unmodifiableList(
            new ArrayList<ElementWrapper>(elements.values()));
    }
    
    public ElementWrapper getElement(String id) {
        return (ElementWrapper) elements.get(id);
    }
    
    public void addElement(ElementWrapper element) {
        internalAddElement(element);
		//id is set in methode above
		localAddElement(element);
		notifyListeners(CHANGE_ELEMENTS);
    }
    
    public void localAddElement(ElementWrapper element) {
        elements.put(element.getId(), element);
    }
    
    protected abstract void internalAddElement(ElementWrapper element);
    
    public void removeElement(ElementWrapper element) {
        elements.remove(element.getId());
        notifyListeners(CHANGE_ELEMENTS);
        internalRemoveElement(element);
    }
    
    protected abstract void internalRemoveElement(ElementWrapper element);
    
    public ProcessWrapper getProcessWrapper() {
        return this;
    }
    
    public void addListener(ModelListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ModelListener listener) {
        listeners.remove(listener);
    }
    
    public void notifyListeners(int change) {
        ModelEvent event = new ModelEvent(change);
        for (ModelListener listener: listeners) {
        	listener.modelChanged(event);
        }
    }
    
    private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
        aInputStream.defaultReadObject();
        listeners = new ArrayList<ModelListener>();
    }
    
    public Object getEditableValue() {
        return this;
    }

    public IPropertyDescriptor[] getPropertyDescriptors() {
    	if (descriptors == null) {
    		initPropertyDescriptors();
    	}
        return descriptors;
    }

    public void initPropertyDescriptors() {
        descriptors = new IPropertyDescriptor[] {
            new TextPropertyDescriptor(NAME, "Name"),
            new TextPropertyDescriptor(VERSION, "Version"),
            new TextPropertyDescriptor(ID, "Id"),
            new TextPropertyDescriptor(PACKAGE_NAME, "Package"),
            new ComboBoxPropertyDescriptor(ROUTER_LAYOUT, "Connection Layout", 
                new String[] { "Manual", "Manhatten", "Shortest Path" }),
            new ListPropertyDescriptor(VARIABLES, "Variables", VariableListCellEditor.class),
            new ListPropertyDescriptor(SWIMLANES, "Swimlanes",
                SwimlanesCellEditor.class),
            new ExceptionHandlersPropertyDescriptor(EXCEPTION_HANDLERS,
        		"Exception Handlers", process),
        };
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
        if (PACKAGE_NAME.equals(id)) {
            return getPackageName();
        }
        if (ROUTER_LAYOUT.equals(id)) {
            return getRouterLayout();
        }
        if (VARIABLES.equals(id)) {
            return ((VariableScope) getProcess().getDefaultContext(VariableScope.VARIABLE_SCOPE)).getVariables();
        }
        if (SWIMLANES.equals(id)) {
            return ((SwimlaneContext) getProcess().getDefaultContext(SwimlaneContext.SWIMLANE_SCOPE)).getSwimlanes();
        }
        if (EXCEPTION_HANDLERS.equals(id)) {
            return ((ExceptionScope) getProcess().getDefaultContext(ExceptionScope.EXCEPTION_SCOPE)).getExceptionHandlers();
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
        if (PACKAGE_NAME.equals(id)) {
            setPackageName("");
        }
        if (ROUTER_LAYOUT.equals(id)) {
            setRouterLayout(null);
        }
        if (VARIABLES.equals(id)) {
            ((VariableScope) getProcess().getDefaultContext(
                VariableScope.VARIABLE_SCOPE)).setVariables(new ArrayList<Variable>());
        }
        if (SWIMLANES.equals(id)) {
            ((SwimlaneContext) getProcess().getDefaultContext(
                SwimlaneContext.SWIMLANE_SCOPE)).setSwimlanes(new ArrayList<Swimlane>());
        }
        if (EXCEPTION_HANDLERS.equals(id)) {
            ((ExceptionScope) getProcess().getDefaultContext(
                ExceptionScope.EXCEPTION_SCOPE)).setExceptionHandlers(new HashMap<String, ExceptionHandler>());
        }
    }

    @SuppressWarnings("unchecked")
	public void setPropertyValue(Object id, Object value) {
        if (NAME.equals(id)) {
            setName((String) value);
        } else if (VERSION.equals(id)) {
            setVersion((String) value);
        } else if (ID.equals(id)) {
            setId((String) value);
        } else if (PACKAGE_NAME.equals(id)) {
            setPackageName((String) value);
        } else if (ROUTER_LAYOUT.equals(id)) {
            setRouterLayout((Integer) value);
        } else if (VARIABLES.equals(id)) {
            ((VariableScope) getProcess().getDefaultContext(
                VariableScope.VARIABLE_SCOPE)).setVariables((List<Variable>) value);
        } else if (SWIMLANES.equals(id)) {
            ((SwimlaneContext) getProcess().getDefaultContext(
                SwimlaneContext.SWIMLANE_SCOPE)).setSwimlanes((List<Swimlane>) value);
        } else if (EXCEPTION_HANDLERS.equals(id)) {
        	((ExceptionScope) getProcess().getDefaultContext(
                ExceptionScope.EXCEPTION_SCOPE)).setExceptionHandlers((Map<String, ExceptionHandler>) value);
        }
    }
}
