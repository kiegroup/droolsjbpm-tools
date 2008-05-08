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
 * A command for creating an element.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class ElementConnectionCreateCommand extends Command {

	private ElementConnection connection;
    private ElementWrapper source;
    private ElementWrapper target;

    public boolean canExecute() {
        if (source.equals(target)) {
            return false;
        }
        
        // Check for existence of connection already
        List connections = source.getOutgoingConnections();
        for (Iterator it = connections.iterator(); it.hasNext(); ) {
        	ElementConnection conn = (ElementConnection) it.next();
            if (conn.getTarget().equals(target)) {
            	return false;
            }
        }
        return source.acceptsOutgoingConnection(connection, target)
            && target != null && target.acceptsIncomingConnection(connection, source);
    }

    public void execute() {
        connection.connect(source, target);
    }

    public ElementWrapper getSource() {
        return source;
    }

    public ElementWrapper getTarget() {
        return target;
    }

    

    public void redo() {
    	connection.connect(source, target);
    }

    public void setSource(ElementWrapper source) {
    	this.source = source;
    }

    public void setConnection(ElementConnection connection) {
        this.connection = connection;
    }

    public void setTarget(ElementWrapper target) {
    	this.target = target;
    }

    public void undo() {
    	connection.disconnect();
    }

}
