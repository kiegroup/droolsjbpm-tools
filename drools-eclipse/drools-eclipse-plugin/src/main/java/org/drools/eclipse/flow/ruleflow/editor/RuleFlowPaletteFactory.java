package org.drools.eclipse.flow.ruleflow.editor;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.WorkItemDefinitions;
import org.drools.eclipse.flow.common.editor.core.ElementConnectionFactory;
import org.drools.eclipse.flow.ruleflow.core.ActionWrapper;
import org.drools.eclipse.flow.ruleflow.core.ConnectionWrapper;
import org.drools.eclipse.flow.ruleflow.core.ConnectionWrapperFactory;
import org.drools.eclipse.flow.ruleflow.core.EndNodeWrapper;
import org.drools.eclipse.flow.ruleflow.core.JoinWrapper;
import org.drools.eclipse.flow.ruleflow.core.MilestoneWrapper;
import org.drools.eclipse.flow.ruleflow.core.RuleSetNodeWrapper;
import org.drools.eclipse.flow.ruleflow.core.SplitWrapper;
import org.drools.eclipse.flow.ruleflow.core.StartNodeWrapper;
import org.drools.eclipse.flow.ruleflow.core.SubFlowWrapper;
import org.drools.eclipse.flow.ruleflow.core.WorkItemWrapper;
import org.drools.process.core.WorkDefinition;
import org.drools.process.core.WorkDefinitionExtension;
import org.drools.process.core.impl.WorkImpl;
import org.drools.workflow.core.Connection;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.ConnectionCreationToolEntry;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;
import org.eclipse.gef.requests.CreationFactory;
import org.eclipse.gef.requests.SimpleFactory;
import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Factory for creating a RuleFlow palette.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class RuleFlowPaletteFactory {

    public static PaletteRoot createPalette() {
        PaletteRoot flowPalette = new PaletteRoot();
        flowPalette.addAll(createCategories(flowPalette));
        return flowPalette;
    }
    
    private static List createCategories(PaletteRoot root) {
        List categories = new ArrayList();
        categories.add(createControlGroup(root));
        categories.add(createComponentsDrawer());
        categories.add(createTaskNodesDrawer());
        return categories;
    }

    private static PaletteContainer createComponentsDrawer() {

        PaletteDrawer drawer = new PaletteDrawer("Components", null);

        List entries = new ArrayList();

        CombinedTemplateCreationEntry combined = new CombinedTemplateCreationEntry(
            "Start",
            "Create a new Start",
            StartNodeWrapper.class,
            new SimpleFactory(StartNodeWrapper.class),
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/process_start.gif")),
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/process_start.gif"))
        );
        entries.add(combined);
        
        combined = new CombinedTemplateCreationEntry(
            "End",
            "Create a new End",
            EndNodeWrapper.class,
            new SimpleFactory(EndNodeWrapper.class),
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/process_stop.gif")), 
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/process_stop.gif"))
        );
        entries.add(combined);
                
        combined = new CombinedTemplateCreationEntry(
            "RuleFlowGroup",
            "Create a new RuleFlowGroup",
            RuleSetNodeWrapper.class,
            new SimpleFactory(RuleSetNodeWrapper.class),
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/activity.gif")), 
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/activity.gif"))
        );
        entries.add(combined);
            
        combined = new CombinedTemplateCreationEntry(
            "Split",
            "Create a new Split",
            SplitWrapper.class,
            new SimpleFactory(SplitWrapper.class),
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/split.gif")), 
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/split.gif"))
        );
        entries.add(combined);
                    
        combined = new CombinedTemplateCreationEntry(
            "Join",
            "Create a new Join",
            JoinWrapper.class,
            new SimpleFactory(JoinWrapper.class),
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/join.gif")), 
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/join.gif"))
        );
        entries.add(combined);
                        
        combined = new CombinedTemplateCreationEntry(
            "Milestone",
            "Create a new Milestone",
            MilestoneWrapper.class,
            new SimpleFactory(MilestoneWrapper.class),
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/question.gif")), 
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/question.gif"))
        );
        entries.add(combined);
                            
        combined = new CombinedTemplateCreationEntry(
            "SubFlow",
            "Create a new SubFlow",
            SubFlowWrapper.class,
            new SimpleFactory(SubFlowWrapper.class),
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/process.gif")), 
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/process.gif"))
        );
        entries.add(combined);
                                
        combined = new CombinedTemplateCreationEntry(
            "Action",
            "Create a new Action",
            ActionWrapper.class,
            new SimpleFactory(ActionWrapper.class),
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/action.gif")), 
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/action.gif"))
        );
        entries.add(combined);
                      
        drawer.addAll(entries);
        return drawer;
    }
    
    private static PaletteContainer createTaskNodesDrawer() {

        PaletteDrawer drawer = new PaletteDrawer("Work Items", null);

        List entries = new ArrayList();

        for (Iterator iterator = WorkItemDefinitions.getWorkDefinitions().iterator(); iterator.hasNext(); ) {
            final WorkDefinition workDefinition = (WorkDefinition) iterator.next();
            final String label;
            String description = workDefinition.getName();
            String icon = null;
            if (workDefinition instanceof WorkDefinitionExtension) {
                WorkDefinitionExtension extension = (WorkDefinitionExtension) workDefinition;
                label = extension.getDisplayName();
                description = extension.getExplanationText();
                icon = extension.getIcon();
            } else {
                label = workDefinition.getName();
            }
            
            CombinedTemplateCreationEntry combined = new CombinedTemplateCreationEntry(
                label,
                description,
                WorkItemWrapper.class,
                new SimpleFactory(WorkItemWrapper.class) {
                    public Object getNewObject() {
                        WorkItemWrapper taskWrapper = (WorkItemWrapper) super.getNewObject();
                        taskWrapper.setName(label);
                        taskWrapper.getWorkItemNode().setName(label);
                        taskWrapper.getWorkItemNode().setWork(new WorkImpl());
                        taskWrapper.getWorkItemNode().getWork().setName(workDefinition.getName());
                        return taskWrapper;
                    }
                },
                ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry(icon == null? "icons/action.gif" : icon)), 
                ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry(icon == null? "icons/action.gif" : icon))
            );
            entries.add(combined);
        }
                                        
        drawer.addAll(entries);
        return drawer;
    }
    
    private static PaletteContainer createControlGroup(PaletteRoot root) {
        PaletteGroup controlGroup = new PaletteGroup("Control Group");

        List entries = new ArrayList();

        ToolEntry tool = new SelectionToolEntry();
        entries.add(tool);
        root.setDefaultEntry(tool);

        tool = new MarqueeToolEntry();
        entries.add(tool);
        
        final ElementConnectionFactory normalConnectionFactory = new ConnectionWrapperFactory();

        tool = new ConnectionCreationToolEntry(
            "Connection Creation",
            "Creating connections",
            new CreationFactory() {
                public Object getNewObject() {
                	return normalConnectionFactory.createElementConnection();
                }
                public Object getObjectType() {
                	return ConnectionWrapper.class;
                }
            },
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/connection.gif")), 
            ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/connection.gif"))
        );
        entries.add(tool);
        
        controlGroup.addAll(entries);
        return controlGroup;
    }
}
