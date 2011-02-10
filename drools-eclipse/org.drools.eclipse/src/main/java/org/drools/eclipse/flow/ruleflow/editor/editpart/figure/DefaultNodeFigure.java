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

package org.drools.eclipse.flow.ruleflow.editor.editpart.figure;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.flow.common.editor.editpart.figure.AbstractElementFigure;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

public class DefaultNodeFigure extends AbstractElementFigure {
        
    private Image icon;
    private Color color;
        
    private RoundedRectangle rectangle;
    
    public void setIcon(String iconName) {
        icon = ImageDescriptor.createFromURL(
            DroolsEclipsePlugin.getDefault().getBundle()
                .getEntry(iconName)).createImage();
    }
    
    public void setColor(RGB rgb) {
        color = new Color(Display.getCurrent(), rgb);
    }
    
    protected void customizeFigure() {
        rectangle = new RoundedRectangle();
        rectangle.setCornerDimensions(new Dimension(25, 25));
        add(rectangle, 0);
        rectangle.setBackgroundColor(color);
        rectangle.setBounds(getBounds());
        setSelected(false);
        setIcon(icon);
    }
    
    public void setBounds(Rectangle rectangle) {
        super.setBounds(rectangle);
        this.rectangle.setBounds(rectangle);
    }
    
    public void setSelected(boolean b) {
        super.setSelected(b);
        rectangle.setLineWidth(b ? 3 : 1);
        repaint();
    }
    
}
