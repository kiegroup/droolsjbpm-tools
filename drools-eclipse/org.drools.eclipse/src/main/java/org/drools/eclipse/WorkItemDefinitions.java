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

package org.drools.eclipse;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.drools.RuleBaseConfiguration;
import org.drools.eclipse.util.ProjectClassLoader;
import org.drools.process.core.ParameterDefinition;
import org.drools.process.core.WorkDefinition;
import org.drools.process.core.datatype.DataType;
import org.drools.process.core.impl.ParameterDefinitionImpl;
import org.drools.process.core.impl.WorkDefinitionExtensionImpl;
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
                Map<String, WorkDefinition> workDefinitions = new HashMap<String, WorkDefinition>();
                Thread.currentThread().setContextClassLoader(newLoader);
                List<Map<String, Object>> workDefinitionsMaps = new RuleBaseConfiguration().getWorkDefinitions();
                for ( Map<String, Object> workDefinitionMap : workDefinitionsMaps ) {
                    if ( workDefinitionMap != null ) {
                        WorkDefinitionExtensionImpl workDefinition = new WorkDefinitionExtensionImpl();
                        workDefinition.setName( (String) workDefinitionMap.get( "name" ) );
                        workDefinition.setDisplayName( (String) workDefinitionMap.get( "displayName" ) );
                        workDefinition.setIcon( (String) workDefinitionMap.get( "icon" ) );
                        String customEditor = (String) workDefinitionMap.get( "eclipse:customEditor" );
                        if (customEditor == null) {
                        	customEditor = (String) workDefinitionMap.get( "customEditor" );
                        }
                        if (customEditor == null) {
                        	customEditor = "org.drools.eclipse.flow.common.editor.editpart.work.SampleCustomEditor";
                        }
                        workDefinition.setCustomEditor( customEditor );
                        Set<ParameterDefinition> parameters = new HashSet<ParameterDefinition>();
                        Map<String, DataType> parameterMap = (Map<String, DataType>) workDefinitionMap.get( "parameters" );
                        if ( parameterMap != null ) {
                            for ( Map.Entry<String, DataType> entry : parameterMap.entrySet() ) {
                                parameters.add( new ParameterDefinitionImpl( entry.getKey(),
                                                                             entry.getValue() ) );
                            }
                        }
                        workDefinition.setParameters( parameters );
                        Set<ParameterDefinition> results = new HashSet<ParameterDefinition>();
                        Map<String, DataType> resultMap = (Map<String, DataType>) workDefinitionMap.get( "results" );
                        if ( resultMap != null ) {
                            for ( Map.Entry<String, DataType> entry : resultMap.entrySet() ) {
                                results.add( new ParameterDefinitionImpl( entry.getKey(),
                                                                          entry.getValue() ) );
                            }
                        }
                        workDefinition.setResults( results );
                        workDefinitions.put( workDefinition.getName(),
                                                  workDefinition );
                    }
                }

                return workDefinitions;
            } finally {
                Thread.currentThread().setContextClassLoader(oldLoader);
            }
        }
        return new HashMap<String, WorkDefinition>();
    }
    
}
