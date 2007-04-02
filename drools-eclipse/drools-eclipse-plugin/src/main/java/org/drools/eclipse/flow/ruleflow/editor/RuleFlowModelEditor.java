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
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.flow.common.editor.GenericModelEditor;
import org.drools.eclipse.flow.ruleflow.core.RuleFlowProcessWrapper;
import org.drools.eclipse.flow.ruleflow.core.StartNodeWrapper;
import org.drools.eclipse.flow.ruleflow.editor.editpart.RuleFlowEditPartFactory;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPartFactory;
import org.eclipse.gef.palette.PaletteRoot;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.part.FileEditorInput;

import com.thoughtworks.xstream.XStream;

/**
 * Graphical editor for a RuleFlow.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class RuleFlowModelEditor extends GenericModelEditor {

    protected EditPartFactory createEditPartFactory() {
        return new RuleFlowEditPartFactory();
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

    protected void createOutputStream(OutputStream os) throws IOException {
    	createOutputStream(os, true);
    }

    
    protected void createOutputStream(OutputStream os, boolean includeGraphics) throws IOException {
        OutputStreamWriter writer = new OutputStreamWriter(os);
        try {
            XStream stream = new XStream();
            stream.setMode(XStream.ID_REFERENCES);
            if (includeGraphics) {
            	stream.toXML(getRuleFlowModel(), writer);
            } else {
            	stream.toXML(getRuleFlowModel().getProcess(), writer);
            }
            writer.close();
        } catch (Throwable t) {
            DroolsEclipsePlugin.log(t);
        }
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
	                    ByteArrayOutputStream out = new ByteArrayOutputStream();
	                    createOutputStream(out, false);
	                    modelFile.create(new ByteArrayInputStream(out.toByteArray()),
	                            true, monitor);
	                    out.close();
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
	        ByteArrayOutputStream out = new ByteArrayOutputStream();
	        try {
	        	createOutputStream(out, false);
	            modelFile.setContents(
	        		new ByteArrayInputStream(out.toByteArray()),
	        		true, false, monitor);
	            out.close();
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

    protected void createInputStream(InputStream is) throws Exception {
        InputStreamReader reader = new InputStreamReader(is);
        XStream stream = new XStream();
        stream.setMode(XStream.ID_REFERENCES);
        
        ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader newLoader = this.getClass().getClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(newLoader);
            setModel(stream.fromXML(reader));
        } finally {
            Thread.currentThread().setContextClassLoader(oldLoader);
        }
        reader.close();
    }
}
