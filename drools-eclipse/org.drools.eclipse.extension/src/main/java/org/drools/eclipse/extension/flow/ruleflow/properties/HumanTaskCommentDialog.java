package org.drools.eclipse.extension.flow.ruleflow.properties;

import org.drools.eclipse.extension.Activator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

public class HumanTaskCommentDialog extends Dialog {

    private String title;
    private String value;

	protected HumanTaskCommentDialog(Shell parentShell, String title) {
        super(parentShell);
        this.title = title;
        setShellStyle(getShellStyle() | SWT.RESIZE);
    }
    
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText(title);
    }
    
    protected Point getInitialSize() {
        return new Point(400, 200);
    }
    public String getValue() {
        return value;
    }
    
    protected void okPressed() {
        try {
            value = updateValue(value);
            super.okPressed();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
            // value could not be set, ignoring ok
        }
    }
    
    protected String updateValue(String value) {
    	return "custom value";
    }
    
    public void setValue(String value) {
        this.value = value;
    }
    
    protected void showError(String error) {
        ErrorDialog.openError(getShell(), "Error", error, new Status(
            IStatus.ERROR, Activator.getDefault().getBundle().getSymbolicName(),
            IStatus.ERROR, error, null));
    }

}
