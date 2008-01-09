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

import java.io.Serializable;import org.drools.eclipse.flow.common.view.datatype.editor.DataTypeEditor;
import org.drools.eclipse.flow.common.view.datatype.editor.Editor;
import org.drools.process.core.datatype.DataType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Default empty editor.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class EmptyEditor extends Composite implements Editor, DataTypeEditor {

    private DataType dataType;
    private Serializable value;
    private Label label;
    
    public EmptyEditor(Composite parent) {
        super(parent, SWT.NONE);
        setSize(0, 0);
        setLayout(new FillLayout());
        label = new Label(this, SWT.NONE);
    }
    
    public DataType getDataType() {
        return dataType;
    }
    
    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }

    public Serializable getValue() {
        return value;
    }

    public void setValue(Serializable value) {
        this.value = value;
    }

    public void reset() {
        // do nothing
    }

    public void addListener(DataTypeEditor.DataTypeListener listener) {
        // do nothing
    }
    
    public void removeListener(DataTypeEditor.DataTypeListener listener) {
        // do nothing
    }
    
    public void setBackground(Color color) {
    	super.setBackground(color);
    	label.setBackground(color);
    }
}
