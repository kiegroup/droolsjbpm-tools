package org.drools.ide.debug.core;

import java.util.ArrayList;
import java.util.List;

import org.drools.ide.DroolsIDEPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;

import com.sun.jdi.StackFrame;

public class DroolsStackFrame extends JDIStackFrame {

	private static final String CONSEQUENCE_SIGNATURE = "(Lorg/drools/spi/KnowledgeHelper";
	
	public DroolsStackFrame(DroolsThread thread, StackFrame frame, int depth) {
		super(thread, frame, depth);
	}
	
	public boolean isExecutingRule() {
		try {
			String methodName = getMethodName();
			String signature = getSignature();
			if ("consequence".equals(methodName) && signature.startsWith(CONSEQUENCE_SIGNATURE)) {
				// TODO return name of drl
				return true;
			}
		} catch (DebugException exc) {
			DroolsIDEPlugin.log(exc);
		}
		return false;
	}
	
	public int getLineNumber() throws DebugException {
		if (isExecutingRule()) {
			return convertToDRLLineNumber();
		}
		return super.getLineNumber();
	}

	private int convertToDRLLineNumber() {
		// TODO
		return 21;
	}
	
	protected JDIStackFrame bind(StackFrame frame, int depth) {
		return super.bind(frame, depth);
	}
	
	protected StackFrame getUnderlyingStackFrame() throws DebugException {
		return super.getUnderlyingStackFrame();
	}
	
	protected void setUnderlyingStackFrame(StackFrame frame) {
		super.setUnderlyingStackFrame(frame);
	}
	
	public IVariable[] getVariables() throws DebugException {
		IVariable[] variables = super.getVariables();
		List result = new ArrayList((variables.length - 1)/2);
		for (int i = 0; i < variables.length; i++) {
			String name = variables[i].getName();
			if (!(name.equals("drools")) && !(name.endsWith("__Handle__"))) {
				result.add(variables[i]);
			}
		}
		return (IVariable[]) result.toArray(new IVariable[result.size()]);
	}
	
}
