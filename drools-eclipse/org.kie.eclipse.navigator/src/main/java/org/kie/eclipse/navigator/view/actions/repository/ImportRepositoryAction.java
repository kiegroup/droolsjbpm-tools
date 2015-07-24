package org.kie.eclipse.navigator.view.actions.repository;

import java.io.File;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.egit.core.EclipseGitProgressTransformer;
import org.eclipse.egit.core.RepositoryUtil;
import org.eclipse.egit.core.securestorage.UserPasswordCredentials;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.TransportConfigCallback;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig.Host;
import org.eclipse.jgit.transport.Transport;
import org.eclipse.jgit.transport.TransportGitSsh;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.jgit.util.FS;
import org.eclipse.swt.widgets.Display;
import org.kie.eclipse.navigator.Activator;
import org.kie.eclipse.navigator.IKieNavigatorConstants;
import org.kie.eclipse.navigator.preferences.PreferencesUtils;
import org.kie.eclipse.navigator.view.actions.KieNavigatorAction;
import org.kie.eclipse.navigator.view.actions.dialogs.LoginDialog;
import org.kie.eclipse.navigator.view.content.ContentNode;
import org.kie.eclipse.navigator.view.content.IContainerNode;
import org.kie.eclipse.navigator.view.server.IKieRepositoryHandler;
import org.kie.eclipse.navigator.view.server.IKieServerHandler;
import org.kie.eclipse.navigator.view.server.IKieServiceDelegate;
import org.kie.eclipse.navigator.view.server.KieRepositoryHandler;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

public class ImportRepositoryAction extends KieNavigatorAction {

	protected ImportRepositoryAction(ISelectionProvider provider, String text) {
		super(provider, text);
	}

	public ImportRepositoryAction(ISelectionProvider selectionProvider) {
		this(selectionProvider, "Import Repository");
	}

	@Override
	public boolean isEnabled() {
		IContainerNode<?> container = getContainer();
		if (container instanceof ContentNode) {
			KieRepositoryHandler handler = (KieRepositoryHandler) ((ContentNode) container).getHandler();
			if (handler == null || !handler.isLoaded())
				return true;
		}
		return false;
	}

	@Override
	public String getToolTipText() {
		return "Clone this Git Repository from the remote server";
	}

	@SuppressWarnings("restriction")
	public void run() {
		final IContainerNode<?> container = getContainer();
		if (container==null)
			return;
		
		final IKieRepositoryHandler handler = (IKieRepositoryHandler) container.getHandler();
		final IKieServerHandler server = (IKieServerHandler) handler.getRoot();
		final IKieServiceDelegate delegate = getDelegate();

		try {
			String host = delegate.getServer().getHost();
			int port = delegate.getGitPort();
			String username = delegate.getUsername();
			String password = delegate.getPassword();
			URIish uri = PreferencesUtils.getRepoURI(host, port, username, handler.getName());
			LoginDialog dlg = new LoginDialog(getShell(), uri);
			dlg.setUsername(username);
			dlg.setPassword(password);
			if (dlg.open() != Dialog.OK)
				return;
			username = dlg.getUsername();
			password = dlg.getPassword();
			uri = PreferencesUtils.getRepoURI(host, port, username, handler.getName());
			final String localPath = PreferencesUtils.getRepoPath(handler);
			final String remotePath = uri.toString();
			final CredentialsProvider credentialsProvider = new KieCredentialsProvider(server, username, password);
			Job job = new Job("Import Repository") {
				@Override
				protected IStatus run(final IProgressMonitor monitor) {
					Repository localRepo = null;
					try {
						EclipseGitProgressTransformer gitMonitor = new EclipseGitProgressTransformer(monitor);
						localRepo = new FileRepository(localPath + File.separator + ".git");
						CloneCommand cloneCmd = Git.cloneRepository().setURI(remotePath).setDirectory(new File(localPath))
								.setProgressMonitor(gitMonitor).setCloneAllBranches(true).setTimeout(60)
								.setCredentialsProvider(credentialsProvider)
								.setTransportConfigCallback(new TransportConfigCallback() {
									@Override
									public void configure(Transport transport) {
										TransportGitSsh gt = (TransportGitSsh) transport;
										gt.setSshSessionFactory(new JschConfigSessionFactory() {
											@Override
											protected Session createSession(Host hc, String user, String host, int port, FS fs)
													throws JSchException {
												Session session = super.createSession(hc, user, host, port, fs);
												session.setConfig("PreferredAuthentications", "password");
												return session;
											}

											@Override
											protected void configure(Host hc, Session session) {
												session.setConfig("PreferredAuthentications", "password");
											}
										});
									}
								});

						cloneCmd.call();
						refreshViewer(container.getParent());
						localRepo.close();
						
						RepositoryUtil util = org.eclipse.egit.ui.Activator.getDefault().getRepositoryUtil();
						util.addConfiguredRepository(localRepo.getDirectory());
						localRepo = null;
					}
					catch (Exception e) {
						handleException(e);
					}
					finally {
						if (localRepo != null)
							localRepo.close();
					}
					return Status.OK_STATUS;
				}
			};
			job.setUser(true);
			job.schedule();
		}
		catch (Exception e) {
			handleException(e);
		}
	}

	class KieCredentialsProvider extends CredentialsProvider {
		private IKieServerHandler server;
		private String user;
		private String password;

		public KieCredentialsProvider(IKieServerHandler server, String username, String password) {
			this.server = server;
			this.user = username;
			this.password = password;
		}

		@Override
		public boolean isInteractive() {
			return false;
		}

		@Override
		public boolean supports(CredentialItem... items) {
			for (CredentialItem i : items) {
				if (i instanceof CredentialItem.Username)
					continue;
				else if (i instanceof CredentialItem.Password)
					continue;
				else
					return false;
			}
			return true;
		}

		@Override
		public boolean get(URIish uri, CredentialItem... items) throws UnsupportedCredentialItem {
			if (items.length == 0) {
				return true;
			}

			CredentialItem.Username userItem = null;
			CredentialItem.Password passwordItem = null;
			for (final CredentialItem item : items) {
				System.out.println("Credentials.get: " + item.getPromptText());
				if (item instanceof CredentialItem.Username)
					userItem = (CredentialItem.Username) item;
				else if (item instanceof CredentialItem.Password)
					passwordItem = (CredentialItem.Password) item;
				else if (item instanceof CredentialItem.YesNoType) {
					String trustedConnection = server.getPreference(
							IKieNavigatorConstants.PREF_SERVER_TRUSTED_CONNECTION,
							MessageDialogWithToggle.NEVER);
					final AtomicReference<Boolean> ar = new AtomicReference<Boolean>();
					if (MessageDialogWithToggle.ALWAYS.equals(trustedConnection)) {
						ar.set(true);
					}
					else {
						Display.getDefault().syncExec(new Runnable() {
							@Override
							public void run() {
								MessageDialogWithToggle dlg = MessageDialogWithToggle.openYesNoQuestion(
										getShell(),
										"Connect to Server",
										item.getPromptText(),
										"Don't ask me again", false,
										Activator.getDefault().getPreferenceStore(),
										server.getPreferenceName(IKieNavigatorConstants.PREF_SERVER_TRUSTED_CONNECTION));
								ar.set(dlg.getReturnCode() == IDialogConstants.YES_ID);
							}
						});
					}
					((CredentialItem.YesNoType) item).setValue(ar.get());
					return true;
				}
				else {
					throw new UnsupportedCredentialItem(uri, "Credential Item not supported : " + item.getPromptText());
				}
			}
			if ((userItem != null || passwordItem != null)) {
				UserPasswordCredentials credentials = null;
				if ((user != null) && (password != null))
					credentials = new UserPasswordCredentials(user, password);

				if (credentials == null) {
					return false;
				}
				if (userItem != null)
					userItem.setValue(credentials.getUser());
				if (passwordItem != null)
					passwordItem.setValue(credentials.getPassword().toCharArray());
				return true;
			}

			return false;
		}
	}
}
