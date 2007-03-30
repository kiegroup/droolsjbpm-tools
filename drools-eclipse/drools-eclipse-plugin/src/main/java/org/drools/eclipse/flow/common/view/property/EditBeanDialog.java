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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog for editing a JavaBean.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public abstract class EditBeanDialog extends Dialog {

    private String title;
    private Object value;
    
    protected EditBeanDialog(Shell parentShell, String title) {
        super(parentShell);
        this.title = title;
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(title);
    }
    
    protected Point getInitialSize() {
        return new Point(400, 200);
    }
    
    public Object getValue() {
        return value;
    }
    
    protected void okPressed() {
        try {
            value = updateValue(value);
            super.okPressed();
        } catch (IllegalArgumentException e) {
            // value could not be set, ignoring ok
        }
    }
    
    protected abstract Object updateValue(Object value);
    
    public void setValue(Object value) {
        this.value = value;
    }
}
