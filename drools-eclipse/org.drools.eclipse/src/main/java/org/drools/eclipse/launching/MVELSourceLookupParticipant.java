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

package org.drools.eclipse.launching;

import org.drools.eclipse.DRLInfo.FunctionInfo;
import org.drools.eclipse.DRLInfo.RuleInfo;
import org.drools.eclipse.debug.core.DroolsDebugTarget;
import org.drools.eclipse.debug.core.MVELStackFrame;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.jdt.launching.sourcelookup.containers.JavaSourceLookupParticipant;

class MVELSourceLookupParticipant extends JavaSourceLookupParticipant {
    public void dispose() {
        //do nothing
    }

    public Object[] findSourceElements(Object object) throws CoreException {
        if ( object instanceof MVELStackFrame ) {
            MVELStackFrame frame = (MVELStackFrame) object;

            //int lineNumber = frame.getBreakpointLineNumber();
            String mvelName = frame.getMVELName();

            IDebugTarget target = frame.getDebugTarget();
            if ( target instanceof DroolsDebugTarget ) {
                DroolsDebugTarget droolsTarget = (DroolsDebugTarget) target;
                Object bpoint = droolsTarget.getDroolsBreakpoint( mvelName );
                return new Object[]{bpoint};
            }
        }
        return null;
    }

    public String getSourceName(Object object) throws CoreException {
        if ( object instanceof MVELStackFrame ) {
            MVELStackFrame frame = (MVELStackFrame) object;
            RuleInfo ruleInfo = frame.getExecutingRuleInfo();
            if ( ruleInfo != null ) {
                String sourcePath = ruleInfo.getSourcePathName();
                return sourcePath;
            }
            FunctionInfo functionInfo = frame.getExecutingFunctionInfo();
            if ( functionInfo != null ) {
                return functionInfo.getSourcePathName();
            }
        }
        return super.getSourceName( object );
    }

    public void init(ISourceLookupDirector director) {
        //do nothing
    }

    public void sourceContainersChanged(ISourceLookupDirector director) {
        //do nothing
    }
}
