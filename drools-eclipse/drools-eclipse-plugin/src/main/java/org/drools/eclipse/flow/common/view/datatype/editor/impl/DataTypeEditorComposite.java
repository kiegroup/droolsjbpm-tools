package org.drools.eclipse.flow.common.view.datatype.editor.impl;
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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.flow.common.datatype.IDataTypeRegistry;
import org.drools.eclipse.flow.common.view.datatype.editor.IDataTypeEditor;
import org.drools.ruleflow.common.datatype.IDataType;
import org.drools.ruleflow.common.datatype.impl.type.UndefinedDataType;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
/**
 * Default editor for a datatype. 
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class DataTypeEditorComposite extends Composite implements IDataTypeEditor.DataTypeListener {
    private IDataTypeRegistry registry;    private IDataTypeEditor dataTypeEditor;    private List listeners = new ArrayList();   
    public DataTypeEditorComposite(Composite parent, int style, IDataTypeRegistry registry) {        super(parent, style);        this.registry = registry;        setLayout(new FillLayout());        dataTypeEditor = new EmptyEditor(this);    }
    public void setDataType(IDataType dataType) {        if (dataType == null) {            reset();        } else {            // update data type editor            dataTypeEditor.removeListener(this);            ((Composite) dataTypeEditor).dispose();            dataTypeEditor = null;            Class editorClass = null;            try {                editorClass = registry.getDataTypeInfo(dataType.getClass()).getDataTypeEditorClass();                dataTypeEditor = (IDataTypeEditor) editorClass.getConstructor(                    new Class[] { Composite.class }).newInstance(new Object[] { this });            } catch (IllegalArgumentException e) {
            	// "Could not find data type info for type " + dataType.getClass()                DroolsEclipsePlugin.log(e);            } catch (InstantiationException e) {                // "Could not create editor for type " + editorClass                DroolsEclipsePlugin.log(e);
            } catch (NoSuchMethodException e) {                // "Could not create editor for type " + editorClass                DroolsEclipsePlugin.log(e);
            } catch (InvocationTargetException e) {                // "Could not create editor for type " + editorClass                DroolsEclipsePlugin.log(e);
            } catch (IllegalAccessException e) {                // "Could not create editor for type " + editorClass                DroolsEclipsePlugin.log(e);
            }                        if (dataTypeEditor == null) {                dataTypeEditor = new EmptyEditor(this);            }            dataTypeEditor.addListener(this);            dataTypeEditor.setDataType(dataType);            ((Composite) dataTypeEditor).setBackground(getBackground());            layout();        }    }        public IDataType getDataType() {        return dataTypeEditor.getDataType();    }    
   public void reset() {        setDataType(UndefinedDataType.getInstance());    }        public void setEnabled(boolean enabled) {        super.setEnabled(enabled);        ((Composite) dataTypeEditor).setEnabled(enabled);    }        public void addListener(IDataTypeEditor.DataTypeListener listener) {        listeners.add(listener);    }        public void removeListener(IDataTypeEditor.DataTypeListener listener) {        listeners.remove(listener);    }        private void notifyListeners() {        IDataType dataType = getDataType();        for (Iterator it = listeners.iterator(); it.hasNext(); ) {
        	IDataTypeEditor.DataTypeListener listener = (IDataTypeEditor.DataTypeListener) it.next();            listener.dataTypeChanged(dataType);        }    }    public void dataTypeChanged(IDataType dataType) {        notifyListeners();    }        public void setBackground(Color color) {    	super.setBackground(color);    	((Composite) dataTypeEditor).setBackground(color);    }}