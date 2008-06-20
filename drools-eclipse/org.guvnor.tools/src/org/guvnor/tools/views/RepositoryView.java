package org.guvnor.tools.views;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.rmi.server.UID;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.FileTransfer;
import org.eclipse.swt.dnd.Transfer;
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
import org.guvnor.tools.GuvnorLocationManager.IRepositorySetListener;
import org.guvnor.tools.utils.PlatformUtils;
import org.guvnor.tools.utils.webdav.WebDavClient;
import org.guvnor.tools.utils.webdav.WebDavServerCache;
import org.guvnor.tools.views.model.TreeObject;
import org.guvnor.tools.views.model.TreeParent;
import org.guvnor.tools.wizards.NewRepLocationWizard;

public class RepositoryView extends ViewPart {
	
	private TreeViewer viewer;
	private DrillDownAdapter drillDownAdapter;
	private Action action1;
	private Action action2;
	private Action doubleClickAction;
	
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

	private void addDragDropSupport() {
		// TODO: Support drag and drop of multiple files, directories, etc.
		Transfer[] transfers = new Transfer[] { FileTransfer.getInstance()};
		viewer.addDragSupport(DND.DROP_COPY, transfers, new DragSourceListener() {
			private TreeObject target;
			
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
					Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
				}
			}
			
			public void dragStart(DragSourceEvent event) {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				if (obj instanceof TreeObject
				   && ((TreeObject)obj).getNodeType() == TreeObject.Type.RESOURCE) {
					target = (TreeObject)obj;
					event.doit = true;
				} else {
					event.doit = false;
				}
			}
		});
	}
	
	private List<String> prepareFileTransfer(TreeObject node) throws Exception {
		List<String> res = new ArrayList<String>();
		String contents = getResourceContents(node);
		IPath path = new Path(Activator.getDefault().getStateLocation().toOSString() + 
								File.separator + new UID().toString());
		if (!path.toFile().mkdir()) {
			throw new Exception("Could not create directory " + path.toOSString());
		}
		path.toFile().deleteOnExit();
		File transfer = new File(path + File.separator + node.getName());
		transfer.deleteOnExit();
		FileOutputStream fos = new FileOutputStream(transfer);
		PrintWriter writer = new PrintWriter(fos);
		writer.write(contents);
		writer.flush();
		writer.close();
		res.add(transfer.getAbsolutePath());
		
		IPath metaPath = new Path(path.toOSString() + File.separator + ".guvnorinfo");
		if (!metaPath.toFile().mkdir()) {
			throw new Exception("Could not create directory " + metaPath.toOSString());
		}
		metaPath.toFile().deleteOnExit();
		File metaFile = new File(metaPath.toOSString() + File.separator + node.getName());
		metaFile.deleteOnExit();
		fos = new FileOutputStream(metaFile);
		Properties props = new Properties();
		props.put("repository", node.getGuvnorRepository().getLocation());
		props.put("fullpath", node.getFullPath());
		props.put("filename", node.getName());
		props.put("lastmodified", node.getResourceProps().getLastModifiedDate());
		props.store(fos, null);
		fos.flush();
		fos.close();
		res.add(metaFile.getAbsolutePath());
		
		return res;
	}
	
	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
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
		manager.add(action1);
		manager.add(new Separator());
		manager.add(action2);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(action1);
		manager.add(action2);
		manager.add(new Separator());
		drillDownAdapter.addNavigationActions(manager);
	}

	private void makeActions() {
		action1 = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection)selection).getFirstElement();
				if (obj instanceof TreeParent) {
					GuvnorRepository rep = ((TreeParent)obj).getGuvnorRepository();
					if (MessageDialog.openConfirm(RepositoryView.this.getSite().getShell(), 
							                     "Remove repository location", "Confirm removing " + 
							                     rep.getLocation())) {
						Activator.getLocationManager().removeRepository(rep.getLocation());
					}
				}
			}
		};
		action1.setText("Delete");
		action1.setToolTipText("Delete Guvnor repository location");
		action1.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_TOOL_DELETE));
		
		action2 = new Action() {
			public void run() {
				NewRepLocationWizard wiz = new NewRepLocationWizard();
				wiz.init(Activator.getDefault().getWorkbench(), null);
				WizardDialog dialog = 
					new WizardDialog(RepositoryView.this.getSite().getShell(), wiz);
			    dialog.create();
			    dialog.open();
			}
		};
		action2.setText("Add");
		action2.setToolTipText("Add a Guvnor respository location");
		action2.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
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
				Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
			}
		}
	}
	
	private String getResourceContents(TreeObject node) throws Exception {
		GuvnorRepository rep = node.getGuvnorRepository();
		WebDavClient webdav = WebDavServerCache.getWebDavClient(rep.getLocation());
		if (webdav == null) {
			webdav = new WebDavClient(new URL(rep.getLocation()));
			WebDavServerCache.cacheWebDavClient(rep.getLocation(), webdav);
		}
		return webdav.getResourceContents(node.getFullPath());	
	}
	
//	private void putResource(TreeParent node, String name, InputStream is) throws Exception {
//		GuvnorRepository rep = node.getGuvnorRepository();
//		WebDavClient webdav = WebDavServerCache.getWebDavClient(rep.getLocation());
//		if (webdav == null) {
//			webdav = new WebDavClient(new URL(rep.getLocation()), 
//					                 rep.getUsername(), rep.getPassword());
//			WebDavServerCache.cacheWebDavClient(rep.getLocation(), webdav);
//		}
//		webdav.putResource(node.getFullPath(), name, is);
//	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}