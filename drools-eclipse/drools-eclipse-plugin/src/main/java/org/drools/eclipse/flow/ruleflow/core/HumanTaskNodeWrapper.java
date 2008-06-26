package org.drools.eclipse.flow.ruleflow.core;
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

import java.util.HashSet;
import java.util.Set;

import org.drools.process.core.ParameterDefinition;
import org.drools.process.core.WorkDefinition;
import org.drools.process.core.datatype.impl.type.StringDataType;
import org.drools.process.core.impl.ParameterDefinitionImpl;
import org.drools.process.core.impl.WorkDefinitionExtensionImpl;
import org.drools.process.core.impl.WorkDefinitionImpl;
import org.drools.workflow.core.node.HumanTaskNode;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;

/**
 * Wrapper for a human task node.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class HumanTaskNodeWrapper extends WorkItemWrapper {

    public static final String SWIMLANE = "Swimlane";

	private static final long serialVersionUID = 4L;

    private static final WorkDefinition WORK_DEFINITION;
    
    static {
        WORK_DEFINITION = new WorkDefinitionExtensionImpl();
        ((WorkDefinitionImpl) WORK_DEFINITION).setName("Human Task");
        Set<ParameterDefinition> parameterDefinitions = new HashSet<ParameterDefinition>();
        parameterDefinitions.add(new ParameterDefinitionImpl("TaskName", new StringDataType()));
        parameterDefinitions.add(new ParameterDefinitionImpl("ActorId", new StringDataType()));
        parameterDefinitions.add(new ParameterDefinitionImpl("Priority", new StringDataType()));
        parameterDefinitions.add(new ParameterDefinitionImpl("Comment", new StringDataType()));
        ((WorkDefinitionExtensionImpl) WORK_DEFINITION).setParameters(parameterDefinitions);
        ((WorkDefinitionExtensionImpl) WORK_DEFINITION).setIcon("icons/human_task.gif");
    }

    public HumanTaskNodeWrapper() {
        setNode(new HumanTaskNode());
        getNode().setName("Human Task");
        setWorkDefinition(WORK_DEFINITION);
    }
    
    protected IPropertyDescriptor[] createPropertyDescriptors() {
        IPropertyDescriptor[] parentDescriptors = super.createPropertyDescriptors();
        IPropertyDescriptor[] descriptors = new IPropertyDescriptor[parentDescriptors.length + 1];
        System.arraycopy(parentDescriptors, 0, descriptors, 0, parentDescriptors.length);
        descriptors[descriptors.length - 1] = 
            new TextPropertyDescriptor(SWIMLANE, "Swimlane");
        return descriptors;
    }
    
    public HumanTaskNode getHumanTaskNode() {
        return (HumanTaskNode) getNode();
    }
    
    public Object getPropertyValue(Object id) {
        if (SWIMLANE.equals(id)) {
        	String swimlane = getHumanTaskNode().getSwimlane();
            return swimlane == null ? "" : swimlane;
        }
        return super.getPropertyValue(id);
    }

    public void resetPropertyValue(Object id) {
        if (SWIMLANE.equals(id)) {
        	getHumanTaskNode().setSwimlane(null);
        } else {
            super.resetPropertyValue(id);
        }
    }

    public void setPropertyValue(Object id, Object value) {
        if (SWIMLANE.equals(id)) {
        	getHumanTaskNode().setSwimlane((String) value);
        } else {
            super.setPropertyValue(id, value);
        }
    }
}
