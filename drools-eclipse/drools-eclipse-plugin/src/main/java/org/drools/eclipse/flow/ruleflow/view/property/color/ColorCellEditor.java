package org.drools.eclipse.flow.ruleflow.view.property.color;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;

public class ColorCellEditor extends DialogCellEditor {

    public ColorCellEditor(Composite parent) {
        super(parent);
    }
    
    protected Object openDialogBox(Control cellEditorWindow) {
    	ColorDialog dialog = new ColorDialog(Display.getCurrent().getActiveShell());
    	Color color = (Color) getValue();
    	if (color != null) {
    		dialog.setRGB(color.getRGB());
    	}
		RGB result = (RGB) dialog.open();
		return result == null ? null : new Color(Display.getCurrent(), result);
    }

}
