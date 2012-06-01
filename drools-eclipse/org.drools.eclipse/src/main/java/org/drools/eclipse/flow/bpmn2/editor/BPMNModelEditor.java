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

package org.drools.eclipse.flow.bpmn2.editor;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.drools.definition.process.Node;
import org.drools.definition.process.Process;
import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.WorkItemDefinitions;
import org.drools.eclipse.flow.common.editor.GenericModelEditor;
import org.drools.eclipse.flow.ruleflow.core.RuleFlowProcessWrapper;
import org.drools.eclipse.flow.ruleflow.core.RuleFlowWrapperBuilder;
import org.drools.eclipse.flow.ruleflow.core.StartNodeWrapper;
import org.drools.eclipse.flow.ruleflow.core.WorkItemWrapper;
import org.drools.eclipse.flow.ruleflow.editor.RuleFlowPaletteFactory;
import org.drools.eclipse.flow.ruleflow.editor.editpart.RuleFlowEditPartFactory;
import org.drools.eclipse.util.ProjectClassLoader;
import org.drools.process.core.WorkDefinition;
import org.drools.process.core.WorkDefinitionExtension;
import org.drools.xml.SemanticModules;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.requests.SimpleFactory;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.jbpm.bpmn2.xml.BPMNDISemanticModule;
import org.jbpm.bpmn2.xml.BPMNExtensionsSemanticModule;
import org.jbpm.bpmn2.xml.BPMNSemanticModule;
import org.jbpm.bpmn2.xml.XmlBPMNProcessDumper;
import org.jbpm.compiler.xml.XmlProcessReader;
import org.jbpm.ruleflow.core.RuleFlowProcess;
import org.jbpm.workflow.core.node.CompositeNode;
import org.jbpm.workflow.core.node.EndNode;
import org.jbpm.workflow.core.node.EventNode;
import org.jbpm.workflow.core.node.FaultNode;
import org.jbpm.workflow.core.node.Join;
import org.jbpm.workflow.core.node.Split;
import org.jbpm.workflow.core.node.StartNode;
import org.jbpm.workflow.core.node.TimerNode;

/**
 * Graphical editor for a RuleFlow.
 */
public class BPMNModelEditor extends GenericModelEditor {

    protected EditPartFactory createEditPartFactory() {
        RuleFlowEditPartFactory factory = new RuleFlowEditPartFactory();
        factory.setProject(getJavaProject());
        return factory;
    }

    protected PaletteRoot createPalette() {
        return RuleFlowPaletteFactory.createPalette();
    }

    protected Object createModel() {
        RuleFlowProcessWrapper result = new RuleFlowProcessWrapper();
        StartNodeWrapper start = new StartNodeWrapper();
        start.setConstraint(new Rectangle(100, 100, -1, -1));
        result.addElement(start);
        start.setParent(result);
        IFile file = ((IFileEditorInput)getEditorInput()).getFile();
        String name = file.getName();
        result.setName(name.substring(0, name.length() - 5));
        result.setId("com.sample.process");
        return result;
    }
    
    public RuleFlowProcessWrapper getRuleFlowModel() {
        return (RuleFlowProcessWrapper) getModel();
    }

    protected void setInput(IEditorInput input) {
        super.setInput(input);
        if (input instanceof IFileEditorInput) {
            refreshPalette(((IFileEditorInput) input).getFile());
        }
    }
    
    private void refreshPalette(IFile file) {
        IJavaProject javaProject = getJavaProject();
        if (javaProject != null) {
            try {
                ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
                ClassLoader newLoader = ProjectClassLoader.getProjectClassLoader(javaProject);
                try {
                    Thread.currentThread().setContextClassLoader(newLoader);
                    if (getPaletteRoot().getChildren().size() > 2) {
	                    PaletteDrawer drawer = (PaletteDrawer) getPaletteRoot().getChildren().get(2);
	                    List entries = new ArrayList();
	                    try {
	                        for (final WorkDefinition workDefinition: WorkItemDefinitions.getWorkDefinitions(file).values()) {
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
	
	                            URL iconUrl = null;
	                            if (icon != null) {
	                                iconUrl = newLoader.getResource(icon);
	                            }
	                            if (iconUrl == null) {
	                                iconUrl = DroolsEclipsePlugin.getDefault().getBundle().getEntry("icons/action.gif");
	                            }
	                            CombinedTemplateCreationEntry combined = new CombinedTemplateCreationEntry(
	                                label,
	                                description,
	                                WorkItemWrapper.class,
	                                new SimpleFactory(WorkItemWrapper.class) {
	                                    public Object getNewObject() {
	                                        WorkItemWrapper workItemWrapper = (WorkItemWrapper) super.getNewObject();
	                                        workItemWrapper.setName(label);
	                                        workItemWrapper.setWorkDefinition(workDefinition);
	                                        return workItemWrapper;
	                                    }
	                                },
	                                ImageDescriptor.createFromURL(iconUrl),
	                                ImageDescriptor.createFromURL(iconUrl)
	                            );
	                            entries.add(combined);
	                        }
	                    } catch (Throwable t) {
	                        DroolsEclipsePlugin.log(t);
	                        MessageDialog.openError(
	                            getSite().getShell(), "Parsing work item definitions", t.getMessage());
	                    }
	                    drawer.setChildren(entries);
                    }
                } finally {
                    Thread.currentThread().setContextClassLoader(oldLoader);
                }
            } catch (Exception e) {
                DroolsEclipsePlugin.log(e);
            }
        }
    }

    protected void writeModel(OutputStream os) throws IOException {
        writeModel(os, true);
    }
    
    protected void writeModel(OutputStream os, boolean includeGraphics) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(os);
        try {
            RuleFlowProcess process = getRuleFlowModel().getRuleFlowProcess();
            XmlBPMNProcessDumper dumper = XmlBPMNProcessDumper.INSTANCE;
            String out = dumper.dump(process, XmlBPMNProcessDumper.META_DATA_USING_DI);
            writer.write(out);
        } catch (Throwable t) {
            DroolsEclipsePlugin.log(t);
            IStatus status = new Status(
                IStatus.ERROR,
                DroolsEclipsePlugin.getUniqueIdentifier(),
                -1,
                "Could not save BPMN process, see error log for more details and contact the developers: " + t.getMessage(),
                t);
            ErrorDialog.openError( getSite().getShell(),
                "Process Save Error",
                "Unable to save process.",
                status);
            throw new IOException(t.getMessage());
        }
        writer.close();
    }
    
    
    protected void createModel(InputStream is) {
        try
        {
            InputStreamReader isr = new InputStreamReader(is);
            SemanticModules semanticModules = new SemanticModules();
            semanticModules.addSemanticModule(new BPMNSemanticModule());
    	    semanticModules.addSemanticModule(new BPMNExtensionsSemanticModule());
            semanticModules.addSemanticModule(new BPMNDISemanticModule());
            XmlProcessReader xmlReader = new XmlProcessReader(semanticModules, Thread.currentThread().getContextClassLoader());

            try
            {
                List<Process> processes = xmlReader.read(isr);
                if (processes == null || processes.size() == 0) {
                    setModel(createModel());
                } else {
                    RuleFlowProcess process = (RuleFlowProcess) processes.get(0);
                    if (process == null) {
                        setModel(createModel());
                    } else {
                        correctEventNodeSize(process.getNodes());
                        setModel(new RuleFlowWrapperBuilder().getProcessWrapper(process, getJavaProject()));
                    }
                }
            } catch (Throwable t) {
                DroolsEclipsePlugin.log(t);
                MessageDialog.openError( getSite().getShell(),
                    "Could not read RuleFlow file",
                    "An exception occurred while reading in the RuleFlow XML: "
                        + t.getMessage() + " See the error log for more details.");
                setModel(createModel());
            }

            if (isr != null){
                isr.close();
            }
        } catch (Throwable t) {
            DroolsEclipsePlugin.log(t);
        }
    }
    
    private void correctEventNodeSize(Node[] nodes) {
        for (Node node: nodes) {
            if (node instanceof StartNode
                    || node instanceof EndNode
                    || node instanceof EventNode
                    || node instanceof FaultNode
                    || node instanceof TimerNode) {
                Integer width = (Integer) node.getMetaData().get("width");
                if (width == null) {
                    width = 48;
                }
                Integer height = (Integer) node.getMetaData().get("height");
                if (height == null) {
                    height = 48;
                }
                if (width != 48 || height != 48) {
                    node.getMetaData().put("width", 48);
                    node.getMetaData().put("height", 48);
                    Integer x = (Integer) node.getMetaData().get("x");
                    Integer y = (Integer) node.getMetaData().get("y");
                    x = x - ((48 - width)/2);
                    y = y - ((48 - height)/2);
                    node.getMetaData().put("x", x);
                    node.getMetaData().put("y", y);
                }
            } else if (node instanceof Split
                    || node instanceof Join) {
                Integer width = (Integer) node.getMetaData().get("width");
                if (width == null) {
                    width = 49;
                }
                Integer height = (Integer) node.getMetaData().get("height");
                if (height == null) {
                    height = 49;
                }
                if (width != 49 || height != 49) {
                    node.getMetaData().put("width", 49);
                    node.getMetaData().put("height", 49);
                    Integer x = (Integer) node.getMetaData().get("x");
                    Integer y = (Integer) node.getMetaData().get("y");
                    x = x - ((49 - width)/2);
                    y = y - ((49 - height)/2);
                    node.getMetaData().put("x", x);
                    node.getMetaData().put("y", y);
                }
            } else if (node instanceof CompositeNode) {
                correctEventNodeSize(((CompositeNode) node).getNodes());
            }
        }
    }
    
}
