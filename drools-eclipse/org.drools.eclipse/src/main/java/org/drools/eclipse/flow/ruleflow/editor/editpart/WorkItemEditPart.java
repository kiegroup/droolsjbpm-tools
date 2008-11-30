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
import java.util.ArrayList;
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.DroolsPluginImages;
import org.drools.eclipse.flow.common.editor.editpart.ElementEditPart;
import org.drools.eclipse.flow.common.editor.editpart.figure.AbstractElementFigure;
import org.drools.eclipse.flow.ruleflow.core.WorkItemWrapper;
import org.drools.eclipse.flow.ruleflow.skin.SkinManager;
import org.drools.eclipse.flow.ruleflow.skin.SkinProvider;
import org.drools.eclipse.preferences.IDroolsConstants;
import org.drools.eclipse.util.ProjectClassLoader;
import org.drools.process.core.Work;
import org.drools.process.core.WorkDefinition;
import org.drools.process.core.WorkDefinitionExtension;
import org.drools.process.core.WorkEditor;
import org.drools.workflow.core.node.WorkItemNode;
import org.eclipse.draw2d.ChopboxAnchor;
import org.eclipse.draw2d.ConnectionAnchor;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.RoundedRectangle;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CommandStack;
import org.eclipse.gef.requests.DropRequest;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.views.properties.IPropertySource;

/**
 * EditPart for a Task node.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class WorkItemEditPart extends ElementEditPart {

	private String SKIN =
		DroolsEclipsePlugin.getDefault().getPreferenceStore().getString(IDroolsConstants.SKIN);

	private static final Color color = new Color(Display.getCurrent(), 255, 250, 205);

    protected IFigure createFigure() {
    	SkinProvider skinProvider = SkinManager.getInstance().getSkinProvider(SKIN);
    	WorkItemFigureInterface figure = skinProvider.createWorkItemFigure();
        String icon = null;
        WorkDefinition workDefinition = getWorkDefinition();
        if (workDefinition instanceof WorkDefinitionExtension) {
            icon = ((WorkDefinitionExtension) workDefinition).getIcon();
        }
        if (icon == null) {
            icon = "icons/action.gif";
        }
        Image image = DroolsPluginImages.getImage(icon);
        if (image == null) {
            IJavaProject javaProject = getProject();
            if (javaProject != null) {
                try {
                    ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
                    ClassLoader newLoader = ProjectClassLoader.getProjectClassLoader(javaProject);
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
    
//    public ConnectionAnchor getSourceConnectionAnchor(Request request) {
//    	Point p = ((DropRequest) request).getLocation();
//    	return ((WorkItemFigure) getFigure()).getOutgoingConnectionAnchorAt(p);
//    }
    
    protected void doubleClicked() {
        super.doubleClicked();
        // open custom editor pane if one exists
        WorkDefinition workDefinition = getWorkDefinition();
        if (workDefinition instanceof WorkDefinitionExtension) {
            String editor = ((WorkDefinitionExtension) workDefinition).getCustomEditor();
            if (editor != null) {
                Work work = openEditor(editor, workDefinition);
                if (work != null) {
	                SetWorkCommand setCommand = new SetWorkCommand();
	        		setCommand.setPropertyValue(work);
	                CommandStack stack = getViewer().getEditDomain().getCommandStack();
	                stack.execute(setCommand);
                }
            }
        }
    }
    
    @SuppressWarnings("unchecked")
	private Work openEditor(String editorClassName, WorkDefinition workDefinition) {
        IJavaProject javaProject = getProject();
        if (javaProject != null) {
            try {
                ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
                ClassLoader newLoader = ProjectClassLoader.getProjectClassLoader(javaProject);
                try {
                    Thread.currentThread().setContextClassLoader(newLoader);
                    Class<WorkEditor> editorClass = (Class<WorkEditor>) newLoader.loadClass(editorClassName);
                    Constructor<WorkEditor> constructor = editorClass.getConstructor(Shell.class);
                    WorkEditor editor = constructor.newInstance(getViewer().getControl().getShell());
                    editor.setWorkDefinition(workDefinition);
                    WorkItemNode workItemNode = getWorkItemWrapper().getWorkItemNode();
                    editor.setWork(workItemNode.getWork());
                    boolean result = editor.show();
                    return result ? editor.getWork() : null;
                } finally {
                    Thread.currentThread().setContextClassLoader(oldLoader);
                }
            } catch (Exception e) {
                DroolsEclipsePlugin.log(e);
            }
        }
        return null;
    }
    
    public static interface WorkItemFigureInterface extends IFigure {
    	void setIcon(Image icon);
    }
    
    public static class WorkItemFigure extends AbstractElementFigure implements WorkItemFigureInterface {
        
        private RoundedRectangle rectangle;
        private ConnectionAnchor defaultConnectionAnchor;
        private List<ConnectionAnchor> outgoingConnectionAnchors = new ArrayList<ConnectionAnchor>();
        
        public WorkItemFigure() {
        	defaultConnectionAnchor = new ChopboxAnchor(this);
//        	FixedConnectionAnchor c = new FixedConnectionAnchor(this);
//        	outgoingConnectionAnchors.add(c);
//        	c = new FixedConnectionAnchor(this);
//        	outgoingConnectionAnchors.add(c);
        }
        
        public void layoutConnectionAnchors() {
//        	FixedConnectionAnchor c = (FixedConnectionAnchor) outgoingConnectionAnchors.get(0);
//        	c.setOffsetV(getBounds().height);
//        	c.setOffsetH(0);
//        	c = (FixedConnectionAnchor) outgoingConnectionAnchors.get(1);
//        	c.setOffsetV(getBounds().height);
//        	c.setOffsetH(getBounds().width);
        }
        
        public ConnectionAnchor getOutgoingConnectionAnchorAt(Point p) {
        	ConnectionAnchor closest = null;
        	long min = Long.MAX_VALUE;
        	for (ConnectionAnchor c: outgoingConnectionAnchors) {
        		Point p2 = c.getLocation(null);
        		long d = p.getDistance2(p2);
        		if (d < min) {
        			min = d;
        			closest = c;
        		}
        	}
        	if (min > 100) {
        		return defaultConnectionAnchor;
        	}
        	return closest;
        }
        
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
        
        public void validate() {
        	if(isValid()) return;
        	layoutConnectionAnchors();
        	super.validate();
        }
    }
    
    private class SetWorkCommand extends Command {

		protected Work propertyValue;
		protected Work undoValue;
		protected IPropertySource target;

		public SetWorkCommand() {
			super("Set Work Value");
		}

		public boolean canExecute() {
			return true;
		}

		public void execute() {
			undoValue = getWorkItemWrapper().getWorkItemNode().getWork();
			getWorkItemWrapper().getWorkItemNode().setWork(propertyValue);
		}

		public void redo() {
			execute();
		}

		public void setPropertyValue(Work val) {
			propertyValue = val;
		}

		public void undo() {
			getWorkItemWrapper().getWorkItemNode().setWork(undoValue);
		}
		
	}
}
