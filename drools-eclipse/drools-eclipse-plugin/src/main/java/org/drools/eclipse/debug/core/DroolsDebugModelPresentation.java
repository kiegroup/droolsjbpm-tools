package org.drools.eclipse.debug.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.internal.debug.ui.JDIModelPresentation;

public class DroolsDebugModelPresentation extends JDIModelPresentation {

	protected String getBreakpointText(IBreakpoint breakpoint) {
		if (breakpoint instanceof DroolsLineBreakpoint) {
			DroolsLineBreakpoint breakp = ((DroolsLineBreakpoint) breakpoint);
            int lineNumber = breakp.getDRLLineNumber();
            int real;
            try {
                real = breakp.getLineNumber();
            } catch ( CoreException e ) {
                return breakpoint.getMarker().getResource().getName() + " [line: " + lineNumber + "] real: NA!!"; 
            }
			return breakpoint.getMarker().getResource().getName() + " [line: " + lineNumber + "] real: "+real;
		}
		return super.getBreakpointText(breakpoint);
	}
	    
}
