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

import java.util.List;

import org.drools.eclipse.flow.common.editor.core.ElementConnection;
import org.drools.eclipse.flow.common.editor.core.ElementWrapper;
import org.eclipse.gef.commands.Command;

/**
 * A command for reconnecting the source of a connection.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class ReconnectElementConnectionSourceCommand extends Command {

	private ElementConnection connection;
	private ElementWrapper target;
	private ElementWrapper oldSource;
	private ElementWrapper newSource;
    
    public boolean canExecute() {
        if (connection.getTarget().equals(newSource))
            return false;
            
        List connections = newSource.getOutgoingConnections(); 
        for (int i = 0; i < connections.size(); i++) {
            ElementConnection connection = ((ElementConnection)(connections.get(i)));
            if (connection.getTarget().equals(target) && !connection.getSource().equals(oldSource))
                return false;
        }
        return newSource.acceptsOutgoingConnection(connection, target); //XXX    
    }

    public void execute() {
        if (newSource != null) {
        	connection.disconnect();
        	connection.connect(newSource, target); 
        }
    }

    public void setSource(ElementWrapper source) {
    	this.newSource = source;
    }

    public void setConnection(ElementConnection connection) {
        this.connection = connection;
        this.target = connection.getTarget();
        this.oldSource = connection.getSource();
    }

    public void undo() {
    	connection.disconnect();
    	connection.connect(oldSource, target);    	
    }
    
    public void redo() {
    	connection.disconnect();
    	connection.connect(newSource, target);
    }
}
