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

import org.drools.eclipse.flow.common.editor.core.ElementWrapper;
import org.drools.eclipse.flow.common.editor.core.ProcessWrapper;
import org.drools.process.core.Process;
import org.drools.ruleflow.core.RuleFlowProcess;
import org.drools.workflow.core.WorkflowProcess;

/**
 * Wrapper for a RuleFlow process.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class RuleFlowProcessWrapper extends ProcessWrapper {

	private static final long serialVersionUID = 400L;

//	private static IPropertyDescriptor[] descriptors;
//
//    public static final String VARIABLES = "variables";
//    static {
//        descriptors = new IPropertyDescriptor[ProcessWrapper.descriptors.length + 1];
//        System.arraycopy(ProcessWrapper.descriptors, 0, descriptors, 0, ProcessWrapper.descriptors.length);
//        descriptors[descriptors.length - 1] = 
//            new ListPropertyDescriptor(VARIABLES, "Variables", VariableListCellEditor.class);
//   }
    
    public WorkflowProcess getRuleFlowProcess() {
        return (WorkflowProcess) getProcess();
    }
    
    protected Process createProcess() {
        return new RuleFlowProcess();
    }

    protected void internalAddElement(ElementWrapper element) {
        getRuleFlowProcess().addNode(((NodeWrapper) element).getNode()); 
    }

    protected void internalRemoveElement(ElementWrapper element) {
        getRuleFlowProcess().removeNode(((NodeWrapper) element).getNode()); 
    }
    
//    public IPropertyDescriptor[] getPropertyDescriptors() {
//        return descriptors;
//    }
//
//    public Object getPropertyValue(Object id) {
//        if (VARIABLES.equals(id)) {
//            return getRuleFlowProcess().getVariables();
//        }
//        return super.getPropertyValue(id);
//    }
//
//    public void resetPropertyValue(Object id) {
//        if (VARIABLES.equals(id)) {
//            getRuleFlowProcess().setVariables(new ArrayList());
//        } else {
//            super.resetPropertyValue(id);
//        }
//    }
//
//    public void setPropertyValue(Object id, Object value) {
//        if (VARIABLES.equals(id)) {
//            getRuleFlowProcess().setVariables((List) value);
//        } else {
//            super.setPropertyValue(id, value);
//        }
//    }
}
