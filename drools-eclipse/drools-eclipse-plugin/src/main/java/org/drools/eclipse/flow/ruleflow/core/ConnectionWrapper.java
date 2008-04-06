package org.drools.eclipse.flow.ruleflow.core;
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
import org.drools.workflow.core.Connection;
import org.drools.workflow.core.Node;
import org.drools.workflow.core.impl.ConnectionImpl;
import org.eclipse.draw2d.geometry.Point;

/**
 * Wrapper for a connection.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public class ConnectionWrapper extends ElementConnection {
		
	private static final long serialVersionUID = 400L;

	private ConnectionImpl connection;
	
	public ConnectionWrapper() {
	}
	
	public Connection getConnection() {
	    return connection;
	}
	
	public void localSetConnection(Connection connection) {
	    this.connection = (ConnectionImpl) connection;
	}
	
	public void disconnect() {
		super.disconnect();
		connection.terminate();
		connection = null;
	}
	
	public void connect(ElementWrapper source, ElementWrapper target) {
		super.connect(source, target);
		Node from = ((NodeWrapper) getSource()).getNode();
		Node to = ((NodeWrapper) getTarget()).getNode();
		connection = new ConnectionImpl(from, Node.CONNECTION_DEFAULT_TYPE, to, Node.CONNECTION_DEFAULT_TYPE);		
	}

    protected List<Point> internalGetBendpoints() {
        return (List<Point>) stringToBendpoints((String) connection.getMetaData("bendpoints"));
    }
    
    protected void internalSetBendpoints(List<Point> bendpoints) {
        connection.setMetaData("bendpoints", bendpointsToString(bendpoints));
    }
    
    private String bendpointsToString(List<Point> bendpoints) {
        if (bendpoints == null) {
            return null;
        }
        String result = "[";
        for (Iterator<Point> iterator = bendpoints.iterator(); iterator.hasNext(); ) {
            Point point = iterator.next();
            result += point.x + "," + point.y + (iterator.hasNext() ? ";" : "");
        }
        result += "]";
        return result;
    }
    
    private List<Point> stringToBendpoints(String s) {
        List<Point> result = new ArrayList<Point>();
        if (s == null) {
            return result;
        }
        s = s.substring(1, s.length() - 1);
        String[] bendpoints = s.split(";");
        for (String bendpoint: bendpoints) {
            String[] xy = bendpoint.split(",");
            result.add(new Point(new Integer(xy[0]), new Integer(xy[1])));
        }
        return result;
    }
	
}
