package org.guvnor.tools.properties;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.views.properties.IPropertyDescriptor;
import org.eclipse.ui.views.properties.IPropertySource;
import org.eclipse.ui.views.properties.TextPropertyDescriptor;
import org.guvnor.tools.Activator;
import org.guvnor.tools.Messages;
import org.guvnor.tools.utils.PlatformUtils;
import org.guvnor.tools.views.model.TreeObject;
/**
 * Shows properties for a Guvnor repository element.
 * @author jgraham
 */
public class RepositoryElementPropsDialog extends TitleAreaDialog {
	
	private static final int INITIAL_WIDTH = 780;
	private static final int INITIAL_HEIGHT = 400;
	
	private TreeObject node;
	private Text unField;
	private Text pwField;
	
	public RepositoryElementPropsDialog(Shell parentShell, TreeObject node) {
		super(parentShell);
		super.setShellStyle(getShellStyle() | SWT.RESIZE);
		this.node = node;
	}
	
	@Override
	protected Control createDialogArea(Composite parent) {
		super.setTitle(Messages.getString("guvnor.repository.element")); //$NON-NLS-1$
		super.setMessage(MessageFormat.format(Messages.getString("guvnor.resource.properties"), //$NON-NLS-1$ 
				                              new Object[] { node.getName() })); 
		super.setTitleImage(Activator.getImageDescriptor(Activator.IMG_GUVREPWIZBAN).createImage());
		
		TabFolder folder = new TabFolder(parent, SWT.TOP);
		folder.setLayoutData(new GridData(GridData.FILL_BOTH));
		TabItem tab = new TabItem(folder, SWT.NONE);
		tab.setText(Messages.getString("properties.basic")); //$NON-NLS-1$
		Composite composite = PlatformUtils.createComposite(folder, 2);
		tab.setControl(composite);
		addElementProperties(composite);
		
		tab = new TabItem(folder, SWT.NONE);
		tab.setText(Messages.getString("properties.security")); //$NON-NLS-1$
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
				new Label(composite, SWT.NONE).
						setText(MessageFormat.format(Messages.getString("guvnor.resource.property"), //$NON-NLS-1$
								new Object[] { oneDesc.getDisplayName() })); 
				String val = ps.getPropertyValue(oneDesc.getId()) != null?
								(String)ps.getPropertyValue(oneDesc.getId()):""; //$NON-NLS-1$
				new Label(composite, SWT.NONE).setText(val);
			}
		}
	}
	
	private void addSecurityProperties(Composite composite) {
		new Label(composite, SWT.NONE).setText(Messages.getString("user.name")); //$NON-NLS-1$
		unField = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
		unField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		unField.setEditable(false);
		unField.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		
		new Label(composite, SWT.NONE).setText(Messages.getString("password")); //$NON-NLS-1$
		pwField = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD  | SWT.READ_ONLY);
		pwField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		pwField.setEditable(false);
		pwField.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));

		populateSecuritySettings();
	}
	
	@SuppressWarnings("unchecked")
	private void populateSecuritySettings() {
		try {
			Map info = Platform.getAuthorizationInfo(
					new URL(node.getGuvnorRepository().getLocation()), "", "basic"); //$NON-NLS-1$ //$NON-NLS-2$
			if (info == null) {
				return;
			}
			String un = (String)info.get("username"); //$NON-NLS-1$
			if (un != null) {
				unField.setText(un);
			}
			String pw = (String)info.get("password"); //$NON-NLS-1$
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

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText(Messages
				.getString("guvnor.resource.properties.title")); //$NON-NLS-1$
	}

}
