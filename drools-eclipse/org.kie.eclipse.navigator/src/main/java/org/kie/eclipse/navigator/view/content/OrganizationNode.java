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

import org.kie.eclipse.navigator.view.server.IKieOrganizationHandler;
import org.kie.eclipse.navigator.view.server.IKieRepositoryHandler;
import org.kie.eclipse.navigator.view.server.IKieResourceHandler;

/**
 *
 */
public class OrganizationNode extends ContainerNode<ServerNode> {
	
	/**
	 * @param container
	 * @param name
	 */
	protected OrganizationNode(ServerNode container, IKieOrganizationHandler organization) {
		super(container, organization);
	}
	
	protected List<? extends IContentNode<?>> createChildren() {
		List<RepositoryNode> children = new ArrayList<RepositoryNode>();
		Iterator<? extends IKieResourceHandler> iter = handlerChildren.iterator();
		while (iter.hasNext()) {
			IKieResourceHandler h = iter.next();
			if (h instanceof IKieRepositoryHandler)
				children.add(new RepositoryNode(this,(IKieRepositoryHandler)h));
		}
		return children;
	}
	
	@Override
	public boolean isResolved() {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.kie.eclipse.navigator.view.content.ContentNode#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		try {
			OrganizationNode other = (OrganizationNode) obj;
			return this.getName().equals(other.getName());
		}
		catch (Exception ex) {
		}
		return false;
	}
}
