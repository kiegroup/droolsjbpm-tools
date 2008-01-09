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

import java.util.Iterator;
import java.util.List;

import org.drools.eclipse.flow.common.editor.core.ElementConnection;
import org.drools.eclipse.flow.common.editor.core.ElementWrapper;
import org.eclipse.gef.commands.Command;

/**
 * A command for reconnecting the target of a connection.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class ReconnectElementConnectionTargetCommand extends Command {

    private ElementConnection connection;
    private ElementWrapper oldTarget;
    private ElementWrapper newTarget;
    private ElementWrapper source;
    
    
    public boolean canExecute() {
        if (connection.getSource().equals(newTarget))
            return false;
            
        List connections = newTarget.getIncomingConnections();
        for (Iterator it = connections.iterator(); it.hasNext(); ) {
        	ElementConnection connection = (ElementConnection) it.next();
            if (connection.getSource().equals(source) && !connection.getTarget().equals(oldTarget))
                return false;
        }   
        return newTarget.acceptsIncomingConnection(connection);    
    }

    public void execute() {
        if (newTarget != null) {
        	connection.disconnect();
        	connection.connect(source, newTarget);    	
        }
    }

    public void setTarget(ElementWrapper target) {
        this.newTarget = target;
    }

    public void setConnection(ElementConnection connection) {
        this.connection = connection;
        this.source = connection.getSource();
        this.oldTarget = connection.getTarget();
    }

    public void undo() {
    	connection.disconnect();
    	connection.connect(source, oldTarget);
    }
    
    public void redo() {
    	connection.disconnect();
    	connection.connect(source, newTarget);
    }
}
