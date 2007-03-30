package org.drools.eclipse.flow.common.editor.core.command;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.drools.eclipse.flow.common.editor.core.ElementConnection;
import org.drools.eclipse.flow.common.editor.core.ElementWrapper;
import org.drools.eclipse.flow.common.editor.core.ProcessWrapper;
import org.eclipse.gef.commands.Command;

/**
 * A command for deleting an element.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class DeleteElementCommand extends Command {

    private ElementWrapper child;
    private ProcessWrapper parent;
    
    private List incomingElementWrappers = new ArrayList();
    private List outgoingElementWrappers = new ArrayList();
    private List incomingConnections = new ArrayList();
    private List outgoingConnections = new ArrayList();
    
    
    private void deleteConnections(ElementWrapper element) {
    	for (Iterator it = element.getIncomingConnections().iterator(); it.hasNext(); ) {
    		ElementConnection connection = (ElementConnection) it.next();
    		incomingElementWrappers.add(connection.getSource());
    		incomingConnections.add(connection);
    	}
    	for (Iterator it = element.getOutgoingConnections().iterator(); it.hasNext(); ) {
    		ElementConnection connection = (ElementConnection) it.next();
    		outgoingElementWrappers.add(connection.getTarget());
    		outgoingConnections.add(connection);
    	} 
    	for (Iterator it = incomingConnections.iterator(); it.hasNext(); ) {
    		ElementConnection connection = (ElementConnection) it.next();
    		connection.disconnect();
    	}
    	for (Iterator it = outgoingConnections.iterator(); it.hasNext(); ) {
    		ElementConnection connection = (ElementConnection) it.next();
    		connection.disconnect();
    	}
    }

    public void execute() {
        deleteConnections(child);
        parent.removeElement(child);
    }

    private void restoreConnections() {
    	int i = 0;
    	for (Iterator it = incomingConnections.iterator(); it.hasNext(); ) {
    		ElementConnection connection = (ElementConnection) it.next();
    		connection.connect((ElementWrapper) incomingElementWrappers.get(i), child);
    		i++;
    	}
    	i = 0;
    	for (Iterator it = outgoingConnections.iterator(); it.hasNext(); ) {
    		ElementConnection connection = (ElementConnection) it.next();
    		connection.connect(child, (ElementWrapper) outgoingElementWrappers.get(i));
    		i++;
    	}
    	incomingConnections.clear();
    	incomingElementWrappers.clear();
    	outgoingConnections.clear();
    	outgoingElementWrappers.clear();
    }
    
    public void setChild(ElementWrapper child) {
        this.child = child;
    }

    public void setParent(ProcessWrapper parent) {
        this.parent = parent;
    }

    public void undo() {
        parent.addElement(child);
        restoreConnections();
    }

}
