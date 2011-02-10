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

package org.drools.eclipse.flow.common.editor.editpart;

import org.eclipse.draw2d.AbstractLayout;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Dimension;

public class GraphLayoutManager extends AbstractLayout {

    private ProcessEditPart diagram;

    public GraphLayoutManager(ProcessEditPart diagram) {
        this.diagram = diagram;
    }

    protected Dimension calculatePreferredSize(IFigure container, int wHint, int hHint) {
        container.validate();
        return container.getSize();
    }


    public void layout(IFigure container) {
        new DirectedGraphLayoutVisitor().layoutDiagram(diagram);
        // diagram.setTableModelBounds();
    }

}
