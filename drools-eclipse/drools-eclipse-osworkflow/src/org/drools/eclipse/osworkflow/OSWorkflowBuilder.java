/**
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

package org.drools.eclipse.osworkflow;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.ProcessInfo;
import org.drools.eclipse.builder.DroolsBuildMarker;
import org.drools.eclipse.builder.DroolsBuilder;
import org.drools.osworkflow.xml.OSWorkflowSemanticModule;
import org.drools.process.core.Process;
import org.drools.xml.SemanticModules;
import org.drools.xml.XmlProcessReader;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 *  
 * @author <a href="mailto:kris_verlaenen@hotmail.com">kris verlaenen </a>
 */
public class OSWorkflowBuilder extends DroolsBuilder {

    protected boolean parseResource(IResource res, boolean clean) {
        try {
            IJavaProject project = JavaCore.create(res.getProject());
            // exclude files that are located in the output directory,
            // unless the ouput directory is the same as the project location
            if (!project.getOutputLocation().equals(project.getPath())
                    && project.getOutputLocation().isPrefixOf(res.getFullPath())) {
                return false;
            }
        } catch (JavaModelException e) {
            // do nothing
        }

        if (res instanceof IFile && "oswf".equals(res.getFileExtension())) {
            removeProblemsFor(res);
            List<DroolsBuildMarker> markers = new ArrayList<DroolsBuildMarker>();
            try {
            	if (clean) {
            		DroolsEclipsePlugin.getDefault().invalidateResource(res);
            	}
            	String input = convertToString(((IFile) res).getContents());
            	ProcessInfo processInfo = parseProcess(input, (IFile) res);
            	markParseErrors(markers, processInfo.getErrors());
            } catch (Throwable t) {
            	createMarker(res, t.getMessage(), -1);
            }
            return false;
        }

        return true;
    }
    
    public ProcessInfo parseProcess(String input, IResource resource) {
        try {
            ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
            ClassLoader newLoader = this.getClass().getClassLoader();
            try {
                Thread.currentThread().setContextClassLoader(newLoader);
                SemanticModules semanticModules = new SemanticModules();
                semanticModules.addSemanticModule(new OSWorkflowSemanticModule());
                XmlProcessReader xmlReader = new XmlProcessReader(semanticModules);
                Process process = xmlReader.read(new StringReader(input));
                if (process != null) {
                    return DroolsEclipsePlugin.getDefault().parseProcess(process, resource);
                } else {
                    throw new IllegalArgumentException(
                        "Could not parse process " + resource);
                }
            } finally {
                Thread.currentThread().setContextClassLoader(oldLoader);
            }           
        } catch (Exception e) {
            DroolsEclipsePlugin.log(e);
        }
        return null;
    }

    
}
