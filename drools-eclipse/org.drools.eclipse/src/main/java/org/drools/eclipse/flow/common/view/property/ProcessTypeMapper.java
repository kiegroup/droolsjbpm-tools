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

package org.drools.eclipse.flow.common.view.property;

import org.drools.eclipse.flow.common.editor.editpart.ElementConnectionEditPart;
import org.drools.eclipse.flow.common.editor.editpart.ElementEditPart;
import org.drools.eclipse.flow.common.editor.editpart.ProcessEditPart;
import org.eclipse.ui.views.properties.tabbed.ITypeMapper;

public class ProcessTypeMapper implements ITypeMapper {
    
    public ProcessTypeMapper() {
    }

    public Class<?> mapType(Object object) {
        if (object instanceof ElementEditPart) {
            return ((ElementEditPart) object).getModel().getClass();
        }
        if (object instanceof ProcessEditPart) {
            return ((ProcessEditPart) object).getModel().getClass();
        }
        if (object instanceof ElementConnectionEditPart) {
            return ((ElementConnectionEditPart) object).getModel().getClass();
        }
        return object.getClass();
    }

}
