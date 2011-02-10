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

package org.drools.eclipse.flow.ruleflow.editor.editpart.figure.bpmn2;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.flow.common.editor.editpart.figure.AbstractElementFigure;
import org.drools.eclipse.flow.ruleflow.editor.editpart.JoinEditPart.JoinFigureInterface;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.jbpm.workflow.core.node.Join;

public class BPMNJoinFigure extends AbstractElementFigure implements JoinFigureInterface {
    
    private static final Image ICON_COMPLEX = ImageDescriptor.createFromURL(
        DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn2/large/gateway_complex.png")).createImage();
    private static final Image ICON_AND = ImageDescriptor.createFromURL(
        DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn2/large/gateway_parallel.png")).createImage();
    private static final Image ICON_XOR = ImageDescriptor.createFromURL(
        DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/bpmn2/large/gateway_exclusive.png")).createImage();
                
    public BPMNJoinFigure() {
        super.setSize(49, 49);
    }

    public void setText(String text) {
    }
    
    public void setBounds(Rectangle r) {
        r.setSize(49, 49);
        super.setBounds(r);
    }
    
    public void setType(int type) {
        if (type == Join.TYPE_AND) {
            setIcon(ICON_AND);
        } else if (type == Join.TYPE_XOR) {
            setIcon(ICON_XOR);
        } else {
            setIcon(ICON_COMPLEX);
        }
    }
    
    protected void customizeFigure() {
        setIcon(ICON_COMPLEX);
    }
    
}
