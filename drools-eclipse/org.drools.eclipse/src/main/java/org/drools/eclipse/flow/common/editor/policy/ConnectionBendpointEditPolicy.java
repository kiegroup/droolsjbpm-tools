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

import org.drools.eclipse.flow.common.editor.core.command.CreateBendpointCommand;
import org.drools.eclipse.flow.common.editor.core.command.DeleteBendpointCommand;
import org.drools.eclipse.flow.common.editor.core.command.MoveBendpointCommand;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.BendpointEditPolicy;
import org.eclipse.gef.requests.BendpointRequest;

/**
 * Policy for bendpoints of connections.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class ConnectionBendpointEditPolicy extends BendpointEditPolicy {

    protected Command getCreateBendpointCommand(BendpointRequest request) {
        Point point = request.getLocation();
        getConnection().translateToRelative(point);
        
        CreateBendpointCommand command = new CreateBendpointCommand();
        command.setLocation(point);
        command.setConnection(getHost().getModel());
        command.setIndex(request.getIndex());
        
        return command;
    }

    protected Command getDeleteBendpointCommand(BendpointRequest request) {
        DeleteBendpointCommand command = new DeleteBendpointCommand();
        command.setConnectionModel(getHost().getModel());
        command.setIndex(request.getIndex());
        return command;
    }

    protected Command getMoveBendpointCommand(BendpointRequest request) {
        Point location = request.getLocation();
        getConnection().translateToRelative(location);

        MoveBendpointCommand command = new MoveBendpointCommand();
        command.setConnectionModel(getHost().getModel());
        command.setIndex(request.getIndex());
        command.setNewLocation(location);

        return command;

    }
}
