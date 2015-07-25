package org.kie.eclipse.navigator.view.actions.dialogs;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jgit.transport.URIish;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.kie.eclipse.utils.PreferencesUtils;

public class LoginDialog extends Dialog {

	private Text usernameText;
	private Text passwordText;
	private URIish uri;
	private String username;
	private String password;

	public LoginDialog(Shell shell, URIish uri) {
		super(shell);
		this.uri = uri;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(2, false));
		getShell().setText("Login");

		Label uriLabel = new Label(composite, SWT.NONE);
		uriLabel.setText("Repository:");
		uriLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		final Text uriText = new Text(composite, SWT.READ_ONLY);
		uriText.setText(uri.toString());
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		GC gc = new GC(uriText);
		int x = gc.stringExtent(uriText.getText()).x;
		gd.minimumWidth = x + x/2;
		uriText.setLayoutData(gd);

		Label usernameLabel = new Label(composite, SWT.NONE);
		usernameLabel.setText("Username");
		usernameText = new Text(composite, SWT.BORDER);
		if (username != null)
			usernameText.setText(username);
		usernameText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				username = usernameText.getText();
				String path = uri.getPath();
				if (path.startsWith("/"))
					path = path.substring(1);
				uri = PreferencesUtils.getRepoURI(uri.getHost(), uri.getPort(), username, path);
				uriText.setText(uri.toString());
			}
		});
		GridDataFactory.fillDefaults().grab(true, false).applyTo(usernameText);

		Label passwordLabel = new Label(composite, SWT.NONE);
		passwordLabel.setText("Password");
		passwordText = new Text(composite, SWT.PASSWORD | SWT.BORDER);
		if (password!=null) {
			passwordText.setText(password);
		}
		GridDataFactory.fillDefaults().grab(true, false).applyTo(passwordText);

		return composite;
	}

	@Override
	protected void okPressed() {
		if (usernameText.getText().length() > 0) {
			username = usernameText.getText();
			password = passwordText.getText();
		}
		super.okPressed();
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUsername() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
}
