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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.wst.server.core.IServer;
import org.jboss.ide.eclipse.as.ui.Messages;
import org.jboss.tools.as.wst.server.ui.xpl.ServerToolTip;
import org.kie.eclipse.navigator.KieNavigatorContentRoot;
import org.kie.eclipse.navigator.view.content.IContainerNode;
import org.kie.eclipse.navigator.view.content.IContentNode;
import org.kie.eclipse.navigator.view.content.ServerNode;

/**
 * Content provider implementation for server management details.
 */
public class KieNavigatorContentProvider implements ITreeContentProvider {

    /** Represents a pending request in the tree. */
    static final Object PENDING = new Object();
    private ConcurrentMap<IContainerNode<?>, Object> pendingUpdates = new ConcurrentHashMap<IContainerNode<?>, Object>();
    private transient TreeViewer viewer;

    /**
     * Loads content for specified nodes, then refreshes the content in the
     * tree.
     */
    private Job loadElementJob = new Job(Messages.ServerContent_Job_Title) {
        @Override
		public boolean shouldRun() {
            return pendingUpdates.size() > 0;
        }

        @Override
		protected IStatus run(final IProgressMonitor monitor) {
            monitor.beginTask(Messages.ServerContent_Job_Title, IProgressMonitor.UNKNOWN);
            try {
                final List<IContainerNode<?>> updatedNodes = new ArrayList<IContainerNode<?>>(pendingUpdates.size());
                for (IContainerNode<?> node : pendingUpdates.keySet()) {
                    try {
                        node.load();
                        updatedNodes.add(node);
                    } catch (Exception e) {
                    }
                    if (monitor.isCanceled()) {
                        pendingUpdates.keySet().removeAll(updatedNodes);
                        return Status.CANCEL_STATUS;
                    }
                }
                final TreeViewer viewer = KieNavigatorContentProvider.this.viewer;
                if (viewer == null) {
                    pendingUpdates.keySet().clear();
                } else {
                    viewer.getTree().getDisplay().asyncExec(new Runnable() {
                        @Override
						public void run() {
                            if (viewer.getTree().isDisposed()) {
                                return;
                            }
                            for (IContainerNode<?> node : updatedNodes) {
                                pendingUpdates.remove(node);
                                viewer.refresh(node);
                            }
                        }
                    });
                }
            } finally {
                monitor.done();
            }
            return Status.OK_STATUS;
        }
    };

    @Override
	public void dispose() {
        viewer = null;
        loadElementJob.cancel();
        pendingUpdates.clear();
    }
    
	private ServerToolTip tooltip = null;
    @Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (viewer instanceof TreeViewer) {
            this.viewer = (TreeViewer) viewer;
        } else {
            viewer = null;
        }
		if( tooltip != null )
			tooltip.deactivate();
		tooltip = new ServerToolTip(((TreeViewer)viewer).getTree()) {
			@Override
			protected boolean isMyType(Object selected) {
				return selected instanceof ServerNode;
			}
			@Override
			protected void fillStyledText(Composite parent, StyledText sText, Object o) {
				sText.setText("View JBoss-7 management details."); //$NON-NLS-1$
			}
		};
		tooltip.setShift(new Point(15, 8));
		tooltip.setPopupDelay(500); // in ms
		tooltip.setHideOnMouseDown(true);
		tooltip.activate();
    }

    @Override
	public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    List<ServerNode> rootElements;
    
	@Override
	public Object[] getChildren(Object parentElement) {
		final List<Object> results = new ArrayList<Object>();
		if (parentElement instanceof KieNavigatorContentRoot) {
			List<? extends Object> children = ((KieNavigatorContentRoot) parentElement).getChildren();
			results.addAll(children);
		}
		else if (parentElement instanceof IContainerNode) {
			IContainerNode<?> container = (IContainerNode<?>) parentElement;
			if (pendingUpdates.containsKey(container)) {
				return new Object[] { PENDING };
			}
			List<? extends Object> children = container.getChildren();
			if (children == null) {
				pendingUpdates.putIfAbsent(container, PENDING);
				loadElementJob.schedule();
				return new Object[] { PENDING };
			}
			for (Object node : children) {
				if (node instanceof IContentNode) {
					// Resolve the content of this node: this may or
					// may not be the node itself. This is currently
					// used only by IProjectNode, which may return
					// the workspace IProject itself. From the IProject
					// level down, we will delegate to the Project View
					// for node content and labels.
					node = ((IContentNode) node).resolveContent();
				}
				results.add(node);
			}
		}
		else if (parentElement instanceof IProject) {
			// TODO: support GoInto actions for Projects
			final IProject project = (IProject) parentElement;
			if (project.isAccessible()) {
				try {
					project.accept(new IResourceVisitor() {
						@Override
						public boolean visit(IResource resource) throws CoreException {
							if (resource!=project)
								results.add(resource);
							return !(resource instanceof IFolder) || resource==project;
						}
					});
				}
				catch (CoreException e) {
					e.printStackTrace();
				}
			}			
		}
		return results.toArray();
	}

    @Override
	public Object getParent(Object element) {
        if (element instanceof IContentNode) {
            Object parent = ((IContentNode<?>) element).getParent();
            if (parent == null) {
                parent = ((IContentNode<?>) element).getServer();
            }
            return parent;
        }
        return null;
    }

    @Override
	public boolean hasChildren(Object element) {
        if (element instanceof IServer) {
            return true;
        } else if (element instanceof IContainerNode) {
            return ((IContainerNode)element).hasChildren();
        }
        return false;
    }

}
