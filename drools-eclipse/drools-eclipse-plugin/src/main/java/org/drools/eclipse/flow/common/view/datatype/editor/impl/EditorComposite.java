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

import java.io.Serializable;import java.lang.reflect.InvocationTargetException;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.flow.common.datatype.DataTypeRegistry;
import org.drools.eclipse.flow.common.view.datatype.editor.Editor;
import org.drools.process.core.datatype.DataType;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

/**
 * Container for an editor.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class EditorComposite extends Composite {
    
    private DataTypeRegistry registry;
    private Editor editor;
    
    public EditorComposite(Composite parent, int style, DataTypeRegistry registry) {
        super(parent, style);
        this.registry = registry;
        setLayout(new FillLayout());
        editor = new EmptyEditor(this);
    }
    
    public void setDataType(DataType type) {
        ((Composite) editor).dispose();
        if (type == null) {
            editor = new EmptyEditor(this);
        } else {
        	Class editorClass = null;
            try {
                editorClass = registry.getDataTypeInfo(type.getClass()).getValueEditorClass();
                editor = (Editor) editorClass.getConstructor(
                    new Class[] { Composite.class }).newInstance(new Object[] { this });
            } catch (IllegalArgumentException e) {
                // "Could not find data type info for type " + type.getClass()
            	DroolsEclipsePlugin.log(e);
                editor = new EmptyEditor(this);
            } catch (InstantiationException e) {
                // "Could not create editor for editor " + editorClass
            	DroolsEclipsePlugin.log(e);
                editor = new EmptyEditor(this);
            } catch (NoSuchMethodException e) {
            	// "Could not create editor for editor " + editorClass
            	DroolsEclipsePlugin.log(e);
                editor = new EmptyEditor(this);
            } catch (InvocationTargetException e) {
            	// "Could not create editor for editor " + editorClass
            	DroolsEclipsePlugin.log(e);
                editor = new EmptyEditor(this);
            } catch (IllegalAccessException e) {
            	// "Could not create editor for editor " + editorClass
            	DroolsEclipsePlugin.log(e);
                editor = new EmptyEditor(this);
            }
            editor.setDataType(type);
            layout();
        }
    }
    
    public void setValue(Serializable value) {
        editor.setValue(value);
    }
    
    public Serializable getValue() {
        return editor.getValue();
    }

    public void reset() {
        editor.reset();
    }
    
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        ((Composite) editor).setEnabled(enabled);
    }
}
