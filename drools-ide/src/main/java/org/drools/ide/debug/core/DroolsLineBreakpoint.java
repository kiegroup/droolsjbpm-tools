package org.drools.ide.debug.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.internal.debug.core.breakpoints.JavaLineBreakpoint;

public class DroolsLineBreakpoint extends JavaLineBreakpoint {
	
	/**
	 * Default constructor is required for the breakpoint manager
	 * to re-create persisted breakpoints. After instantiating a breakpoint,
	 * the <code>setMarker(...)</code> method is called to restore
	 * this breakpoint's attributes.
	 */
	public DroolsLineBreakpoint() {
	}
	
	/**
	 * Constructs a line breakpoint on the given resource at the given
	 * line number.
	 * 
	 * @param resource file on which to set the breakpoint
	 * @param lineNumber line number of the breakpoint
	 * @throws CoreException if unable to create the breakpoint
	 */
	public DroolsLineBreakpoint(IResource resource, int lineNumber)
			throws CoreException {
   		super(resource, getRuleClassName(resource, lineNumber), 
			getRuleLineNumber(resource, lineNumber), -1, -1, 0, true, 
			createAttributesMap(lineNumber), IDroolsDebugConstants.DROOLS_MARKER_TYPE);
	}
	
	private static Map createAttributesMap(int lineNumber) {
		Map map = new HashMap();
		map.put(IDroolsDebugConstants.DRL_LINE_NUMBER, new Integer(lineNumber));
		return map;
	}
	
	public int getDRLLineNumber() {
		return getMarker().getAttribute(IDroolsDebugConstants.DRL_LINE_NUMBER, -1);
	}
	
	public String getModelIdentifier() {
		return IDroolsDebugConstants.ID_DROOLS_DEBUG_MODEL;
	}
	
	private static String getRuleClassName(IResource resource, int lineNumber) {
		String ruleName = "Hello World";
		return "Rule_" + ruleName.replaceAll("[^\\w$]", "_") + "_0";
	}
	
	private static int getRuleLineNumber(IResource resource, int lineNumber) {
		// TODO
		return 7;
	}
}