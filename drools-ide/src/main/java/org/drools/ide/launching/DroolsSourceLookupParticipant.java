package org.drools.ide.launching;

import org.drools.ide.debug.core.DroolsStackFrame;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.launching.sourcelookup.containers.JavaSourceLookupParticipant;

public class DroolsSourceLookupParticipant extends JavaSourceLookupParticipant {

	public String getSourceName(Object object) throws CoreException {
		if (object instanceof DroolsStackFrame) {
			if (((DroolsStackFrame) object).isExecutingRule()) {
				// TODO return name of drl
				return "Sample.drl";
			}
		}
		return super.getSourceName(object);
	}
	
}
