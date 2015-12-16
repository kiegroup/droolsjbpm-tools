/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.guvnor.tools.views.model;

import java.net.ConnectException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.progress.IDeferredWorkbenchAdapter;
import org.eclipse.ui.progress.IElementCollector;
import org.eclipse.webdav.IResponse;
import org.guvnor.tools.Activator;
import org.guvnor.tools.GuvnorRepository;
import org.guvnor.tools.Messages;
import org.guvnor.tools.utils.PlatformUtils;
import org.guvnor.tools.utils.webdav.IWebDavClient;
import org.guvnor.tools.utils.webdav.ResourceProperties;
import org.guvnor.tools.utils.webdav.WebDavClientFactory;
import org.guvnor.tools.utils.webdav.WebDavException;
import org.guvnor.tools.utils.webdav.WebDavServerCache;

/**
 * A container node for Guvnor structure.
 */
public class TreeParent extends TreeObject implements IDeferredWorkbenchAdapter {

        private ArrayList<TreeObject> children;

        public TreeParent(String name, Type nodeType) {
            super(name, nodeType);
            children = new ArrayList<TreeObject>();
        }
        public void addChild(TreeObject child) {
            children.add(child);
            child.setParent(this);
        }
        public void removeChild(TreeObject child) {
            children.remove(child);
            child.setParent(null);
        }
        public TreeObject [] getChildren() {
            return (TreeObject [])children.toArray(new TreeObject[children.size()]);
        }
        public boolean hasChildren() {
            return children.size()>0;
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#fetchDeferredChildren(java.lang.Object, org.eclipse.ui.progress.IElementCollector, org.eclipse.core.runtime.IProgressMonitor)
         */
        public void fetchDeferredChildren(Object object,
                                         IElementCollector collector,
                                         IProgressMonitor monitor) {
            if (!(object instanceof TreeParent)) {
                return;
            }
            TreeParent node = (TreeParent)object;
            if (node.getNodeType() == Type.NONE) {
                List<GuvnorRepository> reps = Activator.getLocationManager().getRepositories();
                monitor.beginTask(Messages.getString("pending"), reps.size()); //$NON-NLS-1$
                for (int i = 0; i < reps.size(); i++) {
                    TreeParent p = new TreeParent(reps.get(i).getLocation(), Type.REPOSITORY);
                    p.setParent(node);
                    p.setGuvnorRepository(reps.get(i));
                    ResourceProperties props = new ResourceProperties();
                    props.setBase(""); //$NON-NLS-1$
                    p.setResourceProps(props);
                    collector.add(p, monitor);
                    monitor.worked(1);
                }
                monitor.done();
            }
            if (EnumSet.of(Type.REPOSITORY, Type.GLOBALS, Type.PACKAGES, Type.SNAPSHOTS, Type.PACKAGE,
                    Type.SNAPSHOT_PACKAGE).contains(node.getNodeType())) {
                listDirectory(node, collector, monitor);
            }
        }

        /**
         * Creates a directory listing.
         * @param node The directory to list.
         * @param collector The collector for the elements listed.
         * @param monitor Progress monitor for the operation.
         */
        public void listDirectory(TreeParent node,
                                  IElementCollector collector,
                                 IProgressMonitor monitor) {
            monitor.beginTask(Messages.getString("pending"), 1); //$NON-NLS-1$

            monitor.worked(1);
            GuvnorRepository rep = node.getGuvnorRepository();
            try {
                IWebDavClient webdav = WebDavServerCache.getWebDavClient(rep.getLocation());
                if (webdav == null) {
                    webdav = WebDavClientFactory.createClient(new URL(rep.getLocation()));
                    WebDavServerCache.cacheWebDavClient(rep.getLocation(), webdav);
                }
                Map<String, ResourceProperties> listing = null;
                try {
                    listing = webdav.listDirectory(node.getFullPath());
                } catch (WebDavException wde) {
                    if (wde.getErrorCode() != IResponse.SC_UNAUTHORIZED) {
                        // If not an authentication failure, we don't know what to do with it
                        throw wde;
                    }
                    boolean retry = PlatformUtils.getInstance().authenticateForServer(
                                                node.getGuvnorRepository().getLocation(), webdav);
                    if (retry) {
                        listing = webdav.listDirectory(node.getFullPath());
                    }
                }
                if (listing != null) {
                    for (String s: listing.keySet()) {
                        ResourceProperties resProps = listing.get(s);
                        TreeObject o = null;
                        if (resProps.isDirectory()) {
                            Type childType;
                            switch (getNodeType()) {
                            case REPOSITORY:
                                if (s.startsWith("snapshot")) {
                                    childType = Type.SNAPSHOTS;
                                } else if (s.startsWith("packages")) {
                                    childType = Type.PACKAGES;
                                } else if (s.startsWith("globalarea")) {
                                    childType = Type.GLOBALS;
                                } else {
                                    childType = Type.PACKAGE;
                                }
                                break;
                            case SNAPSHOTS:
                                childType = Type.SNAPSHOT_PACKAGE;
                                break;
                            case SNAPSHOT_PACKAGE:
                                childType = Type.SNAPSHOT;
                                break;
                            default:
                                childType = Type.PACKAGE;
                            }
                            o = new TreeParent(s, childType);
                        } else {
                            o = new TreeObject(s, Type.RESOURCE);
                        }
                        o.setGuvnorRepository(rep);
                        o.setResourceProps(resProps);
                        node.addChild(o);
                        collector.add(o, monitor);
                    }
                }
                monitor.worked(1);
            } catch (WebDavException e) {
                if (e.getErrorCode() == IResponse.SC_UNAUTHORIZED) {
                    PlatformUtils.reportAuthenticationFailure();
                } else {
                    if (e.getErrorCode() == IResponse.SC_NOT_IMPLEMENTED) {
                        Activator.getDefault().displayMessage(IStatus.ERROR,
                                                           Messages.getString("rep.connect.fail")); //$NON-NLS-1$
                    } else {
                        Activator.getDefault().displayError(IStatus.ERROR, e.getMessage(), e, true);
                    }
                }
            } catch (ConnectException ce) {
                Activator.getDefault().
                    displayMessage(IStatus.ERROR,
                                  Messages.getString("rep.connect.fail")); //$NON-NLS-1$
            } catch (Exception e) {
                Activator.getDefault().displayError(IStatus.ERROR, e.getMessage(), e, true);
            }
        }

        /*
         * (non-Javadoc)
         * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#getRule(java.lang.Object)
         */
        public ISchedulingRule getRule(Object object) {
            return null;
        }
        /*
         * (non-Javadoc)
         * @see org.eclipse.ui.progress.IDeferredWorkbenchAdapter#isContainer()
         */
        public boolean isContainer() {
            return true;
        }
        /*
         * (non-Javadoc)
         * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
         */
        public Object[] getChildren(Object o) {
            return children.toArray();
        }
        /*
         * (non-Javadoc)
         * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
         */
        public ImageDescriptor getImageDescriptor(Object object) {
            return null;
        }
        /*
         * (non-Javadoc)
         * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
         */
        public String getLabel(Object o) {
            return o.toString();
        }
        /*
         * (non-Javadoc)
         * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
         */
        public Object getParent(Object o) {
            if (o instanceof TreeObject) {
                return ((TreeObject)o).getParent();
            } else {
                return null;
            }
        }
}
