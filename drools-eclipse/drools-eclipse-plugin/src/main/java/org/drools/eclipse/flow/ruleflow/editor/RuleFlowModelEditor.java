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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.drools.compiler.PackageBuilderConfiguration;
import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.WorkItemDefinitions;
import org.drools.eclipse.flow.common.editor.GenericModelEditor;
import org.drools.eclipse.flow.ruleflow.core.RuleFlowProcessWrapper;
import org.drools.eclipse.flow.ruleflow.core.RuleFlowWrapperBuilder;
import org.drools.eclipse.flow.ruleflow.core.StartNodeWrapper;
import org.drools.eclipse.flow.ruleflow.core.WorkItemWrapper;
import org.drools.eclipse.flow.ruleflow.editor.editpart.RuleFlowEditPartFactory;
import org.drools.eclipse.util.ProjectClassLoader;
import org.drools.process.core.WorkDefinition;
import org.drools.process.core.WorkDefinitionExtension;
import org.drools.ruleflow.core.RuleFlowProcess;
import org.drools.xml.XmlProcessReader;
import org.drools.xml.XmlRuleFlowProcessDumper;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.palette.CombinedTemplateCreationEntry;
import org.eclipse.gef.palette.PaletteDrawer;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.gef.requests.SimpleFactory;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.part.FileEditorInput;

/**
 * Graphical editor for a RuleFlow.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class RuleFlowModelEditor extends GenericModelEditor {

    protected EditPartFactory createEditPartFactory() {
        return new RuleFlowEditPartFactory(getJavaProject());
    }

    protected PaletteRoot createPalette() {
        return RuleFlowPaletteFactory.createPalette();
    }

    protected Object createModel() {
        RuleFlowProcessWrapper result = new RuleFlowProcessWrapper();
        StartNodeWrapper start = new StartNodeWrapper();
        start.setConstraint(new Rectangle(100, 100, -1, -1));
        result.addElement(start);
        IFile file = ((IFileEditorInput)getEditorInput()).getFile();
        String name = file.getName();
        result.setName(name.substring(0, name.length() - 3));
        return result;
    }
    
    public RuleFlowProcessWrapper getRuleFlowModel() {
        return (RuleFlowProcessWrapper) getModel();
    }

    protected void setInput(IEditorInput input) {
        super.setInput(input);
        refreshPalette(((IFileEditorInput) input).getFile());
    }
    
    private void refreshPalette(IFile file) {
        IJavaProject javaProject = getJavaProject();
        if (javaProject != null) {
            try {
                ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
                ClassLoader newLoader = ProjectClassLoader.getProjectClassLoader(javaProject);
                try {
                    Thread.currentThread().setContextClassLoader(newLoader);
                    PaletteDrawer drawer = (PaletteDrawer) getPaletteRoot().getChildren().get(2);
                    List entries = new ArrayList();
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
                    drawer.setChildren(entries);
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
            XmlRuleFlowProcessDumper dumper = new XmlRuleFlowProcessDumper();
            String out = dumper.dump(getRuleFlowModel().getRuleFlowProcess(), includeGraphics);
            writer.write(out);
        } catch (Throwable t) {
            DroolsEclipsePlugin.log(t);
        }
        writer.close();
    }
    
    public void doSave(IProgressMonitor monitor) {
        super.doSave(monitor);
        // save process as separate model file as well
        IFile file = ((IFileEditorInput) getEditorInput()).getFile();
        final IFile modelFile = getModelFile(file);

		if (!modelFile.exists()) {
	        WorkspaceModifyOperation op = new WorkspaceModifyOperation() {
	            public void execute(final IProgressMonitor monitor)
	                    throws CoreException {
	                try {
	                    XmlRuleFlowProcessDumper dumper = new XmlRuleFlowProcessDumper();
	                    String out = dumper.dump(getRuleFlowModel().getRuleFlowProcess(), false);
	                    modelFile.create(new ByteArrayInputStream(out.getBytes()), true, monitor);
	                } catch (Exception e) {
	                    e.printStackTrace();
	                }
	            }
	        };
	        
	        try {
	            new ProgressMonitorDialog(getSite().getWorkbenchWindow().getShell())
	                    .run(false, true, op);
	            setInput(new FileEditorInput(file));
	            getCommandStack().markSaveLocation();
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
		} else {
	        try {
	            XmlRuleFlowProcessDumper dumper = new XmlRuleFlowProcessDumper();
                String out = dumper.dump(getRuleFlowModel().getRuleFlowProcess(), false);
	            modelFile.setContents(new ByteArrayInputStream(out.getBytes()), true, false, monitor);
	        } catch (Throwable t) {
	        	DroolsEclipsePlugin.log(t);
	        }
		}
    }

	private IFile getModelFile(IFile file) {
		IProject project = file.getProject();
		IPath path = file.getProjectRelativePath();
		String fileName = file.getName().substring(0, file.getName().length() - 2) + "rfm";
		IPath modelPath = path.removeLastSegments(1).append(fileName);
		IFile modelFile = project.getFile(modelPath);
		return modelFile;
	}

    protected void createModel(InputStream is) {
        try {
            InputStreamReader reader = new InputStreamReader(is);
            PackageBuilderConfiguration configuration = new PackageBuilderConfiguration();
            XmlProcessReader xmlReader = new XmlProcessReader( configuration.getSemanticModules() );
            try {
                RuleFlowProcess process = (RuleFlowProcess) xmlReader.read(reader);
                if (process == null) {
                    setModel(createModel());
                } else {
                    setModel(RuleFlowWrapperBuilder.getProcessWrapper(process, getJavaProject()));
                }
            } catch (Throwable t) {
                DroolsEclipsePlugin.log(t);
                setModel(createModel());
            }
            reader.close();
        } catch (Throwable t) {
            DroolsEclipsePlugin.log(t);
        }
    }
}
