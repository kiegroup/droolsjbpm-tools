package org.kie.eclipse.navigator.view.actions.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import com.eclipsesource.json.JsonObject;

public abstract class AbstractKieRequestDialog extends TitleAreaDialog {
	
	protected JsonObject properties = new JsonObject();
	protected Button okButton;
	protected Composite errorComposite;
	protected Label errorText;
	protected IKieRequestValidator validator;
	private String title;

	protected abstract void createFields(Composite parent);
	
	public AbstractKieRequestDialog(Shell shell, String title, IKieRequestValidator validator) {
		super(shell);
		this.validator = validator;
		this.title = title;
		// We don't really provide help, the help toolbar is used as an
		// error message field which includes an image and text label.
		// This is better than the default TitleAreaDialog behavior of
		// overwriting the description with an error message.
		// See the createHelpControl()} and setErrorMessage() overrides
		
        setHelpAvailable(true);
	}
	
	public JsonObject getResult() {
		return properties;
	}

    /* (non-Javadoc)
     * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
     */
	@Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(title);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.TitleAreaDialog#createContents(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createContents(Composite parent) {
        Control contents = super.createContents(parent);
        setTitle(title);
        validate();
        return contents;
    }
    
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite parentComposite = (Composite)super.createDialogArea(parent);
		Composite composite = new Composite(parentComposite, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		composite.setLayout(new GridLayout(3,false));
		composite.setFont(parent.getFont());
		createFields(composite);
		return parentComposite;
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.TrayDialog#createHelpControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createHelpControl(Composite parent) {
        ((GridLayout) parent.getLayout()).numColumns++;
        errorComposite = new Composite(parent, SWT.NONE);
        errorComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        errorComposite.setLayout(new GridLayout(2, false));
    	Label errorImage = new Label(errorComposite, SWT.NONE);
    	errorImage.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
    	errorImage.setImage(JFaceResources.getImage(DLG_IMG_TITLE_ERROR));

    	errorText = new Label(errorComposite, SWT.NONE);
    	errorText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
    	errorText.setText("");
    	return errorComposite;
    }
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#setErrorMessage(java.lang.String)
	 */
	@Override
	public void setErrorMessage(String newErrorMessage) {
		if (errorComposite!=null && !errorComposite.isDisposed()) {
			if (newErrorMessage==null || newErrorMessage.isEmpty()) {
				errorComposite.setVisible(false);
				((GridData)errorComposite.getLayoutData()).exclude = true;
				errorText.setText("");
			}
			else {
				errorComposite.setVisible(true);
				((GridData)errorComposite.getLayoutData()).exclude = false;
				errorText.setText(newErrorMessage);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createButton(org.eclipse.swt.widgets.Composite, int, java.lang.String, boolean)
	 */
	@Override
	protected Button createButton(Composite parent, int id, String label, boolean defaultButton) {
		Button button = super.createButton(parent, id, label, defaultButton);
		if (id==IDialogConstants.OK_ID)
			okButton = button;
		return button;
	}
	
	protected void validate() {
		String msg = null;
		if (validator!=null && (msg = validator.isValid(properties))!=null) {
			setErrorMessage(msg);
			setDialogComplete(false);
		}
		else {
			setErrorMessage(null);
			setDialogComplete(true);
		}
	}
	
    protected void setDialogComplete(boolean value) {
        okButton.setEnabled(value);
    }
}
