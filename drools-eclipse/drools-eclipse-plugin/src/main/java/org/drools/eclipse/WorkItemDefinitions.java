package org.drools.eclipse;

import java.util.HashMap;
import java.util.Map;

import org.drools.RuleBaseConfiguration;
import org.drools.eclipse.builder.DroolsBuilder;
import org.drools.eclipse.util.ProjectClassLoader;
import org.drools.process.core.WorkDefinition;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public final class WorkItemDefinitions {
    
    private WorkItemDefinitions() {
    }

    public static Map<String, WorkDefinition> getWorkDefinitions(IResource resource) {
        return getWorkDefinitions(resource == null ? null : resource.getProject());
    }
    
    public static Map<String, WorkDefinition> getWorkDefinitions(IProject project) {
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
                    return new RuleBaseConfiguration().getProcessWorkDefinitions();
                } finally {
                    Thread.currentThread().setContextClassLoader(oldLoader);
                }
            } catch (Exception e) {
                DroolsEclipsePlugin.log(e);
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
