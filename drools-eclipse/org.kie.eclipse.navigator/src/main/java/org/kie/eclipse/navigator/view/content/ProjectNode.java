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

import java.util.List;

import org.kie.eclipse.server.IKieProjectHandler;

/**
 *
 */
public class ProjectNode extends ContainerNode<RepositoryNode> {
	
	/**
	 * @param container
	 * @param name
	 */
	protected ProjectNode(RepositoryNode container, IKieProjectHandler project) {
		super(container, project);
	}

	/* (non-Javadoc)
	 * @see org.kie.eclipse.navigator.view.content.ContainerNode#createChildren()
	 */
	@Override
	protected List<? extends IContentNode<?>> createChildren() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Object resolveContent() {
		Object resource = getHandler().load();
		if (resource!=null)
			return resource;
		return super.resolveContent();
	}

	/* (non-Javadoc)
	 * @see org.kie.eclipse.navigator.view.content.ContentNode#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		try {
			ProjectNode other = (ProjectNode) obj;
			return other.getName().equals(this.getName());
		}
		catch (Exception ex) {
		}
		return false;
	}
}
