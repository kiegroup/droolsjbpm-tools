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
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.gef.commands.Command;

/**
 * A command for creating a bendpoint.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class CreateBendpointCommand extends Command {

    private ElementConnection connection;
    private Point location;
    private int index;

    public void execute() {
        connection.addBendpoint(index, location);
    }

    public void setConnection(Object model) {
        connection = (ElementConnection) model;
    }

    public void setIndex(int i) {
        index = i;
    }

    public void setLocation(Point point) {
        location = point;
    }

    public void undo() {
        connection.removeBendpoint(index);
    }
}