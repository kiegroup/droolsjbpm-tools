package org.drools.eclipse.flow.common.editor.core;
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

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.draw2d.geometry.Point;

/**
 * A connection between two model elements.
 * 
 * @author <a href="mailto:kris_verlaenen@hotmail.com">Kris Verlaenen</a>
 */
public abstract class ElementConnection implements Serializable {
    
	private static final long serialVersionUID = 400L;
	
	public static final int CHANGE_BENDPOINTS = 1;
	public static final int CHANGE_LABEL = 2;
	
	private ElementWrapper source;
    private ElementWrapper target;
    private transient List<Point> bendpoints = new ArrayList<Point>();
    private transient List<ModelListener> listeners = new ArrayList<ModelListener>();
    
    public ElementConnection() {
    }
    
    public void localSetSource(ElementWrapper source) {
    	this.source = source;
    }
    
    public void localSetTarget(ElementWrapper target) {
        this.target = target;
    }
    
    public void disconnect() {
    	if (source == null) {
    		throw new IllegalStateException("Can't disconnect, source is null");
    	}
    	if (target == null) {
    		throw new IllegalStateException("Can't disconnect, target is null");
    	}
    	source.removeOutgoingConnection(this);
    	target.removeIncomingConnection(this);
    	source = null;
    	target = null;
    }
    
    public void connect(ElementWrapper source, ElementWrapper target) {
    	if (source == null) {
    		throw new IllegalArgumentException("source is null");
    	}
        if (this.source != null) {
            throw new IllegalStateException("The source of a connection cannot be changed");
        }
        if (target == null) {
    		throw new IllegalArgumentException("target is null");
    	}
        if (this.target != null) {
            throw new IllegalStateException("The target of a connection cannot be changed");
        }
        this.source = source;
        this.target = target;
    	source.addOutgoingConnection(this);
        target.addIncomingConnection(this);
    }
    
    
    public ElementWrapper getSource() {
        return source;
    }
    
    public ElementWrapper getTarget() {
        return target;
    }

    public void addBendpoint(int index, Point point) {
        bendpoints.add(index, point);
        internalSetBendpoints(bendpoints);
        notifyListeners(CHANGE_BENDPOINTS);
    }
    
    public void removeBendpoint(int index) {
        bendpoints.remove(index);
        internalSetBendpoints(bendpoints);
        notifyListeners(CHANGE_BENDPOINTS);
    }

    public void replaceBendpoint(int index, Point point) {
        bendpoints.set(index, point);
        internalSetBendpoints(bendpoints);
        notifyListeners(CHANGE_BENDPOINTS);
    }

    protected void internalSetBendpoints(List<Point> bendPoints) {
    }

    public void localSetBendpoints(List<Point> bendpoints) {
        this.bendpoints = bendpoints;
    }
    
    public List<Point> getBendpoints() {
        if (bendpoints == null) {
            bendpoints = internalGetBendpoints();
        }
        return bendpoints;
    }
    
    protected abstract List<Point> internalGetBendpoints();

    public void addListener(ModelListener listener) {
        listeners.add(listener);
    }

    public void removeListener(ModelListener listener) {
        listeners.remove(listener);
    }

    public void notifyListeners(int change) {
        ModelEvent event = new ModelEvent(change);
        for (Iterator it = listeners.iterator(); it.hasNext(); ) {
        	ModelListener listener = (ModelListener) it.next();
        	listener.modelChanged(event);
        }
    }

    private void readObject(ObjectInputStream aInputStream)
            throws ClassNotFoundException, IOException {
        aInputStream.defaultReadObject();
        listeners = new ArrayList();
    }
}
