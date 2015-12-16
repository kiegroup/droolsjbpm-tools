/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
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

package org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.flow.common.editor.editpart.figure.AbstractElementFigure;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;

public class BPMNFaultNodeFigure extends AbstractElementFigure {
    
    private static final Image icon = ImageDescriptor.createFromURL(
        DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn/large/intermediate_error_10.png")).createImage();
        
    public BPMNFaultNodeFigure() {
        setSize(48, 48);
    }
    
    public void setText(String text) {
    }
    
    public void setBounds(Rectangle r) {
        r.setSize(48, 48);
        super.setBounds(r);
    }
    
    protected void customizeFigure() {
        setIcon(icon);
    }
    
    public void setSelected(boolean b) {
        super.setSelected(b);
        ((LineBorder) getBorder()).setWidth(b ? 3 : 0);
        repaint();
    }
    
}
