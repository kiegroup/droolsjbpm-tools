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

import org.drools.eclipse.flow.common.view.datatype.editor.Editor;
import org.drools.process.core.datatype.DataType;
import org.drools.process.core.datatype.impl.type.FloatDataType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * Default float editor.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class FloatEditor extends Composite implements Editor {

    private Text text;
    
    public FloatEditor(Composite parent) {
        super(parent, SWT.NONE);
        setLayout(new FillLayout());
        text = new Text(this, SWT.NONE);
    }

    public void setDataType(DataType dataType) {
        if (!(dataType instanceof FloatDataType)) {
            throw new IllegalArgumentException("Illegal data type " + dataType);
        }
    }

    public Object getValue() throws IllegalArgumentException {
        String valueString = text.getText();
        if ("".equals(valueString)) {
            return null;
        }
        try {
            return new Float(valueString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The value " + valueString
                + " is not a valid float.");
        }
    }
    
    public void setValue(Object value) {
        if (value == null) {
            text.setText("");
        } else if (value instanceof Float) {
            text.setText(((Float) value).toString());
        } else {
            throw new IllegalArgumentException("Value must be a float: " + value);
        }
    }
    
    public void reset() {
        text.setText("");
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        text.setEnabled(enabled);
    }
}
