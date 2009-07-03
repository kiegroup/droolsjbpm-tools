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
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.flow.ruleflow.skin.SkinManager;
import org.drools.eclipse.flow.ruleflow.skin.SkinProvider;
import org.drools.eclipse.preferences.IDroolsConstants;
import org.eclipse.gef.palette.MarqueeToolEntry;
import org.eclipse.gef.palette.PaletteContainer;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteEntry;
import org.eclipse.gef.palette.PaletteGroup;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.palette.SelectionToolEntry;
import org.eclipse.gef.palette.ToolEntry;

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
    
    private static List<PaletteEntry> createCategories(PaletteRoot root) {
        List<PaletteEntry> categories = new ArrayList<PaletteEntry>();
        categories.add(createControlGroup(root));
        String skin = DroolsEclipsePlugin.getDefault().getPreferenceStore().getString(IDroolsConstants.SKIN);
        SkinProvider skinProvider = SkinManager.getInstance().getSkinProvider(skin);
        categories.add(skinProvider.createComponentsDrawer());
        String flowNodes = DroolsEclipsePlugin.getDefault().getPluginPreferences().getString(IDroolsConstants.FLOW_NODES);
        if (flowNodes.charAt(12) == '1') {
            categories.add(createWorkNodesDrawer(skinProvider.getWorkItemsName()));
        }
        return categories;
    }
    
    private static PaletteContainer createWorkNodesDrawer(String name) {

        PaletteDrawer drawer = new PaletteDrawer(name, null);

//        List entries = new ArrayList();
//
//        for (Iterator iterator = WorkItemDefinitions.getWorkDefinitions().iterator(); iterator.hasNext(); ) {
//            final WorkDefinition workDefinition = (WorkDefinition) iterator.next();
//            final String label;
//            String description = workDefinition.getName();
//            String icon = null;
//            if (workDefinition instanceof WorkDefinitionExtension) {
//                WorkDefinitionExtension extension = (WorkDefinitionExtension) workDefinition;
//                label = extension.getDisplayName();
//                description = extension.getExplanationText();
//                icon = extension.getIcon();
//            } else {
//                label = workDefinition.getName();
//            }
//            
//            CombinedTemplateCreationEntry combined = new CombinedTemplateCreationEntry(
//                label,
//                description,
//                WorkItemWrapper.class,
//                new SimpleFactory(WorkItemWrapper.class) {
//                    public Object getNewObject() {
//                        WorkItemWrapper taskWrapper = (WorkItemWrapper) super.getNewObject();
//                        taskWrapper.setName(label);
//                        taskWrapper.getWorkItemNode().setName(label);
//                        taskWrapper.getWorkItemNode().setWork(new WorkImpl());
//                        taskWrapper.getWorkItemNode().getWork().setName(workDefinition.getName());
//                        return taskWrapper;
//                    }
//                },
//                ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry(icon == null? "icons/action.gif" : icon)), 
//                ImageDescriptor.createFromURL(DroolsEclipsePlugin.getDefault().getBundle().getEntry(icon == null? "icons/action.gif" : icon))
//            );
//            entries.add(combined);
//        }
//                                        
//        drawer.addAll(entries);
        return drawer;
    }
    
    private static PaletteContainer createControlGroup(PaletteRoot root) {
        PaletteGroup controlGroup = new PaletteGroup("Control Group");

        List<PaletteEntry> entries = new ArrayList<PaletteEntry>();

        ToolEntry tool = new SelectionToolEntry();
        entries.add(tool);
        root.setDefaultEntry(tool);

        tool = new MarqueeToolEntry();
        entries.add(tool);
        
        String skin = DroolsEclipsePlugin.getDefault().getPreferenceStore().getString(IDroolsConstants.SKIN);
        SkinProvider skinProvider = SkinManager.getInstance().getSkinProvider(skin);
        entries.add(skinProvider.createConnectionEntry());
        
        controlGroup.addAll(entries);
        return controlGroup;
    }
}
