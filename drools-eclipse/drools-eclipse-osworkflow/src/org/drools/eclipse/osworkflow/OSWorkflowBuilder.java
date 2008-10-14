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
