package org.drools.ide.debug.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.drools.ide.DroolsIDEPlugin;
import org.drools.ide.RuleInfo;
import org.drools.lang.descr.PackageDescr;
import org.drools.lang.descr.RuleDescr;
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
	
	private static String getRuleClassName(IResource resource, int lineNumber) throws CoreException {
		try {
			PackageDescr packageDescr = DroolsIDEPlugin.getDefault().parseResource(resource, true);
			if (packageDescr != null) {
				Iterator rules = packageDescr.getRules().iterator();
				int ruleLine = -1;
				RuleDescr resultRule = null;
				while (rules.hasNext()) {
					RuleDescr rule = (RuleDescr) rules.next();
					if (rule.getConsequenceLine() > ruleLine && rule.getConsequenceLine() < lineNumber) {
						ruleLine = rule.getConsequenceLine();
						resultRule = rule;
					}
				}
				if (resultRule != null && resultRule.getClassName() != null) {
					return packageDescr.getName() + "." + resultRule.getClassName();
				}
			}
			throw new CoreException(new Status(IStatus.ERROR, DroolsIDEPlugin.getUniqueIdentifier(), 0,
				"Cannot determine ruleClassName for " + resource + " " + lineNumber, null));
		} catch(Throwable t) {
			throw new CoreException(new Status(IStatus.ERROR, DroolsIDEPlugin.getUniqueIdentifier(), 0,
				"Cannot determine ruleClassName for " + resource + " " + lineNumber, t));
		}
	}
	
	private static int getRuleLineNumber(IResource resource, int lineNumber) throws CoreException {
		// TODO remove duplicated code
		try {
			PackageDescr packageDescr = DroolsIDEPlugin.getDefault().parseResource(resource, true);
			if (packageDescr != null) {
				Iterator rules = packageDescr.getRules().iterator();
				int ruleLine = -1;
				RuleDescr resultRule = null;
				while (rules.hasNext()) {
					RuleDescr rule = (RuleDescr) rules.next();
					if (rule.getConsequenceLine() > ruleLine && rule.getConsequenceLine() < lineNumber) {
						ruleLine = rule.getConsequenceLine();
						resultRule = rule;
					}
				}
				if (resultRule != null && resultRule.getClassName() != null) {
					String ruleClassName = packageDescr.getName() + "." + resultRule.getClassName();
					RuleInfo ruleInfo = DroolsIDEPlugin.getDefault().getRuleInfoByClass(ruleClassName);
					if (ruleInfo != null) {
						return ruleInfo.getConsequenceJavaLineNumber() + lineNumber - ruleLine;
					}
				}
			}
			throw new CoreException(new Status(IStatus.ERROR, DroolsIDEPlugin.getUniqueIdentifier(), 0,
				"Cannot determine ruleLineNumber for " + resource + " " + lineNumber, null));
		} catch(Throwable t) {
			throw new CoreException(new Status(IStatus.ERROR, DroolsIDEPlugin.getUniqueIdentifier(), 0,
				"Cannot determine ruleLineNumber for " + resource + " " + lineNumber, t));
		}
	}
}