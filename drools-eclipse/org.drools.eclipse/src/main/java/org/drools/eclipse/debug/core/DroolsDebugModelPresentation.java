/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
