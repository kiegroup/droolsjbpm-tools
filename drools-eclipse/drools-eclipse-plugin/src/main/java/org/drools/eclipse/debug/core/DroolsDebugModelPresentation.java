package org.drools.eclipse.debug.core;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.internal.debug.ui.JDIModelPresentation;

public class DroolsDebugModelPresentation extends JDIModelPresentation {

	protected String getBreakpointText(IBreakpoint breakpoint) {
		if (breakpoint instanceof DroolsLineBreakpoint) {
			int lineNumber = ((DroolsLineBreakpoint) breakpoint).getDRLLineNumber();
			return breakpoint.getMarker().getResource().getName() + " [line: " + lineNumber + "]";
		}
		return super.getBreakpointText(breakpoint);
	}
	
}
