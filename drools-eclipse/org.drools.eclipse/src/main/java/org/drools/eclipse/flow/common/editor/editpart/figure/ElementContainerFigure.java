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

package org.drools.eclipse.flow.common.editor.editpart.figure;

import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.FreeformViewport;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Layer;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.ScrollPane;
import org.eclipse.draw2d.StackLayout;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

public class ElementContainerFigure extends Figure implements ElementFigure {
    
    private IFigure pane;
    private boolean selected = false;
    private Label label = new Label();
    
    public ElementContainerFigure() {
        setSize(200, 150);
        setBorder(new LineBorder(1));
        ScrollPane scrollpane = new ScrollPane();
        pane = new FreeformLayer();
        pane.setLayoutManager(new FreeformLayout());
        setLayoutManager(new StackLayout());
        add(scrollpane);
        IFigure panel = new Layer();
        FlowLayout flowLayout = new FlowLayout();
        flowLayout.setMajorAlignment(FlowLayout.ALIGN_CENTER);
        panel.setLayoutManager(flowLayout);
        panel.add(label);
        add(panel);
        scrollpane.setViewport(new FreeformViewport());
        scrollpane.setContents(pane);
    }

    public Label getLabel() {
        return label;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setIcon(Image icon) {
        // Do nothing
    }

    public void setSelected(boolean b) {
        this.selected = b;
    }

    public void setText(String text) {
        label.setText(text);
    }
    
    public IFigure getPane() {
        return pane;
    }

    public void setColor(Color newColor) {
    }

}
