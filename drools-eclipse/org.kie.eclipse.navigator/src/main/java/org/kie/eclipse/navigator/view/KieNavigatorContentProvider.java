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
        public boolean shouldRun() {
            return pendingUpdates.size() > 0;
        }

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

    public void dispose() {
        viewer = null;
        loadElementJob.cancel();
        pendingUpdates.clear();
    }
    
	private ServerToolTip tooltip = null;
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        if (viewer instanceof TreeViewer) {
            this.viewer = (TreeViewer) viewer;
        } else {
            viewer = null;
        }
		if( tooltip != null )
			tooltip.deactivate();
		tooltip = new ServerToolTip(((TreeViewer)viewer).getTree()) {
			protected boolean isMyType(Object selected) {
				return selected instanceof ServerNode;
			}
			protected void fillStyledText(Composite parent, StyledText sText, Object o) {
				sText.setText("View JBoss-7 management details."); //$NON-NLS-1$
			}
		};
		tooltip.setShift(new Point(15, 8));
		tooltip.setPopupDelay(500); // in ms
		tooltip.setHideOnMouseDown(true);
		tooltip.activate();
    }

    public Object[] getElements(Object inputElement) {
        return getChildren(inputElement);
    }

    List<ServerNode> rootElements;
    
    public Object[] getChildren(Object parentElement) {
		List<Object> results = new ArrayList<Object>();
    	if (parentElement instanceof KieNavigatorContentRoot) {
    		List<? extends Object> children = ((KieNavigatorContentRoot)parentElement).getChildren();
    		results.addAll(children);
//    		if (rootElements==null) {
//    			rootElements = new ArrayList<ServerNode>();
//	    		for (IServer s : ServerCore.getServers()) {
//	    			if (KieServer.isSupportedServer(s)) {
//	    				s = new ServerProxy(s);
//	    				ServerNode node = new ServerNode(s);
//	    				rootElements.add(node);
//	    				// not strictly necessary at this level,
//	    				// see comment below.
//	    				results.add(node.resolveContent());
//	    			}
//	    		}
//    		}
//    		else {
//	    		for (IServer s : ServerCore.getServers()) {
//	    			if (KieServer.isSupportedServer(s)) {
//	    				boolean found = false;
//	    				for (ServerNode n : rootElements) {
//	    					if (n.getServer().getId().equals(s.getId())) {
//	    						found = true;
//	    						break;
//	    					}
//	    				}
//	    				if (!found) {
//	    					// found a new server
//		    				s = new ServerProxy(s);
//		    				ServerNode node = new ServerNode(s);
//		    				rootElements.add(node);
//	    				}
//	    			}
//	    		}
//	    		List<ServerNode> removed = new ArrayList<ServerNode>();
//	    		for (ServerNode n : rootElements) {
//	    			if (KieServer.isSupportedServer(n.getServer())) {
//						boolean found = false;
//			    		for (IServer s : ServerCore.getServers()) {
//			    			if (KieServer.isSupportedServer(s)) {
//		    					if (n.getServer().getId().equals(s.getId())) {
//		    						found = true;
//		    						break;
//		    					}
//			    			}
//			    		}
//			    		if (!found)
//			    			removed.add(n);
//	    			}
//	    		}
//	    		for (ServerNode n : removed) {
//	    			n.dispose();
//	    		}
//	    		rootElements.removeAll(removed);
//	    		for (ServerNode n : rootElements) {
//	    			results.add(n.resolveContent());
//	    		}
//    		}
        } else
        if (parentElement instanceof IContainerNode) {
            IContainerNode<?> container = (IContainerNode<?>) parentElement;
            if (pendingUpdates.containsKey(container)) {
                return new Object[] {PENDING };
            }
            List<? extends Object> children = container.getChildren();
            if (children == null) {
                pendingUpdates.putIfAbsent(container, PENDING);
                loadElementJob.schedule();
                return new Object[] {PENDING };
            }
            for (Object node : children) {
            	if (node instanceof IContentNode) {
            		// Resolve the content of this node: this may or
            		// may not be the node itself. This is currently
            		// used only by IProjectNode, which may return
            		// the workspace IProject itself. From the IProject
            		// level down, we will delegate to the Project View
            		// for node content and labels.
            		node = ((IContentNode)node).resolveContent();
            	}
            	results.add(node);
            }
        }
        return results.toArray();
    }

    public Object getParent(Object element) {
        if (element instanceof IContentNode) {
            Object parent = ((IContentNode<?>) element).getContainer();
            if (parent == null) {
                parent = ((IContentNode<?>) element).getServer();
            }
            return parent;
        }
        return null;
    }

    public boolean hasChildren(Object element) {
        if (element instanceof IServer) {
            return true;
        } else if (element instanceof IContainerNode) {
            return ((IContainerNode)element).hasChildren();
        }
        return false;
    }

}
