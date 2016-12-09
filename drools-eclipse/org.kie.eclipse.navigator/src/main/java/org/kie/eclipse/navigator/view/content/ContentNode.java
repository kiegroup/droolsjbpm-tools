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

import org.kie.eclipse.Activator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.wst.server.core.IServer;
import org.kie.eclipse.navigator.view.IKieNavigatorView;
import org.kie.eclipse.server.IKieResourceHandler;

public abstract class ContentNode<T extends IContainerNode<?>> implements IContentNode<T>, IWorkbenchAdapter {

    protected IKieResourceHandler handler;
    protected IContainerNode<?> parent;
    protected final String name;

    protected ContentNode(String name) {
        this.parent = null;
       	this.name = name;
    }

    protected ContentNode(T parent, IKieResourceHandler handler) {
        this.parent = parent;
        this.name = handler.getName();
        this.handler = handler;
    }

    @Override
	public IContainerNode<?> getParent() {
        return parent;
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
	public IKieNavigatorView getNavigator() {
		try {
	    	if (parent==null)
	    		return ((ServerNode)this).navigator;
			return getRoot().getNavigator();
		}
		catch (Exception e) {
			Activator.logError(e.getMessage(), e);
		}
		return null;
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
//        parent = null;
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
       	return getHandler()!=null && getHandler().isLoaded();
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
		if (adapter==IWorkbenchAdapter.class) {
			return this;
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
	public int compareTo(Object arg0) {
		if (arg0 instanceof RepositoryNode) {
			if (this instanceof RepositoryNode)
				return getName().compareTo(((RepositoryNode)arg0).getName());
			return 1;
		}
		if (this instanceof RepositoryNode) {
			if (arg0 instanceof RepositoryNode)
				return getName().compareTo(((RepositoryNode)arg0).getName());
			return -1;
		}
		if (arg0 instanceof IContentNode) {
			return getName().compareTo(((IContentNode)arg0).getName());
		}
		return 0;
	}

	@Override
	public synchronized void refresh() {
		final IKieNavigatorView navigator = getNavigator();
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				try {
					navigator.setProperty(INTERNAL_REFRESH_KEY, Boolean.toString(true));
					navigator.refresh(getRoot());
				}
				finally {
					navigator.setProperty(INTERNAL_REFRESH_KEY, Boolean.toString(false));
				}
			}
		});
	}

	protected static Shell getShell() {
		return Display.getDefault().getActiveShell();
	}

	@Override
	public void handleException(final Throwable t) {
		t.printStackTrace();
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				MessageDialog.openError(getShell(), "Error", t.getMessage());
			}
		});
	}


	@Override
	public Object[] getChildren(Object o) {
		return null;
	}

	@Override
	public ImageDescriptor getImageDescriptor(Object object) {
		return null;
	}

	@Override
	public String getLabel(Object o) {
		return getName();
	}

	@Override
	public Object getParent(Object o) {
		return getParent();
	}
}
