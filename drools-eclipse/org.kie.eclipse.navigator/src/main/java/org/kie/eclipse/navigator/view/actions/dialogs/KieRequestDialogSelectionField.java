package org.kie.eclipse.navigator.view.actions.dialogs;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.eclipsesource.json.JsonObject;

public class KieRequestDialogSelectionField extends KieRequestDialogField<Object> {
	Combo combo = null;
	Button[] buttons = null;
	
	public KieRequestDialogSelectionField(Composite parent, String labelValue, String[] textValues, Object[] dataValues, int style, final JsonObject object, final String name) {
		super(parent, object, name);
		
		createLabel(parent, labelValue);
		
		if (style==SWT.DROP_DOWN) {
			combo = new Combo(parent, SWT.DROP_DOWN | SWT.READ_ONLY);
			combo.setLayoutData(createControlGridData());
			combo.setFont(parent.getFont());
			for (int i=0; i<textValues.length; ++i) {
				combo.add(textValues[i]);
				combo.setData(Integer.toString(i), dataValues[i]);
			}
			combo.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					int i = combo.getSelectionIndex();
					if (i>=0) {
						updateObject(combo.getData(Integer.toString(i)));
					}
				}
			});
			combo.select(0);
		}
		else if (style==SWT.RADIO) {
			Composite buttonComposite = new Composite(parent, SWT.NONE);
			buttonComposite.setLayoutData(createControlGridData());
			buttonComposite.setLayout(new FillLayout(SWT.VERTICAL));
			buttons = new Button[textValues.length];
			for (int i=0; i<textValues.length; ++i) {
				Button b = new Button(buttonComposite, SWT.RADIO);
				b.setText(textValues[i]);
				b.setData(dataValues[i]);
				b.setFont(parent.getFont());
				b.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						Button b = (Button)e.getSource();
						updateObject(b.getData());
					}					
				});
				buttons[i] = b;
			}
			buttons[0].setSelection(true);
		}
	}
	
	public Control getControl() {
		if (buttons!=null) {
			for (Button b : buttons) {
				if (b.getSelection())
					return b;
			}
			return null;
		}
		return combo;
	}
}