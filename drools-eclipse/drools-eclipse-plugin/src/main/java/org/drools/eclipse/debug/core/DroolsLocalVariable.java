package org.drools.eclipse.debug.core;

import org.eclipse.jdt.internal.debug.core.model.JDILocalVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;

import com.sun.jdi.LocalVariable;

public class DroolsLocalVariable extends JDILocalVariable {
	
	public DroolsLocalVariable(JDIStackFrame frame, LocalVariable local) {
		super(frame, local);
	}

	protected void setLocal(LocalVariable local) {
		super.setLocal(local);
	}
	
	protected LocalVariable getLocal() {
		return super.getLocal();
	}
	
}
