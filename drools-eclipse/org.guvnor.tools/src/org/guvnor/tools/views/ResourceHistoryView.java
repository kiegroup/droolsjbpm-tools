package org.guvnor.tools.views;


import java.net.URL;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.webdav.IResponse;
import org.guvnor.tools.Activator;
import org.guvnor.tools.Messages;
import org.guvnor.tools.utils.GuvnorMetadataUtils;
import org.guvnor.tools.utils.PlatformUtils;
import org.guvnor.tools.utils.ResourceHistorySorter;
import org.guvnor.tools.utils.webdav.IWebDavClient;
import org.guvnor.tools.utils.webdav.WebDavClientFactory;
import org.guvnor.tools.utils.webdav.WebDavException;
import org.guvnor.tools.utils.webdav.WebDavServerCache;
import org.guvnor.tools.views.model.ResourceHistoryEntry;

/**
 * View showing the versions of a given resource.
 * 
 * @author jgraham
 *
 */
public class ResourceHistoryView extends ViewPart {
	
	private Label repositoryLabel;
	private Label resourceLabel;
	
	private TableViewer viewer;
	
	private Action showVersionAction;
	
	/**
	 * The constructor.
	 */
	public ResourceHistoryView() { }
	
	public void createPartControl(Composite parent) {

		Composite composite = PlatformUtils.createComposite(parent, 1);
		
		repositoryLabel = new Label(composite, SWT.NONE);
		repositoryLabel.setText(Messages.getString("history.rep.label")); //$NON-NLS-1$
		repositoryLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		resourceLabel = new Label(composite, SWT.NONE);
		resourceLabel.setText(Messages.getString("history.resource.label")); //$NON-NLS-1$
		resourceLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		viewer = new TableViewer(PlatformUtils.createResourceHistoryTable(composite));
		viewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		
		viewer.setContentProvider(new ResourceHistoryContentProvider(new ResourceHistoryEntry[0]));
		viewer.setLabelProvider(new ResourceHistoryLabelProvider());
		viewer.setSorter(new ResourceHistorySorter());
		viewer.setInput(getViewSite());
		
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu"); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				ResourceHistoryView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}
	
	private void fillContextMenu(IMenuManager manager) {
		manager.add(showVersionAction);
		manager.add(new Separator());
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}
	
	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				showResourceVersionContents();
			}
		});
	}
	
	private void makeActions() {
		showVersionAction = new Action() {
			public void run() {
				showResourceVersionContents();
			}
		};
		showVersionAction.setText(Messages.getString("action.open")); //$NON-NLS-1$
		showVersionAction.setToolTipText(Messages.getString("action.open.desc")); //$NON-NLS-1$
		showVersionAction.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages().
			getImageDescriptor(ISharedImages.IMG_OBJ_FILE));
	}
	
	private void showResourceVersionContents() {
		String repository = repositoryLabel.getToolTipText();
		String fullPath = resourceLabel.getToolTipText();
		ISelection selection = viewer.getSelection();
		Object obj = ((IStructuredSelection)selection).getFirstElement();
		if (obj instanceof ResourceHistoryEntry) {
			ResourceHistoryEntry theEntry = (ResourceHistoryEntry)obj;
			try {
				IWebDavClient client = WebDavServerCache.getWebDavClient(repository);
				if (client == null) {
					client = WebDavClientFactory.createClient(new URL(repository));
					WebDavServerCache.cacheWebDavClient(repository, client);
				}
				String contents = null;
				try {
					contents = client.getResourceVersionContents(fullPath, theEntry.getRevision());
				} catch (WebDavException wde) {
					if (wde.getErrorCode() != IResponse.SC_UNAUTHORIZED) {
						// If not an authentication failure, we don't know what to do with it
						throw wde;
					}
					boolean retry = PlatformUtils.getInstance().
										authenticateForServer(repository, client); 
					if (retry) {
						contents = client.getResourceVersionContents(fullPath, theEntry.getRevision());
					}
				}
				if (contents != null) {
					String editorTitle = null;
					int pos = fullPath.lastIndexOf("/"); //$NON-NLS-1$
					if (pos != -1
					   && pos + 1 < fullPath.length()) {
						editorTitle = fullPath.substring(pos + 1);
					} else {
						editorTitle = fullPath;
					}
					PlatformUtils.openEditor(contents, editorTitle + ", " + theEntry.getRevision()); //$NON-NLS-1$
				}
			} catch (Exception e) {
				Activator.getDefault().displayError(IStatus.ERROR, e.getMessage(), e);
			}
		}
	}
	
	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
	
	public void setEntries(String repository, String fullPath, Properties entryProps) {
		repositoryLabel.setText(Messages.getString("history.rep.label") + repository); //$NON-NLS-1$
		repositoryLabel.setToolTipText(repository);
		resourceLabel.setText(Messages.getString("history.resource.label") + fullPath.substring(repository.length())); //$NON-NLS-1$
		resourceLabel.setToolTipText(fullPath);
		ResourceHistoryEntry[] entries = GuvnorMetadataUtils.parseHistoryProperties(entryProps);
		viewer.setContentProvider(new ResourceHistoryContentProvider(entries));
		viewer.setInput(getViewSite());
	}
}