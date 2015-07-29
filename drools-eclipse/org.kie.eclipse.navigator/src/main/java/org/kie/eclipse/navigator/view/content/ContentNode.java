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
import org.kie.eclipse.server.IKieResourceHandler;

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

    @Override
	public IContainerNode<?> getParent() {
        return parent;
    }

    @Override
	public T getContainer() {
        return container;
    }

    @Override
	public String getName() {
        return name;
    }

    @Override
	public IServer getServer() {
    	if (parent==null)
    		return ((ServerNode)this).server;
        return getRoot().getServer();
    }

	@Override
	public CommonNavigator getNavigator() {
    	if (parent==null)
    		return ((ServerNode)this).navigator;
		return getRoot().getNavigator();
	}
	
    @Override
	public IContainerNode<?> getRoot() {
    	if (parent==null)
    		return (IContainerNode)this;
        return parent.getRoot();
    }

    public String getRuntimeId() {
    	return getRoot().getRuntimeId();
    }
    
    @Override
	public void dispose() {
        container = null;
        parent = null;
        if (handler!=null) {
        	handler.dispose();
        	handler = null;
        }
    }

    @Override
	public IKieResourceHandler getHandler() {
    	 return handler;
    }
    
    @Override
	public boolean isResolved() {
       	return getHandler().isLoaded();
    }
    
    @Override
	public Object resolveContent() {
    	getHandler().load();
    	return this;
    }

    @Override
	public Object getAdapter(Class adapter) {
		if (adapter==IKieResourceHandler.class) {
			return getHandler();
		}
    	return null;
    }
    
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof IContentNode) {
			try {
				IContentNode<?> other = (IContentNode<?>)obj;
				return other.getName().equals(this.getName());
			}
			catch (Exception ex) {
			}
		}
		return false;
	}

	@Override
	public void refresh() {
		getNavigator().getCommonViewer().refresh(getRoot());
	}
}
