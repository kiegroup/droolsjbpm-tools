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
