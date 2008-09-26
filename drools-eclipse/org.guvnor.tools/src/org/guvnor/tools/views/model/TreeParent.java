package org.guvnor.tools.views.model;

import java.net.URL;
import java.util.ArrayList;
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
 * @author jgraham
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
			if (node.getNodeType() == Type.REPOSITORY
			   || node.getNodeType() == Type.PACKAGE) {
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
							o = new TreeParent(s, Type.PACKAGE);	
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
					Activator.getDefault().displayError(IStatus.ERROR, e.getMessage(), e);
				}
			} catch (Exception e) {
				Activator.getDefault().displayError(IStatus.ERROR, e.getMessage(), e);
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
