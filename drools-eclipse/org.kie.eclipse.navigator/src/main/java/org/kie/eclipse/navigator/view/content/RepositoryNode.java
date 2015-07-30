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
 * @author Bob Brodt
 ******************************************************************************/

package org.kie.eclipse.navigator.view.content;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jgit.lib.Repository;
import org.kie.eclipse.server.IKieProjectHandler;
import org.kie.eclipse.server.IKieRepositoryHandler;
import org.kie.eclipse.server.IKieResourceHandler;

/**
 *
 */
public class RepositoryNode extends ContainerNode<OrganizationNode> {
	
	/**
	 * @param container
	 * @param name
	 */
	protected RepositoryNode(OrganizationNode container, IKieRepositoryHandler repository) {
		super(container, repository);
	}

	protected RepositoryNode(ServerNode server, IKieRepositoryHandler repository) {
		super(null, repository);
        this.parent = server;
	}

	@Override
	protected List<? extends IContentNode<?>> createChildren() {
		clearHandlerChildren();
		load();
		List<ProjectNode> children = new ArrayList<ProjectNode>();
		Iterator<? extends IKieResourceHandler> iter = handlerChildren.iterator();
		while (iter.hasNext()) {
			IKieResourceHandler h = iter.next();
			if (h instanceof IKieProjectHandler)
				children.add(new ProjectNode(this,(IKieProjectHandler)h));
		}
		return children;

	}
    
	@Override
    public Object getAdapter(Class adapter) {
		if (adapter==Repository.class) {
			Object o = getHandler().getResource();
			if (o instanceof Repository)
				return o;
		}
		return super.getAdapter(adapter);
    }

}
