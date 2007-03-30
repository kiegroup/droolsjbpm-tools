package org.drools.eclipse.flow.common.editor.policy;
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

import org.drools.eclipse.flow.common.editor.core.ElementWrapper;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.tools.CellEditorLocator;
import org.eclipse.gef.tools.DirectEditManager;
import org.eclipse.swt.widgets.Text;

/**
 * Manager for directly editing elements.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class ElementDirectEditManager extends DirectEditManager {

    private ElementWrapper element;

    public ElementDirectEditManager(GraphicalEditPart source, Class editorType,
            CellEditorLocator locator) {
        super(source, editorType, locator);
        element = (ElementWrapper) source.getModel();
    }

    protected void initCellEditor() {
        getCellEditor().setValue(element.getName());
        Text text = (Text) getCellEditor().getControl();
        text.selectAll();
    }
}
