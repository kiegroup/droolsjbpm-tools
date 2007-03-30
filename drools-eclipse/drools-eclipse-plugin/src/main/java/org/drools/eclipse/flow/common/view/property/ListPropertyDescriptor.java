package org.drools.eclipse.flow.common.view.property;
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

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.views.properties.PropertyDescriptor;

/**
 * Property descriptor for a list.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class ListPropertyDescriptor extends PropertyDescriptor {
    
    private Class cellEditorClass;
    
    public ListPropertyDescriptor(Object id, String displayName, Class cellEditorClass) {
        super(id, displayName);
        this.cellEditorClass = cellEditorClass;
    }
    
    public CellEditor createPropertyEditor(Composite parent) {
        try {
            CellEditor editor = (CellEditor) cellEditorClass.getConstructor(new Class[] { Composite.class }).newInstance(new Object[] { parent });
            if (getValidator() != null) {
                editor.setValidator(getValidator());
            }
            return editor;
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}