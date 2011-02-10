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
