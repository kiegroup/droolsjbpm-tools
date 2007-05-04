package org.drools.eclipse.debug.core;

import java.util.HashMap;
import java.util.Map;

import org.drools.eclipse.DRLInfo;
import org.drools.eclipse.DroolsEclipsePlugin;
import org.drools.eclipse.DRLInfo.FunctionInfo;
import org.drools.eclipse.DRLInfo.RuleInfo;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
   		super(resource, "", -1,  -1, -1, 0, true, 
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
	
	public void setJavaBreakpointProperties() throws CoreException {
		IMarker marker = getMarker();
		int drlLineNumber = getDRLLineNumber();
		if (marker.exists()) {
			marker.setAttribute(TYPE_NAME, getRuleClassName(marker.getResource(), drlLineNumber));
			marker.setAttribute(IMarker.LINE_NUMBER, getRuleLineNumber(getMarker().getResource(), drlLineNumber));
		}
	}
	
	private String getRuleClassName(IResource resource, int lineNumber) throws CoreException {
		try {
			DRLInfo drlInfo = DroolsEclipsePlugin.getDefault().parseResource(resource, true);
			if (drlInfo != null) {
				RuleInfo ruleInfo = drlInfo.getRuleInfo(lineNumber);
				if (ruleInfo != null) {
					return ruleInfo.getClassName();
				}
				FunctionInfo functionInfo = drlInfo.getFunctionInfo(lineNumber);
				if (functionInfo != null) {
					return functionInfo.getClassName();
				}
			}
			throw new CoreException(new Status(IStatus.ERROR, DroolsEclipsePlugin.getUniqueIdentifier(), 0,
				"Cannot determine ruleClassName for " + resource + " " + lineNumber, null));
		} catch (Throwable t) {
			throw new CoreException(new Status(IStatus.ERROR, DroolsEclipsePlugin.getUniqueIdentifier(), 0,
				"Cannot determine ruleClassName for " + resource + " " + lineNumber, t));
		}
	}
	
	private int getRuleLineNumber(IResource resource, int lineNumber) throws CoreException {
		try {
			DRLInfo drlInfo = DroolsEclipsePlugin.getDefault().parseResource(resource, true);
			if (drlInfo != null) {
				RuleInfo ruleInfo = drlInfo.getRuleInfo(lineNumber);
				if (ruleInfo != null) {
					if (ruleInfo.getConsequenceDrlLineNumber() < lineNumber) {
						return ruleInfo.getConsequenceJavaLineNumber()
							+ (lineNumber - ruleInfo.getConsequenceDrlLineNumber() + 1);
					}
				}
				FunctionInfo functionInfo = drlInfo.getFunctionInfo(lineNumber);
				if (functionInfo != null) {
					return functionInfo.getJavaLineNumber()
						+ (lineNumber - functionInfo.getDrlLineNumber());
				}
			}
			throw new CoreException(new Status(IStatus.ERROR, DroolsEclipsePlugin.getUniqueIdentifier(), 0,
				"Cannot determine ruleLineNumber for " + resource + " " + lineNumber, null));
		} catch(Throwable t) {
			throw new CoreException(new Status(IStatus.ERROR, DroolsEclipsePlugin.getUniqueIdentifier(), 0,
				"Cannot determine ruleLineNumber for " + resource + " " + lineNumber, t));
		}
	}
}