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
package org.kie.eclipse.navigator.view;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.wst.server.core.IServer;
import org.kie.eclipse.navigator.Activator;
import org.kie.eclipse.navigator.view.content.IContainerNode;
import org.kie.eclipse.navigator.view.content.IContentNode;
import org.kie.eclipse.navigator.view.content.IErrorNode;
import org.kie.eclipse.navigator.view.content.OrganizationNode;
import org.kie.eclipse.navigator.view.content.ProjectNode;
import org.kie.eclipse.navigator.view.content.RepositoryNode;
import org.kie.eclipse.navigator.view.content.ServerNode;

/**
 * Label provider implementation for content nodes.
 */
public class KieNavigatorLabelProvider extends LabelProvider {

	public String getText(Object element) {
		if (element instanceof IContainerNode) {
			return ((IContainerNode<?>) element).getName();
		} else if (element instanceof IErrorNode) {
			return ((IErrorNode) element).getText();
		} else if (element instanceof IContentNode) {
			return ((IContentNode<?>) element).getName();
		} else if (element == KieNavigatorContentProvider.PENDING) {
			return "Loading...";
		}
		return super.getText(element);
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof ServerNode) {
			if (((ServerNode) element).getServer().getServerState() == IServer.STATE_STARTED)
				return Activator.getImageDescriptor(Activator.IMG_SERVER_STARTED).createImage();
			return Activator.getImageDescriptor(Activator.IMG_SERVER_STOPPED).createImage();
		} else if (element instanceof OrganizationNode) {
			return Activator.getImageDescriptor(Activator.IMG_ORGANIZATION).createImage();
		} else if (element instanceof RepositoryNode) {
			if (((RepositoryNode) element).isResolved())
				return Activator.getImageDescriptor(Activator.IMG_REPOSITORY).createImage();
			return Activator.getImageDescriptor(Activator.IMG_REPOSITORY_UNAVAILABLE).createImage();
		} else if (element instanceof ProjectNode) {
			if (((ProjectNode) element).isResolved())
				return Activator.getImageDescriptor(Activator.IMG_PROJECT).createImage();
			return Activator.getImageDescriptor(Activator.IMG_PROJECT_CLOSED).createImage();
		} else if (element instanceof IContainerNode<?>) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
		} else if (element instanceof IErrorNode) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJS_ERROR_TSK);
		} else if (element instanceof IContentNode<?>) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
		}
		return super.getImage(element);
	}
}
