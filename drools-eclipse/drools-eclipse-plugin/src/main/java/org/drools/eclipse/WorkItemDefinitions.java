package org.drools.eclipse;

import java.util.HashMap;
import java.util.Map;

import org.drools.RuleBaseConfiguration;
import org.drools.eclipse.builder.DroolsBuilder;
import org.drools.eclipse.util.ProjectClassLoader;
import org.drools.process.core.WorkDefinition;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public final class WorkItemDefinitions {
    
    private WorkItemDefinitions() {
    }

    public static Map<String, WorkDefinition> getWorkDefinitions(IResource resource) {
    	IProject project = resource.getProject();
    	if (project != null) {
    		try {
		        if (project.getNature("org.eclipse.jdt.core.javanature") != null) {
		            IJavaProject javaProject = JavaCore.create(project);
		            if (javaProject != null && javaProject.exists()) {
		            	return getWorkDefinitions(javaProject);
		            }
		        }
    		} catch (CoreException e) {
    			DroolsEclipsePlugin.log(e);
    		}
    	}
    	return null;
    }
    
    public static Map<String, WorkDefinition> getWorkDefinitions(IJavaProject project) {
        if (project != null) {
            ClassLoader oldLoader = Thread.currentThread().getContextClassLoader();
            ClassLoader newLoader = ProjectClassLoader.getProjectClassLoader(project);
            try {
                Thread.currentThread().setContextClassLoader(newLoader);
                return new RuleBaseConfiguration().getProcessWorkDefinitions();
            } finally {
                Thread.currentThread().setContextClassLoader(oldLoader);
            }
        }
        return new HashMap<String, WorkDefinition>();
    }
    
    public static void main(String[] args) {
        for (WorkDefinition def: new RuleBaseConfiguration().getProcessWorkDefinitions().values()) {
            System.out.println(def);
        }
    }
    
}
