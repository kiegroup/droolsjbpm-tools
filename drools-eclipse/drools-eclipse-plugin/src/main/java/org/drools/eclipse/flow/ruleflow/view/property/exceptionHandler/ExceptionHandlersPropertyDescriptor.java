package org.drools.eclipse.flow.ruleflow.view.property.exceptionHandler;

import org.drools.eclipse.flow.common.view.property.ListPropertyDescriptor;
import org.drools.knowledge.definitions.process.Process;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.widgets.Composite;

public class ExceptionHandlersPropertyDescriptor extends ListPropertyDescriptor {
	
	private Process process;
	
	public ExceptionHandlersPropertyDescriptor(Object id, String displayName, Process process) {
		super(id, displayName, ExceptionHandlersCellEditor.class);
		this.process = process;
	}
	
	public CellEditor createPropertyEditor(Composite parent) {
		return new ExceptionHandlersCellEditor(parent, process);
	}

}
