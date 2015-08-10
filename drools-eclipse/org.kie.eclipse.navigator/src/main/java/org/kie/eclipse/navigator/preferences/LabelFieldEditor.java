package org.kie.eclipse.navigator.preferences;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class LabelFieldEditor extends FieldEditor {

    /**
     * Text limit constant (value <code>-1</code>) indicating unlimited
     * text limit and width.
     */
    public static int UNLIMITED = -1;

    /**
     * The Label field.
     */
    Label labelField;
    
    String labelText;

    /**
     * Width of text field in characters; initially unlimited.
     */
    private int widthInChars = UNLIMITED;

	public LabelFieldEditor() {
		// TODO Auto-generated constructor stub
	}

	public LabelFieldEditor(String labelText, Composite parent) {
		super("NONE", "", parent);
		this.labelText = labelText;
	}

	@Override
	protected void adjustForNumColumns(int numColumns) {
	       GridData gd = (GridData) labelField.getLayoutData();
	        gd.horizontalSpan = numColumns;
	        // We only grab excess space if we have to
	        // If another field editor has more columns then
	        // we assume it is setting the width.
	        gd.grabExcessHorizontalSpace = gd.horizontalSpan == 1;
	}

	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
        getLabelControl(parent);

        labelField = getTextControl(parent);
        GridData gd = new GridData();
        gd.horizontalSpan = numColumns;
        if (widthInChars != UNLIMITED) {
            GC gc = new GC(labelField);
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
        labelField.setLayoutData(gd);
	}
	
    public Label getTextControl(Composite parent) {
        if (labelField == null) {
            labelField = new Label(parent, SWT.NONE);
            labelField.setFont(parent.getFont());
        }
        if (labelText!=null)
            labelField.setText(labelText);

        return labelField;
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
        return 1;
	}

}
