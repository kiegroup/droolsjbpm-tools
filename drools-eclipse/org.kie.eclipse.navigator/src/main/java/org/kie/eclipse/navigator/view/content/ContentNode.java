/******************************************************************************* 
 * Copyright (c) 2011 Red Hat, Inc. 
 * Distributed under license by Red Hat, Inc. All rights reserved. 
 * This program is made available under the terms of the 
 * Eclipse Public License v1.0 which accompanies this distribution, 
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * Red Hat, Inc. - initial API and implementation 
 ******************************************************************************/ 
package org.kie.eclipse.navigator.view.content;

import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.wst.server.core.IServer;
import org.kie.eclipse.navigator.view.server.IKieResourceHandler;

public abstract class ContentNode<T extends IContainerNode<?>> implements IContentNode<T> {

    protected IKieResourceHandler handler;
    protected IContainerNode<?> parent;
    protected T container;
    protected final String name;

    protected ContentNode(String name) {
        this.parent = null;
        this.container = null;
       	this.name = name;
    }

    protected ContentNode(T container, IKieResourceHandler handler) {
        this.parent = container instanceof IContainerNode ?
        		(IContainerNode<?>) container :
        			(container==null ? null : container.getParent());
        this.container = container;
        this.name = handler.getName();
        this.handler = handler;
    }

    public IContainerNode<?> getParent() {
        return parent;
    }

    public T getContainer() {
        return container;
    }

    public String getName() {
        return name;
    }

    public IServer getServer() {
        return getRoot().getServer();
    }

	public CommonNavigator getNavigator() {
		return getRoot().getNavigator();
	}
	
    public IContainerNode<?> getRoot() {
    	if (parent==null)
    		return (IContainerNode)this;
        return parent.getRoot();
    }

    public String getRuntimeId() {
    	return getRoot().getRuntimeId();
    }
    
    public void dispose() {
        container = null;
        parent = null;
        if (handler!=null) {
        	handler.dispose();
        	handler = null;
        }
    }

    /**
     * TODO: handler should be a generic IKieResourceHandler
     * @return
     */
    public IKieResourceHandler getHandler() {
    	 return handler;
    }
    
    public boolean isResolved() {
       	return getHandler().isLoaded();
    }
    
    public Object resolveContent() {
    	getHandler().load();
    	return this;
    }

    public Object getAdapter(Class adapter) {
    	return null;
    }
    
	@Override
	public abstract boolean equals(Object obj);
}
