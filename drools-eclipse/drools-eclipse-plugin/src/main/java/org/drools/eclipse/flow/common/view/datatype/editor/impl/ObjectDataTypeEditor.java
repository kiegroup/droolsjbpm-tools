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

import java.util.ArrayList;
import java.util.List;

import org.drools.eclipse.flow.common.view.datatype.editor.DataTypeEditor;
import org.drools.eclipse.flow.common.view.datatype.editor.Editor;
import org.drools.process.core.datatype.DataType;
import org.drools.process.core.datatype.impl.type.ObjectDataType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Default empty editor.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class ObjectDataTypeEditor extends Composite implements Editor, DataTypeEditor {

    private ObjectDataType dataType;
    private Object value;
    private Label label;
    private Text text;
    private List<DataTypeEditor.DataTypeListener> listeners = new ArrayList<DataTypeEditor.DataTypeListener>();
    
    public ObjectDataTypeEditor(Composite parent) {
        super(parent, SWT.NONE);
        setLayout(new FillLayout());
        label = new Label(this, SWT.NONE);
        label.setText("ClassName");
        text = new Text(this, SWT.NONE);
        text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				notifyListeners();
			}
        });
    }
    
    public DataType getDataType() {
    	dataType.setClassName(text.getText());
        return dataType;
    }
    
    public void setDataType(DataType dataType) {
        this.dataType = (ObjectDataType) dataType;
        String className = this.dataType.getClassName();
        text.setText(className == null ? "" : className);
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public void reset() {
        text.setText("");
    }

    public void addListener(DataTypeEditor.DataTypeListener listener) {
        listeners.add(listener);
    }
    
    public void removeListener(DataTypeEditor.DataTypeListener listener) {
        listeners.remove(listener);
    }
    
    private void notifyListeners() {
    	for (DataTypeEditor.DataTypeListener listener: listeners) {
    		listener.dataTypeChanged(getDataType());
    	}
    }
    
    public void setBackground(Color color) {
    	super.setBackground(color);
    	label.setBackground(color);
    }
}
