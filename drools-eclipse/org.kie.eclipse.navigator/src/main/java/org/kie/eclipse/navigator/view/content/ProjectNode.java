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

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.swt.widgets.Display;
import org.kie.eclipse.server.IKieProjectHandler;

/**
 *
 */
public class ProjectNode extends ContainerNode<RepositoryNode> implements IResourceChangeListener {
	
	/**
	 * @param container
	 * @param name
	 */
	protected ProjectNode(RepositoryNode container, IKieProjectHandler project) {
		super(container, project);
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	/* (non-Javadoc)
	 * @see org.kie.eclipse.navigator.view.content.ContainerNode#createChildren()
	 */
	@Override
	protected List<? extends IContentNode<?>> createChildren() {
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

	@Override
	public void resourceChanged(final IResourceChangeEvent event) {
		// TODO: FIX THIS MESS!
		// Figure out why we are getting multiple REMOVED events for the same IProject resource
		// There should be only one!
//		IResourceDelta delta = event.getDelta();
//		if (delta!=null) {
//			try {
//				delta.accept(new IResourceDeltaVisitor() {
//					@Override
//					public boolean visit(IResourceDelta delta) throws CoreException {
//						IResource resource = delta.getResource();
//						if (resource instanceof IProject) {
//							switch (delta.getKind()) {
//							case IResourceDelta.REMOVED:
//								IKieProjectHandler handler = (IKieProjectHandler) getHandler();
//								if (resource==handler.getProject()) {
//									File directory = handler.getDirectory();
//									if (directory!=null && !directory.exists()) {
//										// The actual server request to remove the project needs
//										// to happen in the UI thread
//										Display.getDefault().asyncExec(new Runnable() {
//											@Override
//											public void run() {
//												try {
//													handler.getDelegate().deleteProject(handler);
//												}
//												catch (IOException e) {
//													e.printStackTrace();
//												}
//											}
//										});
//									}
//								}
//								return false;
//							}
//						}
//						return true;
//					}
//					
//				});
//			}
//			catch (CoreException e) {
//				e.printStackTrace();
//			}
//		}
	}
}
