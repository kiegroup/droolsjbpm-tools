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

import org.drools.eclipse.flow.common.editor.core.ElementConnection;
import org.drools.eclipse.flow.common.editor.core.ElementWrapper;
import org.eclipse.gef.commands.Command;

/**
 * A command for deleting a connection.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class DeleteConnectionCommand extends Command {

    private ElementWrapper source;
    private ElementWrapper target;
    private ElementConnection connection;

    public void execute() {
    	connection.disconnect();
    }

    public void setSource(ElementWrapper action) {
        source = action;
    }

    public void setTarget(ElementWrapper action) {
        target = action;
    }

    public void setAntecedentTaskConnection(ElementConnection connection) {
        this.connection = connection;
    }

    public void undo() {
    	connection.connect(source, target);
    }
}
