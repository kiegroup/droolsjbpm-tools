package org.drools.eclipse.launching;

import org.drools.eclipse.DRLInfo.FunctionInfo;
import org.drools.eclipse.DRLInfo.RuleInfo;
import org.drools.eclipse.debug.core.DroolsStackFrame;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.launching.sourcelookup.containers.JavaSourceLookupParticipant;

public class DroolsSourceLookupParticipant extends JavaSourceLookupParticipant {

	public String getSourceName(Object object) throws CoreException {
		if (object instanceof DroolsStackFrame) {
			RuleInfo ruleInfo = ((DroolsStackFrame) object).getExecutingRuleInfo();
			if (ruleInfo != null) {
                String p = ruleInfo.getSourcePathName();
				return p;
			}
			FunctionInfo functionInfo = ((DroolsStackFrame) object).getExecutingFunctionInfo();
			if (functionInfo != null) {
				return functionInfo.getSourcePathName();
			}
		}
		return super.getSourceName(object);
	}
	
}
