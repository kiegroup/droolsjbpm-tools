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

package org.kie.eclipse.navigator;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.ServerCore;
import org.kie.eclipse.navigator.view.content.ContainerNode;
import org.kie.eclipse.navigator.view.content.IContentNode;
import org.kie.eclipse.navigator.view.content.ServerNode;
import org.kie.eclipse.navigator.view.server.KieServerHandler;
import org.kie.eclipse.navigator.view.server.ServerProxy;

/**
 * This is a placeholder class that replaces the default IWorkspaceRoot used as
 * initial input for CommonViewers. This class is part of the public API and
 * allows other CommonNavigator plugins to embed KIE Navigator content as child
 * or root nodes into their own content.
 */
public class KieNavigatorContentRoot {

	protected CommonNavigator viewer;
	protected List<? extends IContentNode<?>> children;

	/**
	 * @param kieNavigatorView
	 */
	public KieNavigatorContentRoot(CommonNavigator viewer) {
		this.viewer = viewer;
	}

    public List<? extends Object> getChildren() {
		children = ContainerNode.updateChildren(children, createChildren());
		return children;
    }
    
	protected List<? extends IContentNode<?>> createChildren() {
		List<ServerNode> children = new ArrayList<ServerNode>();
		for (IServer s : ServerCore.getServers()) {
			if (KieServerHandler.isSupportedServer(s)) {
				s = new ServerProxy(s);
				ServerNode node = new ServerNode(s, viewer);
				children.add(node);
			}
		}
		return ( List<? extends IContentNode<?>> )children;
	}
}
