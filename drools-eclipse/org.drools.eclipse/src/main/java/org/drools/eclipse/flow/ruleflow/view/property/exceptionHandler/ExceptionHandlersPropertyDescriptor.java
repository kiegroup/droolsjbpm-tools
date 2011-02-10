/*
 * Copyright 2010 JBoss Inc
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

package org.drools.eclipse.flow.ruleflow.view.property.exceptionHandler;

import org.drools.definition.process.Process;
import org.drools.eclipse.flow.common.view.property.ListPropertyDescriptor;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;

public class ExceptionHandlersPropertyDescriptor extends ListPropertyDescriptor {

    private Process process;

    public ExceptionHandlersPropertyDescriptor(Object id, String displayName, Process process) {
        super(id, displayName, ExceptionHandlersCellEditor.class);
        this.process = process;
    }

    public CellEditor createPropertyEditor(Composite parent) {
        return new ExceptionHandlersCellEditor(parent, process);
    }

}
