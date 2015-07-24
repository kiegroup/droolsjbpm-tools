package org.kie.eclipse.navigator.preferences;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class PasswordFieldEditor extends FieldEditor {

    /**
     * Text limit constant (value <code>-1</code>) indicating unlimited
     * text limit and width.
     */
    public static int UNLIMITED = -1;

    /**
     * Old text value.
     * @since 3.4 this field is protected.
     */
    protected String oldValue;

    /**
     * The text field, or <code>null</code> if none.
     */
    Text textField;

    /**
     * Width of text field in characters; initially unlimited.
     */
    private int widthInChars = UNLIMITED;

	public PasswordFieldEditor(String name, String labelText, Composite parent) {
		super(name, labelText, parent);
	}
	
    public void setStringValue(String value) {
        if (textField != null) {
            if (value == null) {
				value = "";//$NON-NLS-1$
			}
            oldValue = textField.getText();
            if (!oldValue.equals(value)) {
                textField.setText(value);
                valueChanged();
            }
        }
    }

	@Override
	protected void adjustForNumColumns(int numColumns) {
	       GridData gd = (GridData) textField.getLayoutData();
	        gd.horizontalSpan = numColumns - 1;
	        // We only grab excess space if we have to
	        // If another field editor has more columns then
	        // we assume it is setting the width.
	        gd.grabExcessHorizontalSpace = gd.horizontalSpan == 1;
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
        getLabelControl(parent);

        textField = getTextControl(parent);
        GridData gd = new GridData();
        gd.horizontalSpan = numColumns - 1;
        if (widthInChars != UNLIMITED) {
            GC gc = new GC(textField);
            try {
                Point extent = gc.textExtent("X");//$NON-NLS-1$
                gd.widthHint = widthInChars * extent.x;
            } finally {
                gc.dispose();
            }
        } else {
            gd.horizontalAlignment = GridData.FILL;
            gd.grabExcessHorizontalSpace = true;
        }
        textField.setLayoutData(gd);
	}
    public Text getTextControl(Composite parent) {
        if (textField == null) {
            textField = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.PASSWORD);
            textField.setFont(parent.getFont());
        }

        return textField;
    }

    protected void valueChanged() {
        setPresentsDefaultValue(false);

        String newValue = textField.getText();
        if (!newValue.equals(oldValue)) {
            fireValueChanged(VALUE, oldValue, newValue);
            oldValue = newValue;
        }
    }

	@Override
	protected void doLoad() {
        if (textField != null) {
            String value = getPreferenceStore().getString(getPreferenceName());
            textField.setText(value);
            oldValue = value;
        }
	}

	@Override
	protected void doLoadDefault() {
        if (textField != null) {
            String value = getPreferenceStore().getDefaultString(
                    getPreferenceName());
            textField.setText(value);
        }
        valueChanged();
	}

	@Override
	protected void doStore() {
        getPreferenceStore().setValue(getPreferenceName(), textField.getText());
	}

	@Override
	public int getNumberOfControls() {
        return 2;
	}

}
