/*******************************************************************************
 * Copyright (c) 2008,2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.kie.eclipse.navigator.view;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.actions.ActionContext;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.navigator.CommonNavigator;
import org.eclipse.ui.navigator.CommonViewer;
import org.eclipse.ui.navigator.CommonViewerSiteFactory;
import org.eclipse.ui.navigator.ICommonViewerSite;
import org.eclipse.ui.navigator.NavigatorActionService;
import org.eclipse.ui.part.PageBook;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerLifecycleListener;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.ServerEvent;
import org.eclipse.wst.server.core.ServerPort;
import org.jboss.ide.eclipse.as.core.server.internal.JBossServer;
import org.jboss.ide.eclipse.as.core.util.ServerConverter;
import org.kie.eclipse.navigator.KieNavigatorContentRoot;
import org.kie.eclipse.navigator.view.content.IContentNode;
import org.kie.eclipse.navigator.view.utils.ViewUtils;

/**
 * A view of servers, their modules, and status.
 */
public class KieNavigatorView extends CommonNavigator implements IResourceChangeListener {
	private static final String KIE_NAVIGATOR_VIEW_CONTEXT = "org.kie.eclipse.navigator.context";
	protected CommonViewer treeViewer;
	private Control mainPage;
	private Control noServersPage;
	PageBook book;

	protected IServerLifecycleListener serverResourceListener;
	protected IServerListener serverListener;

	// servers that are currently starting
	protected static Set<String> starting = new HashSet<String>(4);
	protected boolean animationActive = false;
	protected boolean stopAnimation = false;

	/**
	 * ServersView constructor comment.
	 */
	public KieNavigatorView() {
		super();
	}
	
	@Override
	protected Object getInitialInput() {
		KieNavigatorContentRoot root = new KieNavigatorContentRoot(this);
		return root;
	}

	@Override
	public void createPartControl(Composite parent) {
		// Add PageBook as parent composite
		FormToolkit toolkit = new FormToolkit(parent.getDisplay());
		book = new PageBook(parent, SWT.NONE);
		super.createPartControl(book);
		// Main page for the Servers tableViewer
		mainPage = getCommonViewer().getControl();
		// Page prompting to define a new server
		noServersPage = createDefaultPage(toolkit);
		book.showPage(mainPage);

		IContextService contextSupport = (IContextService) getSite().getService(IContextService.class);
		contextSupport.activateContext(KIE_NAVIGATOR_VIEW_CONTEXT);
		deferInitialization();
	}
	
	@Override
	protected CommonViewer createCommonViewerObject(Composite aParent) {
		return new CommonViewer(getViewSite().getId(), aParent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL) {
			@Override
			public ISelection getSelection() {
				return super.getSelection();
			}
			
			@Override
			protected void associate(Object element, Item item) {
				if (element instanceof IProject) {
					IProject project = (IProject) element;
					if (!project.exists() || !project.isOpen()) {
						Object data = item.getData("org.kie.navigator.content.node");
						if (data!=null)
							element = data;
					}
				}
				super.associate(element, item);
				if (element instanceof IContentNode) {
					item.setData("org.kie.navigator.content.node", element);
				}
			}

			@Override
			public ViewerComparator getComparator() {
				// The ContentNodes will handle sorting 
				return null;
			}
		};
	}

	/**
	 * Creates a page displayed when there are no servers defined.
	 * 
	 * @param kit
	 * @return Control
	 */
	private Control createDefaultPage(FormToolkit kit) {
		Form form = kit.createForm(book);
		Composite body = form.getBody();
		GridLayout layout = new GridLayout(2, false);
		body.setLayout(layout);

		Link hlink = new Link(body, SWT.NONE);
		hlink.setText("<a>Use the Servers View to create a new server...</a>");
		hlink.setBackground(book.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
		GridData gd = new GridData(SWT.LEFT, SWT.FILL, true, false);
		hlink.setLayoutData(gd);
		hlink.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				// show Servers View
				ViewUtils.showServersView();
			}
		});

		// Create the context menu for the default page
		final CommonViewer commonViewer = this.getCommonViewer();
		if (commonViewer != null) {
			ICommonViewerSite commonViewerSite = CommonViewerSiteFactory.createCommonViewerSite(this.getViewSite());

			if (commonViewerSite != null) {
				// Note: actionService cannot be null
				final NavigatorActionService actionService = new NavigatorActionService(commonViewerSite, commonViewer,
						commonViewer.getNavigatorContentService());

				MenuManager menuManager = new MenuManager("#PopupMenu");
				menuManager.addMenuListener(new IMenuListener() {
					public void menuAboutToShow(IMenuManager mgr) {
						ISelection selection = commonViewer.getSelection();
						actionService.setContext(new ActionContext(selection));
						actionService.fillContextMenu(mgr);
					}
				});
				Menu menu = menuManager.createContextMenu(body);

				// It is necessary to set the menu in two places:
				// 1. The white space in the server view
				// 2. The text and link in the server view. If this menu is not
				// set, if the
				// user right clicks on the text or uses shortcut keys to open
				// the context menu,
				// the context menu will not come up
				body.setMenu(menu);
				hlink.setMenu(menu);
			} else {
				// if (Trace.FINEST) {
				// Trace.trace(Trace.STRING_FINEST,
				// "The commonViewerSite is null");
				// }
			}
		} else {
			// if (Trace.FINEST) {
			// Trace.trace(Trace.STRING_FINEST, "The commonViewer is null");
			// }
		}

		return form;
	}

	/**
	 * Switch between the servers and default/empty page.
	 * 
	 */
	void toggleDefaultPage() {
		if (treeViewer.getTree().getItemCount() < 1) {
			book.showPage(noServersPage);
		} else {
			book.showPage(mainPage);
		}
	}

	private void deferInitialization() {
		// TODO Angel Says: Need to do a final check on this line below. I don't
		// think there is anything else
		// that we need from to port from the old Servers View
		// initializeActions(getCommonViewer());

		Job job = new Job("Initializing Servers View") {
			public IStatus run(IProgressMonitor monitor) {
				IServer[] servers = ServerCore.getServers();
				int size = servers.length;
				for (int i = 0; i < size; i++) {
					((IServer) servers[i]).getModules();
				}
				deferredInitialize();
				return Status.OK_STATUS;
			}
		};

		job.setSystem(true);
		job.setPriority(Job.SHORT);
		job.schedule();
	}
	
	public void refresh() {
	}
	
	protected void deferredInitialize() {
		addListener();

		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				try {
					treeViewer = getCommonViewer();
					getSite().setSelectionProvider(treeViewer);
					
					// init the tooltip
					// ServerToolTip toolTip = new
					// ServerToolTip(tableViewer.getTree());
					// toolTip.setShift(new Point(10, 3));
					// toolTip.setPopupDelay(400); // in ms
					// toolTip.setHideOnMouseDown(true);
					// toolTip.activate();

				} catch (Exception e) {
					// ignore - view has already been closed
				}
			}
		});

		UpdateServerJob job = new UpdateServerJob(ServerCore.getServers());
		job.addJobChangeListener(new JobChangeAdapter() {
			@Override
			public void done(IJobChangeEvent event) {
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						try {
							if (treeViewer.getTree().getItemCount() > 0) {
								Object obj = treeViewer.getTree().getItem(0).getData();
								treeViewer.setSelection(new StructuredSelection(obj));
							} else {
								toggleDefaultPage();
							}
						} catch (Exception e) {
							// if (Trace.WARNING) {
							// Trace.trace(Trace.STRING_WARNING,
							// "Failed to update the server view.", e);
							// }
						}
					}
				});
			}
		});
		job.schedule();
	}
	
	protected void refreshServerContent(final IServer server) {
		// if (Trace.FINEST) {
		// Trace.trace(Trace.STRING_FINEST, "Refreshing Content for server=" +
		// server);
		// }
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (!treeViewer.getTree().isDisposed()) {
					Object[] expanded = treeViewer.getExpandedElements();
					treeViewer.refresh(server, true);
					treeViewer.setExpandedElements(expanded);
				}
				
//				if (server.getServerState() == IServer.STATE_STARTED) 
				{
					System.out.println("Server State="+server.getServerState());
					System.out.println("Server Type ID="+server.getServerType().getId());
					System.out.println("Modules:");
					for (IModule m : server.getModules()) {
						System.out.println("  "+m.getName());
					}
					if (ServerConverter.getJBossServer(server) instanceof JBossServer) {
						JBossServer jbs = (JBossServer) ServerConverter.getJBossServer(server);
						System.out.println("HTTP="+jbs.getJBossWebPort());
					}
					for (ServerPort p : server.getServerPorts(null)) {
						System.out.println(p.getProtocol()+"="+p.getPort());
					}
				}
				
			}
		});
	}

	protected void refreshServerState(final IServer server) {
		// if (Trace.FINEST) {
		// Trace.trace(Trace.STRING_FINEST, "Refreshing UI for server=" +
		// server);
		// }
		// ServerDecoratorsHandler.refresh(tableViewer);
	}

	@Override
	public void resourceChanged(final IResourceChangeEvent event) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				IResourceDelta delta = event.getDelta();
				if (delta!=null) {
					try {
						delta.accept(new IResourceDeltaVisitor() {

							@Override
							public boolean visit(IResourceDelta delta) throws CoreException {
								IResource resource = delta.getResource();
								if (resource instanceof IProject) {
									switch (delta.getKind()) {
									case IResourceDelta.ADDED:
									case IResourceDelta.REMOVED:
										KieNavigatorContentRoot root = (KieNavigatorContentRoot) treeViewer.getInput();
										root.getChildren();
										treeViewer.refresh(root, true);
										return false;
									}
								}
								return true;
							}
							
						});
					}
					catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
	}
	
	protected void addListener() {
		// To enable the UI updating of servers and its children
		serverResourceListener = new IServerLifecycleListener() {
			public void serverAdded(IServer server) {
				addServer(server);
				server.addServerListener(serverListener);
			}

			public void serverChanged(IServer server) {
				refreshServerContent(server);
			}

			public void serverRemoved(IServer server) {
				removeServer(server);
				server.removeServerListener(serverListener);
			}
		};
		ServerCore.addServerLifecycleListener(serverResourceListener);

		serverListener = new IServerListener() {
			public void serverChanged(ServerEvent event) {
				if (event == null)
					return;

				int eventKind = event.getKind();
				IServer server = event.getServer();
				if ((eventKind & ServerEvent.SERVER_CHANGE) != 0) {
					// server change event
					if ((eventKind & ServerEvent.STATE_CHANGE) != 0) {
						int state = event.getState();
						String id = server.getId();
						if (state == IServer.STATE_STARTING || state == IServer.STATE_STOPPING) {
							boolean startThread = false;
							synchronized (starting) {
								if (!starting.contains(id)) {
									if (starting.isEmpty())
										startThread = true;
									starting.add(id);
								}
							}
							if (startThread)
								startThread();
						} else {
							boolean stopThread = false;
							synchronized (starting) {
								if (starting.contains(id)) {
									starting.remove(id);
									if (starting.isEmpty())
										stopThread = true;
								}
							}
							if (stopThread)
								stopThread();
						}
						refreshServerState(server);
						refreshServerContent(server);
					} else if ((eventKind & ServerEvent.PUBLISH_STATE_CHANGE) != 0
							|| (eventKind & ServerEvent.STATUS_CHANGE) != 0) {
						refreshServerState(server);
					}
					
				} else if ((eventKind & ServerEvent.MODULE_CHANGE) != 0) {
					// module change event
					if ((eventKind & ServerEvent.STATE_CHANGE) != 0
							|| (eventKind & ServerEvent.PUBLISH_STATE_CHANGE) != 0
							|| (eventKind & ServerEvent.STATUS_CHANGE) != 0) {
						refreshServerContent(server);
					}
				}
			}
		};

		// add listeners to servers
		IServer[] servers = ServerCore.getServers();
		if (servers != null) {
			int size = servers.length;
			for (int i = 0; i < size; i++) {
				servers[i].addServerListener(serverListener);
			}
		}

		// add Resource listener to check for Project changes
        ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	protected void addServer(final IServer server) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				KieNavigatorContentRoot root = new KieNavigatorContentRoot(KieNavigatorView.this);
				treeViewer.setInput(root);
				toggleDefaultPage();
			}
		});
	}

	protected void removeServer(final IServer server) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				KieNavigatorContentRoot root = new KieNavigatorContentRoot(KieNavigatorView.this);
				treeViewer.setInput(root);
				toggleDefaultPage();
			}
		});
	}

	@Override
	public void dispose() {
		ServerCore.removeServerLifecycleListener(serverResourceListener);

		// remove listeners from servers
		IServer[] servers = ServerCore.getServers();
		if (servers != null) {
			int size = servers.length;
			for (int i = 0; i < size; i++) {
				servers[i].removeServerListener(serverListener);
			}
		}

		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		
		super.dispose();
	}

	/**
	 * Start the animation thread
	 */
	protected void startThread() {
		if (animationActive)
			return;

		stopAnimation = false;

		final Display display = treeViewer == null ? Display.getDefault() : treeViewer.getControl().getDisplay();
		final int SLEEP = 200;
		final Runnable[] animator = new Runnable[1];
		animator[0] = new Runnable() {
			public void run() {
				if (!stopAnimation) {
					try {
						int size = 0;
						String[] servers;
						synchronized (starting) {
							size = starting.size();
							servers = new String[size];
							starting.toArray(servers);

						}

						for (int i = 0; i < size; i++) {
							IServer server = ServerCore.findServer(servers[i]);
							if (server != null) {
								// ServerDecorator.animate();
								treeViewer.update(server, new String[] { "ICON" });
							}
						}
					} catch (Exception e) {
						// if (Trace.FINEST) {
						// Trace.trace(Trace.STRING_FINEST,
						// "Error in Servers view animation", e);
						// }
					}
					display.timerExec(SLEEP, animator[0]);
				}
			}
		};
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				display.timerExec(SLEEP, animator[0]);
			}
		});
	}

	protected void stopThread() {
		stopAnimation = true;
	}
}
