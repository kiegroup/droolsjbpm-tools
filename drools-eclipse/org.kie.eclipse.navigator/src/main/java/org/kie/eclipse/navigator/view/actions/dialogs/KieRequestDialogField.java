package org.kie.eclipse.navigator.view.actions.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

public abstract class KieRequestDialogField<TYPE extends Object> {
	JsonObject object;
	String name;
	int numColumns;
	IKieRequestChangeListener changeListener;

	public KieRequestDialogField(final Composite parent, final JsonObject object, final String name) {
		this.object = object;
		this.name = name;
		numColumns = ((GridLayout)parent.getLayout()).numColumns;
	}

	public void setChangeListener(IKieRequestChangeListener changeListener) {
		this.changeListener = changeListener;
	}
		
	protected void updateObject(TYPE value) {
		JsonValue oldValue = object.get(name);
		JsonValue newValue = JsonValue.valueOf(value);
		if (newValue!=null && !newValue.equals(oldValue)) {
			object.set(name, (TYPE)value);
			if (changeListener!=null)
				changeListener.objectChanged(object);
		}
	}
	
	protected GridData createLabelGridData() {
		return new GridData(SWT.END, SWT.CENTER, false, true, 1, 1);
	}
	
	protected GridData createControlGridData() {
		return new GridData(SWT.FILL, SWT.CENTER, true, true, numColumns-1, 1);
	}
	
	protected Label createLabel(Composite parent, String labelValue) {
		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(createLabelGridData());
		label.setFont(parent.getFont());
		label.setText(labelValue);
		return label;
	}
	
	protected Text createText(Composite parent, int style, String textValue) {
		Text text = new Text(parent,style);
		text.setLayoutData(createControlGridData());
		text.setFont(parent.getFont());
		text.setText(textValue);
		return text;
	}
	
	public abstract Control getControl();
}
