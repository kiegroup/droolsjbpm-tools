package org.guvnor.tools.utils;

import java.text.MessageFormat;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.guvnor.tools.Activator;
import org.guvnor.tools.Messages;
import org.guvnor.tools.preferences.GuvnorPreferencePage;

/**
 * A dialog for collecting user authentication information
 * @author jgraham
 *
 */
public class AuthenticationPromptDialog extends TitleAreaDialog {
	
	private static final int INITIAL_WIDTH = 780;
	private static final int INITIAL_HEIGHT = 350;
	
	private Text unField;
	private Text pwField;
	
	private Button cbSavePassword;
	
	private String serverName;
	
	private String username;
	private String password;
	private boolean saveInfo;
	
	private Label warningLabel;
	
	public AuthenticationPromptDialog(Shell parentShell, String serverName) {
		super(parentShell);
		super.setShellStyle(getShellStyle() | SWT.RESIZE);
		this.serverName = serverName;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		super.setTitle(Messages.getString("login.dialog.caption")); //$NON-NLS-1$
		super.setMessage(MessageFormat.format(Messages.getString("login.dialog.desc"), //$NON-NLS-1$ 
				                             new Object[] { serverName })); 
		super.setTitleImage(Activator.getImageDescriptor(Activator.IMG_GUVREPWIZBAN).createImage());
		
		Composite composite = PlatformUtils.createComposite(parent, 2);
		new Label(composite, SWT.NONE).setText(Messages.getString("user.name")); //$NON-NLS-1$
		unField = new Text(composite, SWT.SINGLE | SWT.BORDER);
		unField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		unField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				username = unField.getText();	
			}
		});
		
		new Label(composite, SWT.NONE).setText(Messages.getString("password")); //$NON-NLS-1$
		pwField = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
		pwField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		pwField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				password = pwField.getText();	
			}
		});
		
		new Label(composite, SWT.NONE);
		
		cbSavePassword = new Button(composite, SWT.CHECK);
		cbSavePassword.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				saveInfo = cbSavePassword.getSelection();
				warningLabel.setEnabled(saveInfo);
			}
			
		});
		// WTF? setSelection(true) is not picked up by the control, so we have to set 
		// this initial value explicitly. After that toggle seems to work...
		boolean shouldSavePasswords = GuvnorPreferencePage.shouldSavePasswords();
		saveInfo = shouldSavePasswords;
		cbSavePassword.setSelection(shouldSavePasswords);
		cbSavePassword.setText(Messages.getString("save.password"));
		
		
		new Label(composite, SWT.NONE);
		warningLabel = new Label(composite, SWT.WRAP);
		warningLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		warningLabel.setText(Messages.getString("password.warning")); //$NON-NLS-1$
		warningLabel.setEnabled(shouldSavePasswords);
		
		return super.createDialogArea(parent);
	}
	
	@Override
	protected Point getInitialSize() {
		// Try to set a reasonable default size.
		return new Point(INITIAL_WIDTH, INITIAL_HEIGHT);
	}
	
	public String getUserName() {
		return username;
	}
	
	public String getPassword() {
		return password;
	}
	
	public boolean saveAuthenInfo() {
		return saveInfo;
	}
}
