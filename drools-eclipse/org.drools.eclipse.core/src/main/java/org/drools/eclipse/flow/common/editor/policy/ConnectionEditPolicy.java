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
import org.drools.eclipse.flow.common.editor.core.ElementConnectionFactory;
import org.drools.eclipse.flow.common.editor.core.ElementWrapper;
import org.drools.eclipse.flow.common.editor.core.ProcessWrapper;
import org.drools.eclipse.flow.common.editor.core.command.DeleteConnectionCommand;
import org.drools.eclipse.flow.common.editor.core.command.SplitConnectionCommand;
import org.drools.eclipse.flow.common.editor.editpart.ElementConnectionEditPart;
import org.eclipse.draw2d.PolylineConnection;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.requests.CreateRequest;
import org.eclipse.gef.requests.GroupRequest;

/**
 * Policy for editing connections.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class ConnectionEditPolicy extends org.eclipse.gef.editpolicies.ConnectionEditPolicy {

	private ElementConnectionFactory elementConnectionFactory;
	
	
	public void setDefaultElementConnectionFactory(ElementConnectionFactory factory) {
		if (factory == null) {
			throw new IllegalArgumentException("ElementConnectionFactory is null");
		}
		this.elementConnectionFactory = factory;
	}
	
	public ElementConnectionFactory getDefaultElementConnectionFactory() {
		return elementConnectionFactory;
	}
	
    public Command getCommand(Request request) {
        if (REQ_CREATE.equals(request.getType()))
            return getSplitTransitionCommand(request);
        return super.getCommand(request);
    }

    private PolylineConnection getConnectionFigure() {
        return ((PolylineConnection) ((ElementConnectionEditPart) getHost()).getFigure());
    }

    protected Command getDeleteCommand(GroupRequest request) {
        DeleteConnectionCommand cmd = new DeleteConnectionCommand();
        ElementConnection connection = (ElementConnection) getHost().getModel();
        cmd.setAntecedentTaskConnection(connection);
        cmd.setSource(connection.getSource());
        cmd.setTarget(connection.getTarget());
        return cmd;
    }

    protected Command getSplitTransitionCommand(Request request) {
        // TODO error when using this split, nodes do not know connections
    	if (elementConnectionFactory == null) {
    		throw new IllegalStateException("DefaultElementConnectionFactory is null");
    	}
        SplitConnectionCommand cmd = new SplitConnectionCommand();
        cmd.setElementConnection(((ElementConnection) getHost().getModel()));
        cmd.setNewSecondConnection(elementConnectionFactory.createElementConnection());
        cmd.setParent(((ProcessWrapper) ((ElementConnectionEditPart) getHost())
            .getSource().getParent().getModel()));
        cmd.setNewElement(((ElementWrapper) ((CreateRequest) request).getNewObject()));
        return cmd;
    }

    public EditPart getTargetEditPart(Request request) {
        if (REQ_CREATE.equals(request.getType()))
            return getHost();
        return null;
    }

    public void eraseTargetFeedback(Request request) {
        if (REQ_CREATE.equals(request.getType()))
            getConnectionFigure().setLineWidth(1);
    }

    public void showTargetFeedback(Request request) {
        if (REQ_CREATE.equals(request.getType()))
            getConnectionFigure().setLineWidth(2);
    }

}
