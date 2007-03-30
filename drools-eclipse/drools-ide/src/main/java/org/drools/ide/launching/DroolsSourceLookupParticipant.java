package org.drools.ide.launching;

import org.drools.ide.DRLInfo.FunctionInfo;
import org.drools.ide.DRLInfo.RuleInfo;
import org.drools.ide.debug.core.DroolsStackFrame;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.launching.sourcelookup.containers.JavaSourceLookupParticipant;

public class DroolsSourceLookupParticipant extends JavaSourceLookupParticipant {

	public String getSourceName(Object object) throws CoreException {
		if (object instanceof DroolsStackFrame) {
			RuleInfo ruleInfo = ((DroolsStackFrame) object).getExecutingRuleInfo();
			if (ruleInfo != null) {
				return ruleInfo.getSourcePathName();
			}
			FunctionInfo functionInfo = ((DroolsStackFrame) object).getExecutingFunctionInfo();
			if (functionInfo != null) {
				return functionInfo.getSourcePathName();
			}
		}
		return super.getSourceName(object);
	}
	
}
