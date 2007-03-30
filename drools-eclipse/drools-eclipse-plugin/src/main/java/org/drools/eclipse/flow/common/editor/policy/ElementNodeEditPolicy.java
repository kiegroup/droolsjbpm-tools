package org.drools.eclipse.flow.common.editor.policy;
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
import org.drools.eclipse.flow.common.editor.core.command.ElementConnectionCreateCommand;
import org.drools.eclipse.flow.common.editor.core.command.ReconnectElementConnectionSourceCommand;
import org.drools.eclipse.flow.common.editor.core.command.ReconnectElementConnectionTargetCommand;
import org.drools.eclipse.flow.common.editor.editpart.ElementEditPart;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.GraphicalNodeEditPolicy;
import org.eclipse.gef.requests.CreateConnectionRequest;
import org.eclipse.gef.requests.ReconnectRequest;

/**
 * Policy for editing an element node.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class ElementNodeEditPolicy extends GraphicalNodeEditPolicy {

    protected Command getConnectionCompleteCommand(CreateConnectionRequest request) {
        ElementConnectionCreateCommand cmd =
            (ElementConnectionCreateCommand) request.getStartCommand();
        cmd.setConnection((ElementConnection) request.getNewObject());
        cmd.setTarget(getElement());
        return cmd;
    }

    protected Command getConnectionCreateCommand(CreateConnectionRequest request) {
        ElementConnectionCreateCommand cmd =
            new ElementConnectionCreateCommand();
        cmd.setConnection((ElementConnection) request.getNewObject());
        cmd.setSource(getElement());
        request.setStartCommand(cmd);
        return cmd;
    }

    protected ElementEditPart getActivityPart() {
        return (ElementEditPart) getHost();
    }

    protected ElementWrapper getElement() {
        return (ElementWrapper) getHost().getModel();
    }

    protected Command getReconnectSourceCommand(ReconnectRequest request) {
        ReconnectElementConnectionSourceCommand cmd
            = new ReconnectElementConnectionSourceCommand();
        cmd.setConnection((ElementConnection) request.getConnectionEditPart().getModel());
        cmd.setSource(getElement());
        return cmd;
    }

    protected Command getReconnectTargetCommand(ReconnectRequest request) {
        ReconnectElementConnectionTargetCommand cmd
            = new ReconnectElementConnectionTargetCommand();
        cmd.setConnection((ElementConnection) request.getConnectionEditPart().getModel());
        cmd.setTarget(getElement());
        return cmd;
    }

}
