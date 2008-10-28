package org.drools.eclipse.flow.common.editor.editpart.figure;
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

import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.Panel;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;

/**
 * Default implementation of an element Figure.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public abstract class AbstractElementFigure extends Panel implements ElementFigure {
    
    private boolean selected;
    private Label label = new Label();

    public AbstractElementFigure() {
        add(label);
        customizeFigure();
        setSize(80, 40);
    }
    
    public void setIcon(Image icon) {
        label.setIcon(icon);
    }
    
    public void setText(String text) {
        label.setText(text);
    }
    
    public Label getLabel() {
        return label;
    }
    
    public void setBounds(Rectangle bounds) {
        super.setBounds(bounds);
        label.setBounds(bounds);
    }
    
    protected abstract void customizeFigure();
    
    public void setSelected(boolean b) {
        selected = b;
    }
    
    public boolean isSelected() {
        return selected;
    }

    public void setFocus(boolean b) {
        repaint();
    }

	public void setColor(Color newColor) {
	}
	
}
