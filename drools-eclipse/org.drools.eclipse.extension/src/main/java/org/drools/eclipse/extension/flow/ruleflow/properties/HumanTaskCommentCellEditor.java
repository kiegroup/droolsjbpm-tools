package org.drools.eclipse.extension.flow.ruleflow.properties;

import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.jbpm.workflow.core.node.WorkItemNode;

public class HumanTaskCommentCellEditor extends DialogCellEditor {

	private WorkItemNode workItemNode;

	public HumanTaskCommentCellEditor(Composite parent,
			WorkItemNode workItemNode) {
		super(parent);
		this.workItemNode = workItemNode;
	}

	@Override
	protected Object openDialogBox(Control cellEditorWindow) {
		HumanTaskCommentDialog dialog = new HumanTaskCommentDialog(
				cellEditorWindow.getShell(), "Custom Comments Dialog ");
		String value = (String) getValue();
		if (value != null) {
			dialog.setValue(value);
		}
		int result = dialog.open();
		if (result == Window.CANCEL) {
			return null;
		}
		return dialog.getValue();
	}

}
