package org.drools.eclipse.osworkflow.core;
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
import org.drools.eclipse.flow.ruleflow.core.NodeWrapper;
import org.drools.osworkflow.core.OSWorkflowProcess;
import org.drools.process.core.Process;
import org.drools.workflow.core.Node;

/**
 * Wrapper for a OSWorkflow process.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class OSWorkflowProcessWrapper extends ProcessWrapper {

	private static final long serialVersionUID = 400L;

    public OSWorkflowProcess getOSWorkflowProcess() {
        return (OSWorkflowProcess) getProcess();
    }
    
    protected Process createProcess() {
        return new OSWorkflowProcess();
    }

    protected void internalAddElement(ElementWrapper element) {
        Node node = ((NodeWrapper) element).getNode();
        long id = 0;
        for (Node n: getOSWorkflowProcess().getNodes()) {
            if (n.getId() > id) {
                id = n.getId();
            }
        }
        node.setId(++id);
        getOSWorkflowProcess().addNode(node); 
    }

    protected void internalRemoveElement(ElementWrapper element) {
        getOSWorkflowProcess().removeNode(((NodeWrapper) element).getNode()); 
    }
    
}
