package org.kie.eclipse.navigator.view.actions.repository;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.egit.core.securestorage.UserPasswordCredentials;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jgit.errors.UnsupportedCredentialItem;
import org.eclipse.jgit.transport.CredentialItem;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.swt.widgets.Display;
import org.kie.eclipse.IKieConstants;
import org.kie.eclipse.navigator.Activator;
import org.kie.eclipse.server.IKieServerHandler;

public class KieCredentialsProvider extends CredentialsProvider {
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
						IKieConstants.PREF_SERVER_TRUSTED_CONNECTION,
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
									Display.getDefault().getActiveShell(),
									"Connect to Server",
									item.getPromptText(),
									"Don't ask me again", false,
									Activator.getDefault().getPreferenceStore(),
									server.getPreferenceName(IKieConstants.PREF_SERVER_TRUSTED_CONNECTION));
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