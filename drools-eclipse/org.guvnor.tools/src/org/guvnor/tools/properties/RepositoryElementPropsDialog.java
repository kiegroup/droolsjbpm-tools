package org.guvnor.tools.properties;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
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
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.guvnor.tools.Activator;
import org.guvnor.tools.utils.PlatformUtils;
import org.guvnor.tools.views.model.TreeObject;

public class RepositoryElementPropsDialog extends TitleAreaDialog {
	
	private static final int INITIAL_WIDTH = 780;
	private static final int INITIAL_HEIGHT = 400;
	
	private TreeObject node;
	private Text unField;
	private Text pwField;
	private Button cbSavePassword;
	private String username;
	private String password;
	private boolean saveInfo;
	
	private Label warningLabel;
	
	public RepositoryElementPropsDialog(Shell parentShell, TreeObject node) {
		super(parentShell);
		super.setShellStyle(getShellStyle() | SWT.RESIZE);
		this.node = node;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		super.setTitle("Guvnor Repository Element");
		super.setMessage("Properties for element: " + node.getName());
		super.setTitleImage(Activator.getImageDescriptor(Activator.IMG_GUVREPWIZBAN).createImage());
		
		TabFolder folder = new TabFolder(parent, SWT.TOP);
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		TabItem tab = new TabItem(folder, SWT.NONE);
		tab.setText("Basic");
		Composite composite = PlatformUtils.createComposite(folder, 2);
		tab.setControl(composite);
		addElementProperties(composite);
		
		tab = new TabItem(folder, SWT.NONE);
		tab.setText("Security");
		composite = PlatformUtils.createComposite(folder, 2);
		tab.setControl(composite);
		addSecurityProperties(composite);
		
		return super.createDialogArea(parent);
	}
	
	private void addElementProperties(Composite composite) {
		IPropertySource ps = (IPropertySource)node.getAdapter(IPropertySource.class);
		if (ps == null) {
			return;
		}
		IPropertyDescriptor[] desc = ps.getPropertyDescriptors();
		for (int i = 0; i < desc.length; i++) {
			if (desc[i] instanceof TextPropertyDescriptor) {
				TextPropertyDescriptor oneDesc = (TextPropertyDescriptor)desc[i];
				new Label(composite, SWT.NONE).setText(oneDesc.getDisplayName() + ":");
				String val = ps.getPropertyValue(oneDesc.getId()) != null?
								(String)ps.getPropertyValue(oneDesc.getId()):"";
				new Label(composite, SWT.NONE).setText(val);
			}
		}
	}
	
	private void addSecurityProperties(Composite composite) {
		new Label(composite, SWT.NONE).setText("User Name: ");
		unField = new Text(composite, SWT.SINGLE | SWT.BORDER);
		unField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		unField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				username = unField.getText();	
			}
		});
		
		new Label(composite, SWT.NONE).setText("Password: ");
		pwField = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
		pwField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		pwField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				password = pwField.getText();
			}
		});
		
		new Label(composite, SWT.NONE).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Composite pwgroup = PlatformUtils.createComposite(composite, 2);
		cbSavePassword = new Button(pwgroup, SWT.CHECK);
		cbSavePassword.addSelectionListener(new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				saveInfo = cbSavePassword.getSelection();
				warningLabel.setEnabled(saveInfo);
			}
			
		});
			
		new Label(pwgroup, SWT.NONE).setText("Save user name and password");
		
		new Label(composite, SWT.NONE).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		warningLabel = new Label(composite, SWT.WRAP);
		warningLabel.setText("NOTE: Saved passwords are stored on your computer in a file that is difficult, but not impossible, for an intruder to read.");
		
		populateSecuritySettings();
		if (unField.getText().trim().length() == 0) {
			cbSavePassword.setSelection(false);
			warningLabel.setEnabled(false);
		} else {
			// WTF? setSelection(true) is not picked up by the control, so we have to set 
			// this initial value explicitly. After that toggle seems to work...
			saveInfo = true;
			cbSavePassword.setSelection(true);
		}
	}
	
	@SuppressWarnings("unchecked")
	private void populateSecuritySettings() {
		try {
			Map info = Platform.getAuthorizationInfo(
					new URL(node.getGuvnorRepository().getLocation()), "", "basic");
			if (info == null) {
				return;
			}
			String un = (String)info.get("username");
			if (un != null) {
				unField.setText(un);
			}
			String pw = (String)info.get("password");
			if (pw != null) {
				pwField.setText(pw);
			}
		} catch (MalformedURLException e) {
			Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
		}
	}
	
	@Override
	protected Point getInitialSize() {
		// Try to set a reasonable default size.
		return new Point(INITIAL_WIDTH, INITIAL_HEIGHT);
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	public boolean saveAuthenInfo() {
		return saveInfo;
	}
	
	public boolean wereSecuritySettingModified() {
		// If username or password is not null,
		// then the user changed the text in at least
		// one of those boxes.
		return username != null || password != null; 
	}
}
