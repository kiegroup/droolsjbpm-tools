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

import java.io.Serializable;import org.drools.eclipse.flow.common.view.datatype.editor.Editor;
import org.drools.ruleflow.common.datatype.DataType;
import org.drools.ruleflow.common.datatype.impl.type.BooleanDataType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

/**
 * Default boolean editor.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class BooleanEditor extends Composite implements Editor {

    private static final String[] TF = { "true", "false" };
    
    private Combo combo;
    
    public BooleanEditor(Composite parent) {
        super(parent, SWT.NONE);
        setLayout(new FillLayout());
        combo = new Combo(this, SWT.READ_ONLY);
        combo.setItems(TF);
        combo.select(1);
    }

    public void setDataType(DataType dataType) {
        if (!(dataType instanceof BooleanDataType)) {
            throw new IllegalArgumentException("Illegal data type " + dataType);
        }
    }

    public Serializable getValue() {
        return Boolean.valueOf(combo.getSelectionIndex() == 0);
    }
    
    public void setValue(Serializable value) {
        if (value == null) {
            combo.select(1);
        } else if (value instanceof Boolean) {
            combo.select(((Boolean) value).booleanValue() ? 0 : 1);
        } else {
            throw new IllegalArgumentException("Value must be a boolean: " + value);
        }
    }
    
    public void reset() {
        combo.select(1);
    }
    
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        combo.setEnabled(enabled);
    }
}
