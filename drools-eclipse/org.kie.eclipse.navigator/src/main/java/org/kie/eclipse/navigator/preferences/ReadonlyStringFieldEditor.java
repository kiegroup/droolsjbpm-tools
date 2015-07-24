package org.kie.eclipse.navigator.preferences;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class ReadonlyStringFieldEditor extends FieldEditor {

    /**
     * Text limit constant (value <code>-1</code>) indicating unlimited
     * text limit and width.
     */
    public static int UNLIMITED = -1;

    /**
     * The text field, or <code>null</code> if none.
     */
    Text textField;
    
    String textValue;

    /**
     * Width of text field in characters; initially unlimited.
     */
    private int widthInChars = UNLIMITED;

	public ReadonlyStringFieldEditor() {
		// TODO Auto-generated constructor stub
	}

	public ReadonlyStringFieldEditor(String labelText, String textValue, Composite parent) {
		super("NONE", labelText, parent);
		this.textValue = textValue;
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
            textField = new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
            textField.setFont(parent.getFont());
        }
        if (textValue!=null)
            textField.setText(textValue);

        return textField;
    }
    
	@Override
	protected void doLoad() {
	}

	@Override
	protected void doLoadDefault() {
	}

	@Override
	protected void doStore() {
	}

	@Override
	public int getNumberOfControls() {
        return 2;
	}

}
