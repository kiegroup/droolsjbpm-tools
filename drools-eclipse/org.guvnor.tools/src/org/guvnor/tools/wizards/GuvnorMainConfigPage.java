package org.guvnor.tools.wizards;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.guvnor.tools.Activator;
import org.guvnor.tools.Messages;
import org.guvnor.tools.preferences.GuvnorPreferencePage;
import org.guvnor.tools.utils.PlatformUtils;

public class GuvnorMainConfigPage extends WizardPage {
	
	private Text serverField;
	private Text portField;
	private Text replocField;
	private Text unField;
	private Text pwField;
	
	private Button cbSavePassword;
	private boolean saveAuthInfo;
	
	private Label warningLabel;
	
	public GuvnorMainConfigPage(String pageName) {
		super(pageName);
	}

	public GuvnorMainConfigPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	public void createControl(Composite parent) {
		
		Composite composite = PlatformUtils.createComposite(parent, 2);
		new Label(composite, SWT.NONE).setText(Messages.getString("mainconfig.guvnor.loc")); //$NON-NLS-1$
		serverField = new Text(composite, SWT.SINGLE | SWT.BORDER);
		serverField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		serverField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateModel();
			}
		});
		addDropSupport(serverField);
		
		serverField.setFocus();
		
		new Label(composite, SWT.NONE).setText(Messages.getString("mainconfig.port")); //$NON-NLS-1$
		portField = new Text(composite, SWT.SINGLE | SWT.BORDER);
		portField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		portField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateModel();
			}
		});
		
		new Label(composite, SWT.NONE).setText(Messages.getString("mainconfig.rep")); //$NON-NLS-1$
		replocField = new Text(composite, SWT.SINGLE | SWT.BORDER);
		replocField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		replocField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateModel();
			}
		});
		
		new Label(composite, SWT.NONE).setText(Messages.getString("mainconfig.user.name")); //$NON-NLS-1$
		unField = new Text(composite, SWT.SINGLE | SWT.BORDER);
		unField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		unField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateModel();
			}
		});
		
		new Label(composite, SWT.NONE).setText(Messages.getString("mainconfig.password")); //$NON-NLS-1$
		pwField = new Text(composite, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
		pwField.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		pwField.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateModel();
			}
		});

		new Label(composite, SWT.NONE).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Composite pwgroup = PlatformUtils.createComposite(composite, 2);
		cbSavePassword = new Button(pwgroup, SWT.CHECK);
		cbSavePassword.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) { }

			public void widgetSelected(SelectionEvent e) {
				saveAuthInfo = cbSavePassword.getSelection();
				warningLabel.setEnabled(saveAuthInfo);
				updateModel();
			}
			
		});
		
		boolean shouldSavePasswords = GuvnorPreferencePage.shouldSavePasswords();
		// WTF? setSelection(true) is not picked up by the control, so we have to set 
		// this initial value explicitly. After that toggle seems to work...
		saveAuthInfo = shouldSavePasswords;
		cbSavePassword.setSelection(shouldSavePasswords);
		
		new Label(pwgroup, SWT.NONE).setText(Messages.getString("mainconfig.save.password")); //$NON-NLS-1$
		new Label(composite, SWT.NONE).setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		warningLabel = new Label(composite, SWT.WRAP);
		warningLabel.setText(Messages.getString("mainconfig.save.password.warning")); //$NON-NLS-1$
		warningLabel.setEnabled(shouldSavePasswords);
		
		super.setControl(composite);
	}

	private void addDropSupport(Text field) {
		int operations = DND.DROP_COPY | DND.DROP_DEFAULT;
		DropTarget target = new DropTarget(field, operations);
		final TextTransfer textTransfer = TextTransfer.getInstance();
		Transfer[] types = new Transfer[] { textTransfer };
		target.setTransfer(types);
		target.addDropListener(new DropTargetListener() {
			public void dragEnter(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					if ((event.operations & DND.DROP_COPY) != 0) {
						event.detail = DND.DROP_COPY;
					} else {
						event.detail = DND.DROP_NONE;
					}
				}
			}

			public void dragLeave(DropTargetEvent event) { }

			public void dragOperationChanged(DropTargetEvent event) {
				if (event.detail == DND.DROP_DEFAULT) {
					if ((event.operations & DND.DROP_COPY) != 0) {
						event.detail = DND.DROP_COPY;
					} else {
						event.detail = DND.DROP_NONE;
					}
				}
			}

			public void dragOver(DropTargetEvent event) {
				event.feedback = DND.FEEDBACK_SELECT | DND.FEEDBACK_SCROLL;
			}

			public void drop(DropTargetEvent event) {
				if (textTransfer.isSupportedType(event.currentDataType)) {
					parseCandidateUrl((String)event.data);
				}
			}

			public void dropAccept(DropTargetEvent event) { }
		});
	}
	
	private void parseCandidateUrl(String dropped) {
		try {
			URL server = new URL(dropped);
			serverField.setText(server.getHost());
			if (server.getPort() != -1) {
				portField.setText(String.valueOf(server.getPort()));
			}
			replocField.setText(server.getFile());
		} catch (MalformedURLException e) {
			Activator.getDefault().writeLog(IStatus.ERROR, e.getMessage(), e);
		}
	}
	
	private void updateModel() {
		GuvWizardModel model = ((IGuvnorWizard)super.getWizard()).getModel();
		try {
			URL server = validateUrl();
			model.setRepLocation(server.toString());
			model.setUsername(unField.getText());
			model.setPassword(pwField.getText());
			model.setSaveAuthInfo(saveAuthInfo);
			model.setCreateNewRep(true);
		} catch (Exception e) {
			model.setRepLocation(null);
		}
		IWizardContainer container = super.getWizard().getContainer();
		if (container != null) {
			container.updateButtons();
		}
	}

	private URL validateUrl() throws Exception {
		// If the server text box is empty, this is not a valid location
		if (serverField.getText().trim().length() == 0) {
			return null;
		}
		// First we'll test if the server text box is a complete
		// URL in itself. If so, we'll parse it out into the other
		// fields and leave early
		URL res = testCompleteField();
		if (res != null) {
			return res;
		}
		// Try to construct a valid URL from the text boxes
		String repPath = null;
		if (replocField.getText().trim().length() > 0) {
			if (replocField.getText().startsWith("/")) { //$NON-NLS-1$
				repPath = replocField.getText();
			} else {
				repPath = "/" + replocField.getText(); //$NON-NLS-1$
			}
		} else {
			repPath = ""; //$NON-NLS-1$
		}
		if (portField.getText().trim().length() > 0) {
			int port = Integer.parseInt(portField.getText());
			res = new URL("http", serverField.getText(), port, repPath); //$NON-NLS-1$
		} else {
			res = new URL("http", serverField.getText(), repPath); //$NON-NLS-1$
		}
		return res;
	}
	
	private URL testCompleteField() {
		URL res = null;
		try {
			res = new URL(serverField.getText());
			parseCandidateUrl(res.toString());
		} catch (MalformedURLException e) {
			// If it is not a valid URL, we just move along...
		}
		return res;
	}

	@Override
	public boolean isPageComplete() {
		GuvWizardModel model = ((IGuvnorWizard)super.getWizard()).getModel();
		return model.getRepLocation() != null;
	}
	
	private boolean initialized = false;
	
	@Override
	public void setVisible(boolean visible) {
		if (visible && !initialized) {
			// See if there is a preference setting for the default values and, if so, use it
			String guvnorLocTemplate = GuvnorPreferencePage.getGuvnorTemplatePref();
			if (guvnorLocTemplate != null && guvnorLocTemplate.trim().length() > 0) {
				parseCandidateUrl(guvnorLocTemplate);
			}
			initialized = true;
		}
		super.setVisible(visible);
	}
}
