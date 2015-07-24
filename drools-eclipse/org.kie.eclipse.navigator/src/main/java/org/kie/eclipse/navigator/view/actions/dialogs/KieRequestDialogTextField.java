package org.kie.eclipse.navigator.view.actions.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;

import com.eclipsesource.json.JsonObject;

public class KieRequestDialogTextField extends KieRequestDialogField<String> {
	Text text;
	
	public KieRequestDialogTextField(Composite parent, String labelValue, String textValue, final JsonObject object, final String name) {
		super(parent, object, name);
		
		createLabel(parent, labelValue);
		
		text = createText(parent,SWT.BORDER, textValue);
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				updateObject(text.getText());
			}
		});
	}
	
	public Control getControl() {
		return text;
	}
}