/*
 * Copyright 2005 Red Hat, Inc. and/or its affiliates.
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

package org.drools.eclipse.flow.common.editor.core.command;

import java.util.ArrayList;
import java.util.List;

import org.drools.eclipse.flow.common.editor.core.ElementConnection;
import org.drools.eclipse.flow.common.editor.core.ElementContainer;
import org.drools.eclipse.flow.common.editor.core.ElementWrapper;
import org.eclipse.gef.commands.Command;

/**
 * A command for deleting an element.
 */
public class DeleteElementCommand extends Command {

    private ElementWrapper child;
    private ElementContainer parent;
    
    private List<ElementWrapper> incomingElementWrappers = new ArrayList<ElementWrapper>();
    private List<ElementWrapper> outgoingElementWrappers = new ArrayList<ElementWrapper>();
    private List<ElementConnection> incomingConnections = new ArrayList<ElementConnection>();
    private List<ElementConnection> outgoingConnections = new ArrayList<ElementConnection>();
    
    
    private void deleteConnections(ElementWrapper element) {
        for (ElementConnection connection : element.getIncomingConnections()) {
            incomingElementWrappers.add(connection.getSource());
            incomingConnections.add(connection);
        }
        for (ElementConnection connection : element.getOutgoingConnections()) {
            outgoingElementWrappers.add(connection.getTarget());
            outgoingConnections.add(connection);
        }
        for (ElementConnection connection : incomingConnections) {
            connection.disconnect();
        }
        for (ElementConnection connection : outgoingConnections) {
            connection.disconnect();
        }
    }

    public void execute() {
        deleteConnections(child);
        parent.removeElement(child);
    }

    private void restoreConnections() {
        int i = 0;
        for (ElementConnection connection : incomingConnections) {
            connection.connect(incomingElementWrappers.get(i), child);
            i++;
        }
        i = 0;
        for (ElementConnection connection : outgoingConnections) {
            connection.connect(child, outgoingElementWrappers.get(i));
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

    public void setParent(ElementContainer parent) {
        this.parent = parent;
    }

    public void undo() {
        parent.addElement(child);
        restoreConnections();
    }

}
