package org.drools.eclipse.flow.ruleflow.editor.editpart;
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

import java.lang.reflect.Constructor;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.DroolsPluginImages;
import org.drools.eclipse.builder.DroolsBuilder;
import org.drools.eclipse.flow.common.editor.editpart.ElementEditPart;
import org.drools.eclipse.flow.common.editor.editpart.figure.AbstractElementFigure;
import org.drools.eclipse.flow.ruleflow.core.WorkItemWrapper;
import org.drools.eclipse.util.ProjectClassLoader;
import org.drools.process.core.WorkDefinition;
import org.drools.process.core.WorkDefinitionExtension;
import org.drools.process.core.WorkEditor;
import org.drools.workflow.core.node.WorkItemNode;
import org.eclipse.core.resources.IProject;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * EditPart for a Task node.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class WorkItemEditPart extends ElementEditPart {

    private static final Color color = new Color(Display.getCurrent(), 255, 250, 205);

    protected IFigure createFigure() {
        String icon = null;
        WorkDefinition workDefinition = getWorkDefinition();
        if (workDefinition instanceof WorkDefinitionExtension) {
            icon = ((WorkDefinitionExtension) workDefinition).getIcon();
        }
        if (icon == null) {
            icon = "icons/action.gif";
        }
        Image image = DroolsPluginImages.getImage(icon);
        WorkItemFigure figure = new WorkItemFigure();
        if (image == null) {
            IProject project = getEditor().getFile().getProject();
            if (project != null) {
                ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
                ClassLoader newLoader = DroolsBuilder.class.getClassLoader();
                try {
                    if (project.getNature("org.eclipse.jdt.core.javanature") != null) {
                        IJavaProject javaProject = JavaCore.create(project);
                        newLoader = ProjectClassLoader.getProjectClassLoader(javaProject);
                    }
                    try {
                        Thread.currentThread().setContextClassLoader(newLoader);
                        image = ImageDescriptor.createFromURL(
                            newLoader.getResource(icon)).createImage();
                        DroolsPluginImages.putImage(icon, image);
                    } finally {
                        Thread.currentThread().setContextClassLoader(oldLoader);
                    }
                } catch (Exception e) {
                    DroolsEclipsePlugin.log(e);
                }
            }
        }
        figure.setIcon(image);
        return figure;
    }
    
    protected WorkItemWrapper getWorkItemWrapper() {
        return (WorkItemWrapper) getElementWrapper();
    }
    
    private WorkDefinition getWorkDefinition() {
        return getWorkItemWrapper().getWorkDefinition();
    }
    
    protected void doubleClicked() {
        super.doubleClicked();
        // open custom editor pane if one exists
        WorkDefinition workDefinition = getWorkDefinition();
        if (workDefinition instanceof WorkDefinitionExtension) {
            String editor = ((WorkDefinitionExtension) workDefinition).getCustomEditor();
            if (editor != null) {
                openEditor(editor, workDefinition);
            }
        }
    }
    
    private void openEditor(String editorClassName, WorkDefinition workDefinition) {
        IProject project = getEditor().getFile().getProject();
        if (project != null) {
            ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
            ClassLoader newLoader = DroolsBuilder.class.getClassLoader();
            try {
                if (project.getNature("org.eclipse.jdt.core.javanature") != null) {
                    IJavaProject javaProject = JavaCore.create(project);
                    newLoader = ProjectClassLoader.getProjectClassLoader(javaProject);
                }
                try {
                    Thread.currentThread().setContextClassLoader(newLoader);
                    Class<WorkEditor> editorClass = (Class<WorkEditor>) newLoader.loadClass(editorClassName);
                    Constructor<WorkEditor> constructor = editorClass.getConstructor(Shell.class);
                    WorkEditor editor = constructor.newInstance(getViewer().getControl().getShell());
                    editor.setWorkDefinition(workDefinition);
                    WorkItemNode workItemNode = getWorkItemWrapper().getWorkItemNode();
                    editor.setWork(workItemNode.getWork());
                    editor.show();
                    workItemNode.setWork(editor.getWork());
                } finally {
                    Thread.currentThread().setContextClassLoader(oldLoader);
                }
            } catch (Exception e) {
                DroolsEclipsePlugin.log(e);
            }
        }
    }
    
    public static class WorkItemFigure extends AbstractElementFigure {
        
        private RoundedRectangle rectangle;
        
        protected void customizeFigure() {
            rectangle = new RoundedRectangle();
            rectangle.setCornerDimensions(new Dimension(25, 25));
            add(rectangle, 0);
            rectangle.setBackgroundColor(color);
            rectangle.setBounds(getBounds());
            setSelected(false);
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
}
