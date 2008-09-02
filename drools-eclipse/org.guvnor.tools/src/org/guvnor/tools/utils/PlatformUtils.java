package org.guvnor.tools.utils;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IDecoratorManager;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.guvnor.tools.Activator;
import org.guvnor.tools.GuvnorDecorator;
import org.guvnor.tools.Messages;
import org.guvnor.tools.utils.webdav.IWebDavClient;
import org.guvnor.tools.utils.webdav.WebDavClientFactory;
import org.guvnor.tools.utils.webdav.WebDavServerCache;
import org.guvnor.tools.utils.webdav.WebDavSessionAuthenticator;
import org.guvnor.tools.views.IGuvnorConstants;
import org.guvnor.tools.views.RepositoryView;
import org.guvnor.tools.views.ResourceHistoryView;

/**
 * A set of utilities for interacting with the Eclipse platform.
 * 
 * @author jgraham
 */
public class PlatformUtils {
	
	private static PlatformUtils instance;
	
	/**
	 * For convenience, we keep one instance of PlatformUtils around
	 * @return the PlatformUtils instance
	 */
	public static PlatformUtils getInstance() {
		if (instance == null) {
			instance = new PlatformUtils();
		}
		return instance;
	}
	
	public static Composite createComposite(Composite parent, int numColumns) {
		Composite composite = new Composite(parent, SWT.NULL);
		
		GridLayout layout = new GridLayout();
		layout.numColumns = numColumns;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return composite;
	}
	
	/**
	 * Causes the platform to update Guvnor decoration notions.
	 */
	public static void updateDecoration() {
		final IWorkbench workbench = Activator.getDefault().getWorkbench();
		workbench.getDisplay().syncExec(new Runnable() {
			public void run() {
				IDecoratorManager manager = workbench.getDecoratorManager();
				manager.update(GuvnorDecorator.DECORATOR_ID);
			}
		});
	}
	
	/**
	 * Causes the Repository view to refresh, if it is open.
	 */
	public static void refreshRepositoryView() {
		IWorkbenchWindow activeWindow = Activator.getDefault().
											getWorkbench().getActiveWorkbenchWindow();
		// If there is no active workbench window, then there can be no Repository view
		if (activeWindow == null) {
			return;
		}
		// If there is no active workbench page, then there can be no Repository view
		IWorkbenchPage page = activeWindow.getActivePage();
		if (page == null) {
			return;
		}
		RepositoryView view = (RepositoryView)page.findView(IGuvnorConstants.REPVIEW_ID);
		if (view != null) {
			view.refresh();
		}
	}
	
	/**
	 * Tries to find the Resource History view, attempting to open it if necessary.
	 */
	public static ResourceHistoryView getResourceHistoryView() throws Exception {
		IWorkbenchWindow activeWindow = Activator.getDefault().
											getWorkbench().getActiveWorkbenchWindow();
		// If there is no active workbench window, then there can be no Repository History view
		if (activeWindow == null) {
			return null;
		}
		// If there is no active workbench page, then there can be no Repository History view
		IWorkbenchPage page = activeWindow.getActivePage();
		if (page == null) {
			return null;
		}
		return (ResourceHistoryView)page.showView(IGuvnorConstants.RESHISTORYVIEW_ID);
	}
	
	/**
	 * Opens a read-only, in-memory editor.
	 * @param contents The contents for the editor
	 * @param name The name of the file. Will be used to determine
	 *        eclipse editor association, defaulting to text editor
	 *        if no association is found
	 */
	public static void openEditor(String contents, String name) {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IStorage storage = new StringStorage(contents, name);
		IStorageEditorInput input = new StringInput(storage);
		IWorkbenchPage page = window.getActivePage();
		IEditorDescriptor desc = PlatformUI.getWorkbench().
        							getEditorRegistry().getDefaultEditor(name);
		// If there is no editor associated with the given file name, we'll just
		// use the eclipse text editor as a default
		String editorId = desc != null?desc.getId():"org.eclipse.ui.DefaultTextEditor"; //$NON-NLS-1$
		try {
		if (page != null) {
			page.openEditor(input, editorId);
		}
		} catch (Exception e) {
			Activator.getDefault().displayError(IStatus.ERROR, e.getMessage(), e);
		}
	}
	
	public static Table createResourceHistoryTable(Composite parent) {
		int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | 
		            SWT.FULL_SELECTION | SWT.HIDE_SELECTION;

		Table table = new Table(parent, style);

		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.grabExcessVerticalSpace = true;
		gridData.horizontalSpan = 3;
		table.setLayoutData(gridData);		
		
		table.setLinesVisible(true);
		table.setHeaderVisible(true);

		TableColumn column = new TableColumn(table, SWT.LEFT, 0);
		column.setResizable(true);
		column.setText(Messages.getString("history.revision")); //$NON-NLS-1$
		column.setWidth(100);

		column = new TableColumn(table, SWT.LEFT, 1);
		column.setResizable(true);
		column.setText(Messages.getString("history.date")); //$NON-NLS-1$
		column.setWidth(175);

		column = new TableColumn(table, SWT.LEFT, 2);
		column.setResizable(true);
		column.setText(Messages.getString("history.author")); //$NON-NLS-1$
		column.setWidth(200);

		column = new TableColumn(table, SWT.CENTER, 3);
		column.setResizable(true);
		column.setText(Messages.getString("history.comment")); //$NON-NLS-1$
		column.setWidth(350);

		return table;
	}
	
	public boolean authenticateForServer(String server, IWebDavClient client) throws Exception {
		AuthPromptResults res = promptForAuthentication(server);
		if (res != null) {
			if (res.wasSaved()) {
				Platform.addAuthorizationInfo(new URL(server), "", "basic", res.getAuthInfo()); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				WebDavSessionAuthenticator authen = new WebDavSessionAuthenticator();
				authen.addAuthenticationInfo(new URL(server), "", "basic", res.getAuthInfo()); //$NON-NLS-1$ //$NON-NLS-2$
				client.setSessionAuthenticator(authen);
			}
			return true;
		} else {
			return false;
		}
	}
	
	public void updateAuthentication(String server, String username, 
			                        String password, boolean saveInfo) throws Exception {
		Map<String, String> info = new HashMap<String, String>();
		info.put("username", username); //$NON-NLS-1$
		info.put("password", password); //$NON-NLS-1$
		URL serverUrl = new URL(server);
		IWebDavClient client = WebDavServerCache.getWebDavClient(server);
		if (client == null) {
			client = WebDavClientFactory.createClient(serverUrl);
			WebDavServerCache.cacheWebDavClient(server, client);
		}
		if (saveInfo) {
			Platform.flushAuthorizationInfo(serverUrl, "", "basic"); //$NON-NLS-1$ //$NON-NLS-2$
			Platform.addAuthorizationInfo(serverUrl, "", "basic", info); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			WebDavSessionAuthenticator authen = new WebDavSessionAuthenticator();
			authen.addAuthenticationInfo(new URL(server), "", "basic", info); //$NON-NLS-1$ //$NON-NLS-2$
			client.setSessionAuthenticator(authen);
		}
	}
	
	/**
	 * Convenience method for reporting log in failure
	 */
	public static void reportAuthenticationFailure() {
		Display display = PlatformUI.getWorkbench().getDisplay();
		display.asyncExec(new Runnable() {
			public void run() {
				Display display = Display.getCurrent();
				Shell shell = display.getActiveShell();
				MessageDialog.openError(shell, Messages.getString("login.failure.dialog.caption"),  //$NON-NLS-1$
						               Messages.getString("login.failure.dialog.message")); //$NON-NLS-1$
			}
		});
	}
	
	/**
	 * Prompts for user name and password for a given Guvnor repository.
	 * @param server The repository for log in
	 * @return The dialog results. Includes whether the user decided to save
	 *         the user name and password in the platform's key ring.
	 *         Null if the user cancels the dialog.
	 */
	public AuthPromptResults promptForAuthentication(final String server) {
		
		Display display = PlatformUI.getWorkbench().getDisplay();
		AuthPromptRunnable op = new AuthPromptRunnable(server);
	    display.syncExec(op);
	    return op.getResults();
	}
	
	/**
	 * An operation for running a log in dialog in the next
	 * available UI thread.
	 * @author jgraham
	 */
	class AuthPromptRunnable implements Runnable {
		AuthPromptResults res = null;
		private String server;
		
		public AuthPromptRunnable(String server) {
			this.server = server;
		}
		
		public void run() {
            Display display = Display.getCurrent();
            Shell shell = display.getActiveShell();
            AuthenticationPromptDialog diag = new AuthenticationPromptDialog(shell, server);
            if (diag.open() == Dialog.OK) {
            	Map<String, String> info = new HashMap<String, String>();
            	info.put("username", diag.getUserName()); //$NON-NLS-1$
            	info.put("password", diag.getPassword()); //$NON-NLS-1$
            	res = new AuthPromptResults(info, diag.saveAuthenInfo());
            }
        }
		
		public AuthPromptResults getResults() {
			return res;
		}
	}
	
	/**
	 * The results from a log in dialog prompt.
	 * @author jgraham
	 */
	public class AuthPromptResults {
		// username and password
		private Map<String, String> info;
		// whether the user wants to save the authentication information
		// in the platform's key ring file
		private boolean saved;
		
		public AuthPromptResults(Map<String, String> info, boolean saved) {
			this.info = info;
			this.saved = saved;
		}
		
		public Map<String, String> getAuthInfo() {
			return info;
		}
		
		public boolean wasSaved() {
			return saved;
		}
	}
}