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