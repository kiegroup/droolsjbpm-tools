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
import org.eclipse.swt.widgets.Display;
import org.kie.eclipse.server.IKieProjectHandler;
import org.kie.eclipse.server.IKieResourceHandler;

/**
 *
 */
public class ProjectNode extends ContainerNode<RepositoryNode> implements IResourceChangeListener {
	
	/**
	 * @param parent
	 * @param name
	 */
	protected ProjectNode(RepositoryNode parent, IKieProjectHandler project) {
		super(parent, project);
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
		if (resource==null)
			resource = super.resolveContent();
		
		if (resource instanceof IProject) {
			try {
				((IProject)resource).setSessionProperty(IKieResourceHandler.RESOURCE_KEY, this);
			}
			catch (Exception e) {
			}
		}
		return resource;
	}

	@Override
	public void dispose() {
		try {
			IProject project = (IProject) getHandler().getResource();
			project.setSessionProperty(IKieResourceHandler.RESOURCE_KEY, null);
		}
		catch (Exception e) {
		}
		super.dispose();
        ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
	}

	@Override
	public void resourceChanged(final IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		if (delta!=null) {
			try {
				delta.accept(new IResourceDeltaVisitor() {
					@Override
					public boolean visit(IResourceDelta delta) throws CoreException {
						IResource resource = delta.getResource();
						if (resource instanceof IProject) {
							switch (delta.getKind()) {
							case IResourceDelta.REMOVED:
								final ProjectNode container = ProjectNode.this;
								final IKieProjectHandler handler = (IKieProjectHandler) getHandler();
								if (resource==handler.getResource()) {
									handler.setResource(null);
									File directory = handler.getDirectory();
									if (directory!=null && !directory.exists()) {
										// The actual server request to remove the project needs
										// to happen in the UI thread
										Display.getDefault().asyncExec(new Runnable() {
											@Override
											public void run() {
												try {
													handler.getDelegate().deleteProject(handler);
													container.getParent().clearChildren();
													refresh();
												}
												catch (IOException e) {
													e.printStackTrace();
												}
											}
										});
									}
								}
							}
						}
						return true;
					}
					
				});
			}
			catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}
}
