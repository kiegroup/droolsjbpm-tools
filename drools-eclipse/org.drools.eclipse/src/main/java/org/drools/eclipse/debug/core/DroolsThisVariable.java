package org.drools.eclipse.debug.core;

import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIThisVariable;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;

public class DroolsThisVariable extends JDIThisVariable {

	public DroolsThisVariable(JDIDebugTarget target, ObjectReference object) {
		super(target, object);
	}
	
	protected Value retrieveValue() {
		return super.retrieveValue();
	}
}
