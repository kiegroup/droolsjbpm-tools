package org.drools.eclipse.debug.core;

import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIThisVariable;

import com.sun.jdi.ObjectReference;

public class MVELThisVariable extends JDIThisVariable {

    private String label;

    public MVELThisVariable(JDIDebugTarget target, ObjectReference object, String label) {
        super( target, object );
        this.label = label;
    }

    public String getName() {
        return label;
    }

    public boolean isPublic() throws DebugException {
        return true;
    }
}
