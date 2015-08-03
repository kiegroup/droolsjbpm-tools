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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.wst.server.core.IServer;
import org.kie.eclipse.server.IKieResourceHandler;

public abstract class ContainerNode<T extends IContainerNode<?>> extends ContentNode<T> implements IContainerNode<T> {

    private IErrorNode error;
	protected List<? extends IKieResourceHandler> handlerChildren;
	protected List<? extends IContentNode<?>> children;

    protected ContainerNode(String name) {
        super(name);
    }

    protected ContainerNode(T parent, IKieResourceHandler handler) {
        super(parent, handler);
    }
    
	@Override
	public boolean hasChildren() {
		return isResolved();
	}

    @Override
	public List<? extends IContentNode<?>> getChildren() {
        if (error != null) {
            return Collections.singletonList(error);
        }
		if (handlerChildren!=null) {
			children = updateChildren(children, createChildren());
			return children;
		}
		return null;
    }

    protected abstract List<? extends IContentNode<?>> createChildren();
    
    @Override
	public final void clearChildren() {
        clearError();
        clearHandlerChildren();
		if (children!=null) {
			for (IContentNode<?> n : children)
				n.dispose();
			children.clear();
			children = null;
		}
    }

    public final void clearHandlerChildren() {
		if (handlerChildren!=null) {
			for (IKieResourceHandler h : handlerChildren)
				h.dispose();
			handlerChildren.clear();
			handlerChildren = null;
		}
    }
    
    protected void setError(IErrorNode error) {
        clearError();
        this.error = error;
    }

    @Override
    public void dispose() {
        clearChildren();
        super.dispose();
    }

    @Override
	public final void load() {
        if (getServer().getServerState() != IServer.STATE_STARTED) {
            setError(new ErrorNode(this, "Not connected"));
            return;
        }
        try {
    		handlerChildren = getHandler().getChildren();
            clearError();
        } catch (Exception e) {
            setError(new ErrorNode(this, e));
        }
    }

	@SuppressWarnings("unchecked")
	public static List<? extends IContentNode<?>> updateChildren(List<? extends IContentNode<?>> children, List<? extends IContentNode<?>> newChildren) {
		if (children==null)
			return newChildren;
		else {
			List<IContentNode<?>> removed = new ArrayList<IContentNode<?>>();
			Iterator<? extends IContentNode<?>> newIter = newChildren.iterator();
			while (newIter.hasNext()) {
				IContentNode<?> newChild = newIter.next();
				boolean found = false;
				Iterator<? extends IContentNode<?>> oldIter = children.iterator();
				while (oldIter.hasNext()) {
					IContentNode<?> oldChild = oldIter.next();
					if (oldChild.equals(newChild)) {
						newChild.dispose();
						found = true;
						break;
					}
				}
				if (!found) {
					((List<IContentNode<?>>)children).add(newChild);
				}
			}

			Iterator<? extends IContentNode<?>> oldIter = children.iterator();
			while (oldIter.hasNext()) {
				IContentNode<?> oldChild = oldIter.next();
				boolean found = false;
				Iterator<? extends IContentNode<?>> newIter2 = newChildren.iterator();
				while (newIter2.hasNext()) {
					IContentNode<?> newChild = newIter2.next();
					if (oldChild.equals(newChild)) {
						found = true;
						break;
					}
				}
				if (!found) {
					removed.add(oldChild);
					oldChild.dispose();
				}
			}
			children.removeAll(removed);
		}
		Collections.sort(children);
		return children;
	}

    private void clearError() {
        if (error != null) {
            error.dispose();
            error = null;
        }
    }
}
