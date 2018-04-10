/*******************************************************************************
 * Copyright (c) 2011, 2012, 2013, 2014 Red Hat, Inc.
 *  All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Red Hat, Inc. - initial API and implementation
 *
 ******************************************************************************/

package org.kie.eclipse.navigator.view.content;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.wst.server.core.IServer;
import org.kie.eclipse.IKieConstants;
import org.kie.eclipse.navigator.view.IKieNavigatorView;
import org.kie.eclipse.server.IKieSpaceHandler;
import org.kie.eclipse.server.IKieRepositoryHandler;
import org.kie.eclipse.server.IKieResourceHandler;
import org.kie.eclipse.server.KieServerHandler;

/**
 *
 */
public class ServerNode extends ContainerNode implements IPropertyChangeListener, IKieConstants {

	protected IKieNavigatorView navigator;
    protected final IServer server;

	/**
	 * @param server
	 * @param name
	 */
	public ServerNode(IServer server, IKieNavigatorView navigator) {
		super(server==null ? "root" : server.getName());
		this.server = server;
		this.navigator = navigator;
        handler = server==null ? null : new KieServerHandler(server);
        org.kie.eclipse.Activator.getDefault().getPreferenceStore().addPropertyChangeListener(this);
	}
	
	@Override
	protected List<? extends IContentNode<?>> createChildren() {
		clearHandlerChildren();
		load();
		List<IContentNode<?>> children = new ArrayList<IContentNode<?>>();
		Iterator<? extends IKieResourceHandler> iter = handlerChildren.iterator();
		while (iter.hasNext()) {
			IKieResourceHandler h = iter.next();
			IContentNode<?> n = null;
			if (h instanceof IKieSpaceHandler)
				n = new SpaceNode(this, (IKieSpaceHandler)h);
			else if (h instanceof IKieRepositoryHandler)
				n = new RepositoryNode(this,(IKieRepositoryHandler)h);
			if (n!=null)
				children.add(n);
		}
		return children;
	}

    @Override
	public IServer getServer() {
        return server;
    }

	@Override
	public IKieNavigatorView getNavigator() {
		return navigator;
	}
	
	@Override
	public IKieResourceHandler getHandler() {
    	 if (handler==null) {
    		 handler = new KieServerHandler(server);
    	 }
    	 return handler;
    }
    @Override
	public String getRuntimeId() {
    	return getHandler().getRuntimeId();
    }
    
	@Override
	public boolean isResolved() {
		return getServer().getServerState() == IServer.STATE_STARTED;
	}

	@Override
	public void dispose() {
		super.dispose();
		org.kie.eclipse.Activator.getDefault().getPreferenceStore().removePropertyChangeListener(this);
	}

	/* (non-Javadoc)
	 * @see org.kie.eclipse.navigator.view.content.ContentNode#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ServerNode) {
			try {
				ServerNode other = (ServerNode)obj;
				return other.getServer().getId().equals(this.getServer().getId());
			}
			catch (Exception ex) {
			}
		}
		return false;
	}

	@Override
	public void propertyChange(PropertyChangeEvent event) {
		String name = event.getProperty();
		if (name.endsWith(IKieConstants.PREF_GIT_REPO_PATH) || name.endsWith(IKieConstants.PREF_USE_DEFAULT_GIT_PATH)) {
	        clearChildren();
	        navigator.refresh(this);
		}
	}
}
