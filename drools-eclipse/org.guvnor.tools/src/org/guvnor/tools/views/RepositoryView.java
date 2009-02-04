package org.guvnor.tools.views;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.rmi.server.UID;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.DrillDownAdapter;
import org.eclipse.ui.part.ViewPart;
import org.guvnor.tools.Activator;
import org.guvnor.tools.GuvnorRepository;
import org.guvnor.tools.Messages;
import org.guvnor.tools.GuvnorLocationManager.IRepositorySetListener;
import org.guvnor.tools.utils.GuvnorMetadataProps;
import org.guvnor.tools.utils.GuvnorMetadataUtils;
import org.guvnor.tools.utils.PlatformUtils;
import org.guvnor.tools.utils.webdav.IWebDavClient;
import org.guvnor.tools.utils.webdav.ResourceProperties;
import org.guvnor.tools.utils.webdav.WebDavClientFactory;
import org.guvnor.tools.utils.webdav.WebDavServerCache;
import org.guvnor.tools.views.model.TreeObject;
import org.guvnor.tools.views.model.TreeParent;
import org.guvnor.tools.wizards.NewRepLocationWizard;

/**
 * A view showing Guvnor structure in a tree.
 * @author jgraham
 */
public class RepositoryView extends ViewPart {
	
	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action deleteRepositoryLocAction;
	private Action addRepositoryLocAction;
	private Action doubleClickAction;
	private Action refreshAction;
	
	class NameSorter extends ViewerSorter {
	}

	public RepositoryView() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl(Composite parent) {
		viewer = new TreeViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		drillDownAdapter = new DrillDownAdapter(viewer);
		viewer.setContentProvider(new RepositoryContentProvider());
		viewer.setLabelProvider(new RepositoryLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(viewer);
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
		
		Activator.getLocationManager().addRepositorySetListener(new IRepositorySetListener() {
			public void repositorySetChanged(int type, List<GuvnorRepository> repList) {
				// TODO: Just creating an entirely new content provider.
				//       Someday might update this to have incremental changes
				//       to existing content provider.
				viewer.setContentProvider(new RepositoryContentProvider());
			}
		});
		
		super.getSite().setSelectionProvider(viewer);
		
		addDragDropSupport();
	}
	
	public void refresh() {
		viewer.refresh();
	}
	
	private void addDragDropSupport() {
		// TODO: Support drag and drop of directories
		Transfer[] transfers = new Transfer[] { FileTransfer.getInstance()};
		viewer.addDragSupport(DND.DROP_COPY, transfers, new DragSourceListener() {
			private TreeObject[] target;
			
			public void dragFinished(DragSourceEvent event) {
				target = null;
				event.doit = true;
			}
			public void dragSetData(DragSourceEvent event) {
				if (target == null) {
					return;
				}
				try {
					List<String> files = prepareFileTransfer(target);
					String[] res = new String[files.size()];
					files.toArray(res);
					event.data = res;
				} catch (Exception e) {
					// Note: we could catch a 401 (not authorized) WebDav error and retry,
					// like we do when listing directories. But since we have to first list
					// directories to get to files, and the act of listing directories authenticates
					// for the server, currently we do not have a situation requiring authentication
					// for specific files. This might be different in the future if the Guvnor security
					// model changes, or users can directly connect to specific files.
					Activator.getDefault().displayError(IStatus.ERROR, e.getMessage(), e, true);
				}
			}
			
			public void dragStart(DragSourceEvent event) {
				List<TreeObject> transferNodes = new ArrayList<TreeObject>();
				ISelection selection = viewer.getSelection();
				Object[] objs = ((IStructuredSelection)selection).toArray();
				for (int i = 0; i < objs.length; i++) {
					if (objs[i] instanceof TreeObject
					    && ((TreeObject)objs[i]).getNodeType() == TreeObject.Type.RESOURCE) {
						transferNodes.add((TreeObject)objs[i]);
					}
				}
				if (transferNodes.size() > 0) {
					event.doit = true;
					target = new TreeObject[transferNodes.size()];
					transferNodes.toArray(target);
				} else {
					event.doit = false;
				}
			}
		});
		
		viewer.addDropSupport(DND.DROP_COPY | DND.DROP_MOVE, transfers, new ViewerDropAdapter(viewer) {
			
			private TreeParent targetNode;
			
			@Override
			public boolean performDrop(Object data) {
				if (targetNode == null) {
					return false;
				}
				String[] items = (String[])data;
				String[] errors = processDrop(targetNode, items);
				if (errors.length != 0) {
					StringBuilder msg = new StringBuilder();
					for (int i = 0; i < errors.length; i++) {
						msg.append(errors[i]);
						msg.append("\r\n"); //$NON-NLS-1$
					}
					Activator.getDefault().
						displayError(IStatus.ERROR, msg.toString(), new Exception(), true);
				}
				if (items.length != errors.length) {
					// At least one item did not have an error,
					// so refresh the views
					PlatformUtils.updateDecoration();
					PlatformUtils.refreshRepositoryView();
				}
				return items.length != errors.length;
			}

			@Override
			public boolean validateDrop(Object target, 
					                   int operation,
					                   TransferData transferType) {
				// The drop target needs to be a directory
				if (target == null
					|| !(target instanceof TreeParent)) {
					targetNode = null;
					return false;
				}
				targetNode = (TreeParent)target;
				return true;
			}
			
		});
	}
	
	private String[] processDrop(TreeParent target, String[] items) {
		List<String> errors = new ArrayList<String>();
		
		for (int i = 0; i < items.length; i++) {
			IFile sourceFile = PlatformUtils.getResourceFromFSPath(items[i]);
			if (sourceFile != null) {
				try {
					GuvnorMetadataProps md = GuvnorMetadataUtils.getGuvnorMetadata(sourceFile);
					if (md == null) {
						// The file is not already associated with Guvnor, so just add it
						boolean res = GuvnorMetadataUtils.
										addResourceToGuvnor(target.getGuvnorRepository().getLocation(), 
										                   target.getFullPath(), sourceFile);
						if (!res) {
							errors.add(MessageFormat.format(Messages.getString("add.failure"), //$NON-NLS-1$ 
								                            new Object[] { items[i],
								                                           target.getFullPath() }));
						}
					} else {
						// Need to check if the drop location is the same as the Guvnor
						// associated location. If so, then perform commit. 
						// If not, disallow the drop.
						String itemPath = target.getFullPath() + sourceFile.getName();
						if (itemPath.equals(md.getFullpath())) {
							// If there are pending changes
							if (!GuvnorMetadataUtils.isGuvnorResourceCurrent(sourceFile)) {
								GuvnorMetadataUtils.commitFileChanges(sourceFile);
							}
						} else {
							errors.add(MessageFormat.format(Messages.getString("already.guvnor.as"), //$NON-NLS-1$ 
									                       new Object[] { items[i],
								                                          md.getFullpath() }));
						}
					}
				} catch (Exception e) {
					Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
				}
			} else {
				Activator.getDefault().
					writeLog(IStatus.WARNING, 
							"Could not resolve: " + items[i],  //$NON-NLS-1$
							new Exception());
			}
		}
		String[] res = new String[errors.size()];
		errors.toArray(res);
		return res;
	}
	
	private List<String> prepareFileTransfer(TreeObject[] nodes) throws Exception {
		List<String> res = new ArrayList<String>();
		for (int i = 0; i < nodes.length; i++) {
			String contents = getResourceContents(nodes[i]);
			IPath path = new Path(Activator.getDefault().getStateLocation().toOSString() + 
								  File.separator + new UID().toString());
			if (!path.toFile().mkdir()) {
				throw new Exception("Could not create directory " + path.toOSString()); //$NON-NLS-1$
			}
			path.toFile().deleteOnExit();
			File transfer = new File(path + File.separator + nodes[i].getName());
			transfer.deleteOnExit();
			FileOutputStream fos = new FileOutputStream(transfer);
			PrintWriter writer = new PrintWriter(fos);
			writer.write(contents);
			writer.flush();
			writer.close();
			res.add(transfer.getAbsolutePath());
		
			IPath metaPath = GuvnorMetadataUtils.
								createGuvnorMetadataLocation(path.toOSString());
			metaPath.toFile().deleteOnExit();
			File metaFile = GuvnorMetadataUtils.
								getGuvnorMetadataFile(metaPath.toOSString(), nodes[i].getName());
			metaFile.deleteOnExit();
			GuvnorMetadataUtils.writeGuvnorMetadataProps(metaFile, getGuvnorMetadataProps(nodes[i]));
			res.add(metaFile.getAbsolutePath());
		}
		return res;
	}
	
	private GuvnorMetadataProps getGuvnorMetadataProps(TreeObject node) throws Exception {
		GuvnorRepository rep = node.getGuvnorRepository();
		IWebDavClient webdav = WebDavServerCache.getWebDavClient(rep.getLocation());
		if (webdav == null) {
			webdav = WebDavClientFactory.createClient(new URL(rep.getLocation()));
			WebDavServerCache.cacheWebDavClient(rep.getLocation(), webdav);
		}
		ResourceProperties props  = webdav.queryProperties(node.getFullPath());
		return new GuvnorMetadataProps(node.getName(), 
	                                  node.getGuvnorRepository().getLocation(),
                                      node.getFullPath(), 
                                      props.getLastModifiedDate(),
                                      props.getRevision());
	}
	
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				RepositoryView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(deleteRepositoryLocAction);
		manager.add(new Separator());
		manager.add(addRepositoryLocAction);
		manager.add(new Separator());
		manager.add(refreshAction);
	}
	
	private boolean shouldAddDeleteAction() {
		ISelection selection = viewer.getSelection();
		if (selection == null) {
			return false;
		}
		Object obj = ((IStructuredSelection)selection).getFirstElement();
		if (obj instanceof TreeParent) {
			return ((TreeParent)obj).getNodeType() == TreeObject.Type.REPOSITORY;
		} else {
			return false;
		}
	}
	
	private void fillContextMenu(IMenuManager manager) {
		if (shouldAddDeleteAction()) {
			manager.add(deleteRepositoryLocAction);
		}
		manager.add(refreshAction);
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(deleteRepositoryLocAction);
		manager.add(addRepositoryLocAction);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {
		deleteRepositoryLocAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				if (obj instanceof TreeParent) {
					GuvnorRepository rep = ((TreeParent)obj).getGuvnorRepository();
					if (MessageDialog.openConfirm(RepositoryView.this.getSite().getShell(),
							                     Messages.getString("remove.rep.dialog.caption"), //$NON-NLS-1$
							                     MessageFormat.format(Messages.getString("remove.rep.dialog.msg"), //$NON-NLS-1$
							                                         new Object[] { rep.getLocation() }))) {
						Activator.getLocationManager().removeRepository(rep.getLocation());
					}
				}
			}
		};
		deleteRepositoryLocAction.setText(Messages.getString("action.delete.rep")); //$NON-NLS-1$
		deleteRepositoryLocAction.setToolTipText(Messages.getString("action.delete.rep.desc")); //$NON-NLS-1$
		deleteRepositoryLocAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		
		addRepositoryLocAction = new Action() {
			public void run() {
				NewRepLocationWizard wiz = new NewRepLocationWizard();
				wiz.init(Activator.getDefault().getWorkbench(), null);
				WizardDialog dialog = 
					new WizardDialog(RepositoryView.this.getSite().getShell(), wiz);
			    dialog.create();
			    dialog.open();
			}
		};
		addRepositoryLocAction.setText(Messages.getString("action.add.rep")); //$NON-NLS-1$
		addRepositoryLocAction.setToolTipText(Messages.getString("action.add.rep.desc")); //$NON-NLS-1$
		addRepositoryLocAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_TOOL_NEW_WIZARD));
		
		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				if (obj instanceof TreeObject) {
					doubleClick((TreeObject)obj);
				}
			}
		};
		
		refreshAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection strucSel = (IStructuredSelection)selection;
					if (!strucSel.isEmpty()) {
						viewer.refresh(strucSel.getFirstElement());
					}
				}
			}
		};
		refreshAction.setText(Messages.getString("action.refresh.rep")); //$NON-NLS-1$
		refreshAction.setToolTipText(Messages.getString("action.refresh.rep.desc")); //$NON-NLS-1$
		refreshAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
				getImageDescriptor(ISharedImages.IMG_TOOL_REDO));
	}
	
	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}
	
	private void doubleClick(TreeObject node) {
		if (node.getNodeType() == TreeObject.Type.PACKAGE
			|| node.getNodeType() == TreeObject.Type.REPOSITORY) {
			if (viewer.getExpandedState(node)) {
				viewer.collapseToLevel(node, 1);
			} else {
				viewer.expandToLevel(node, 1);
			}
		}
		if (node.getNodeType() == TreeObject.Type.RESOURCE) {
			
			try {
				String contents = getResourceContents(node);
				PlatformUtils.openEditor(contents, node.getName());
			} catch (Exception e) {
				// Note: we could catch a 401 (not authorized) WebDav error and retry,
				// like we do when listing directories. But since we have to first list
				// directories to get to files, and the act of listing directories authenticates
				// for the server, currently we do not have a situation requiring authentication
				// for specific files. This might be different in the future if the Guvnor security
				// model changes, or users can directly connect to specific files.
				Activator.getDefault().displayError(IStatus.ERROR, e.getMessage(), e, true);
			}
		}
	}
	
	private String getResourceContents(TreeObject node) throws Exception {
		GuvnorRepository rep = node.getGuvnorRepository();
		IWebDavClient webdav = WebDavServerCache.getWebDavClient(rep.getLocation());
		if (webdav == null) {
			webdav = WebDavClientFactory.createClient(new URL(rep.getLocation()));
			WebDavServerCache.cacheWebDavClient(rep.getLocation(), webdav);
		}
		String res = webdav.getResourceContents(node.getFullPath());
		return res;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}